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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.web.extension;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.exception.DefaultExceptionHandler;

import org.apache.log4j.Logger;

import eionet.meta.service.ServiceException;
import eionet.web.action.ErrorActionBean;
import eionet.web.action.ErrorActionBean.ErrorType;

/**
 * Data Dictionary stripes exception handler.
 * 
 * @author Juhan Voolaid
 */
public class DDExceptionHandler extends DefaultExceptionHandler {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(DDExceptionHandler.class);

    /**
     * Handles unexpected exception.
     * 
     * @param exc
     * @param request
     * @param response
     * @return
     */
    public Resolution handleGenericException(Exception exc, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.error("Exception caught", exc);
        return new RedirectResolution(ErrorActionBean.class).addParameter("message", exc.getMessage());
    }

    /**
     * Handles service exception.
     * 
     * @param exc
     * @param request
     * @param response
     * @return ErrorResolution if it is a 404 error, otherwise RedirectResolution to forward to ErrorAction
     */
    public Resolution handleServiceException(ServiceException exc, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.error("Exception caught", exc);
        String message = exc.getMessage();
        ErrorActionBean.ErrorType errorType = ErrorActionBean.ErrorType.UNKNOWN;

        HashMap<String, Object> errorParameters = exc.getErrorParameters();
        if (errorParameters != null && errorParameters.containsKey(ErrorActionBean.ERROR_TYPE_KEY)) {
            errorType = (ErrorType) errorParameters.get(ErrorActionBean.ERROR_TYPE_KEY);
        }

        if (errorType.equals(ErrorActionBean.ErrorType.NOT_FOUND_404)) {
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, message);
        } else {
            return new RedirectResolution(ErrorActionBean.class).addParameter("message", message).addParameter("type", errorType);
        }
    }

}
