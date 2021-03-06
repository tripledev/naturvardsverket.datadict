/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */
package eionet.meta.scheduled;

import java.text.ParseException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Class to set up quartz jobs.
 *
 * @author Kaido Laine, enver
 */
public class DDJobScheduler implements ServletContextListener {
    /**
     * default interval for jobs if not defined in the props file. 24h.
     */
    static final long DEFAULT_SCHEDULE_INTERVAL = 24 * 60 * 60 * 1000;

    /** Logger instance. */
    private static final Log LOGGER = LogFactory.getLog(DDJobScheduler.class);

    /** Scheduler instance. */
    private static Scheduler quartzScheduler = null;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            initQuartzScheduler();
            LOGGER.debug("DDJobScheduler is initialized");
        } catch (SchedulerException e) {
            LOGGER.fatal("Cannot initialize quartz scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    } // end of method contextInitialized

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            shutdownQuartzScheduler();
        } catch (SchedulerException e) {
            LOGGER.warn("Cannot shutdown quartz scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    } // end of method contextDestroyed

    /**
     * Initialize quartz scheduler.
     *
     * @throws SchedulerException
     *             when an error occurs during initialization.
     */
    private static synchronized void initQuartzScheduler() throws SchedulerException {
        if (quartzScheduler == null) {
            quartzScheduler = StdSchedulerFactory.getDefaultScheduler();
            quartzScheduler.start();
        }
    } // end of function initQuartzScheduler

    /**
     * De-initialize quartz scheduler.
     *
     * @throws SchedulerException
     *             when an error occurs during shutdown.
     */
    private static synchronized void shutdownQuartzScheduler() throws SchedulerException {
        if (quartzScheduler != null) {
            quartzScheduler.shutdown();
        }
    } // end of function shutdownQuartzScheduler

    /**
     * Schedules an interval job.
     *
     * @param repeatInterval
     *            repeat interval in millis
     * @param jobDetails
     *            job details
     * @throws SchedulerException
     *             if scheduling fails
     */
    public static synchronized void scheduleIntervalJob(long repeatInterval, JobDetail jobDetails) throws SchedulerException {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
        triggerBuilder.withIdentity(jobDetails.getKey().getName(), jobDetails.getKey().getGroup());
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        simpleScheduleBuilder.withIntervalInMilliseconds(repeatInterval);
        simpleScheduleBuilder.repeatForever();
        triggerBuilder.withSchedule(simpleScheduleBuilder);
        quartzScheduler.scheduleJob(jobDetails, triggerBuilder.build());
    } // end of function scheduleIntervalJob

    /**
     * Schedules a job based on crontab format.
     *
     * @param cronExpression
     *            crontab time expression
     * @param jobDetails
     *            job details
     * @throws SchedulerException
     *             if scheduling fails
     * @throws java.text.ParseException
     *             if crontab format is incorrect
     */
    public static synchronized void scheduleCronJob(String cronExpression, JobDetail jobDetails) throws SchedulerException,
            ParseException {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
        triggerBuilder.withIdentity(jobDetails.getKey().getName(), jobDetails.getKey().getGroup());
        triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression));
        triggerBuilder.forJob(jobDetails);
        quartzScheduler.scheduleJob(jobDetails, triggerBuilder.build());
    } // end of function scheduleCronJob

} // end of class DDJobScheduler
