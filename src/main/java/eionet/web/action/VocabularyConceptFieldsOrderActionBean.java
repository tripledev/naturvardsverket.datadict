package eionet.web.action;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.meta.dao.DAOException;
import eionet.meta.dao.domain.VocabularyConceptFieldsOrderElement;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IVocabularyConceptFieldsOrderService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;

@UrlBinding("/vocabularyConceptFieldsOrder/{vocId}/{$event}")
public class VocabularyConceptFieldsOrderActionBean extends AbstractActionBean{

    /** */
    private static final Logger LOGGER = Logger.getLogger(VocabularyConceptFieldsOrderActionBean.class);

    /** */
    private static final String DEFAULT_JSP = "/pages/vocabularies/conceptFieldsOrder.jsp";

    /** */
    @SpringBean
    private IVocabularyConceptFieldsOrderService conceptFieldsOrderService;

    /** */
    @SpringBean
    private IVocabularyService vocabularyService;

    /** */
    private int vocabularyId;

    /** */
    private VocabularyFolder vocabulary;

    /** */
    private List<VocabularyConceptFieldsOrderElement> orderElements;

    @DefaultHandler
    @HandlesEvent(value = "list")
    public Resolution list() throws ServiceException {

        LOGGER.trace("Entered list method...");

        return new ForwardResolution(DEFAULT_JSP);
    }

    /**
     *
     * @throws DAOException
     * @throws ServiceException
     */
    @ValidationMethod
    public void validateAnyEvent() throws DAOException, ServiceException {

        vocabulary = vocabularyService.getVocabularyFolder(vocabularyId);
        if (vocabulary == null) {
            throw new ServiceException("No vocabulary found by such id: " + vocabularyId);
        }

        if (!vocabulary.isWorkingCopy() && !isUserWorkingCopy()) {
            throw new ServiceException("Concept fields display order can only be modified on a vocabulary's working copy that you have checked out!");
        }
    }

    /**
     * True, if logged in user is the working user of the vocabulary.
     *
     * @return
     */
    private boolean isUserWorkingCopy() {
        boolean result = false;
        String sessionUser = getUserName();
        if (!StringUtils.isBlank(sessionUser)) {
            if (vocabulary != null) {
                String workingUser = vocabulary.getWorkingUser();
                return vocabulary.isWorkingCopy() && StringUtils.equals(workingUser, sessionUser);
            }
        }

        return result;
    }

    /**
     * @return the orderElements
     */
    public List<VocabularyConceptFieldsOrderElement> getOrderElements() {

        if (orderElements == null) {
            orderElements = conceptFieldsOrderService.getOrderElements(vocabularyId);
        }
        return orderElements;
    }

    /**
     * @return the vocabularyId
     */
    public int getVocabularyId() {
        return vocabularyId;
    }

    /**
     * @param vocabularyId the vocabularyId to set
     */
    public void setVocabularyId(int vocabularyId) {
        this.vocabularyId = vocabularyId;
    }
}
