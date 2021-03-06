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

package eionet.web.action;

import eionet.meta.dao.domain.DDApiKey;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.json.VocabularyJSONOutputHelper;
import eionet.meta.service.IApiKeyService;
import eionet.meta.service.IJWTService;
import eionet.meta.service.IRDFVocabularyImportService;
import eionet.meta.service.IVocabularyImportService.MissingConceptsAction;
import eionet.meta.service.IVocabularyImportService.UploadAction;
import eionet.meta.service.IVocabularyImportService.UploadActionBefore;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;
import net.sf.json.JSONObject;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vocabulary folder API action bean.
 *
 * @author enver
 */
@UrlBinding("/api/vocabulary/{vocabularyFolder.folderName}/{vocabularyFolder.identifier}/{$event}")
public class VocabularyFolderApiActionBean extends AbstractActionBean {

    //Constants
    /**
     * Request parameter name for action before.
     */
    public static final String ACTION_BEFORE_REQ_PARAM = "actionBefore";

    /**
     * Request parameter name for missing concepts.
     */
    public static final String MISSING_CONCEPTS_REQ_PARAM = "missingConcepts";

    /**
     * Request parameter name for action.
     */
    public static final String ACTION_REQ_PARAM = "action";

    /**
     * API key header for request.
     */
    public static final String JWT_API_KEY_HEADER = "X-DD-API-KEY";

    /**
     * Keyword for content type.
     */
    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * Valid content type for RDF upload.
     */
    public static final String VALID_CONTENT_TYPE_FOR_RDF_UPLOAD = "application/rdf+xml";

    /**
     * API Key identifier in json.
     */
    public static final String API_KEY_IDENTIFIER_IN_JSON = "API_KEY";

    /**
     * Created time identifier in json.
     */
    public static final String TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON = "iat";

    /**
     * JWT Key.
     */
    private static final String JWT_KEY = Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_KEY);

    /**
     * JWT Audience.
     */
    private static final String JWT_AUDIENCE = Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);

    /**
     * JWT Expiration in minutes for signing.
     */
    private static final int JWT_EXPIRATION_IN_MINUTES = Props.getIntProperty(PropsIF.DD_VOCABULARY_API_JWT_EXP_IN_MINUTES);

    /**
     * JWT Timeout in minutes for verification (used to validate if sent token is still active or deprecated).
     */
    private static final int JWT_TIMEOUT_IN_MINUTES = Props.getIntProperty(PropsIF.DD_VOCABULARY_API_JWT_TIMEOUT_IN_MINUTES);

    /**
     * JWT Algorithm for signing.
     */
    private static final String JWT_SIGNING_ALGORITHM = Props.getProperty(PropsIF.DD_VOCABULARY_ADI_JWT_ALGORITHM);

    //Static variables
    /**
     * Reserved API names, that cannot be vocabulary concept identifiers.
     */
    public static final List<String> RESERVED_VOCABULARY_API_EVENTS;


    /**
     * Static block for initializations.
     */
    static {
        //Create supported/reserved api names
        RESERVED_VOCABULARY_API_EVENTS = new ArrayList<String>();
        RESERVED_VOCABULARY_API_EVENTS.add("uploadRdf");
    } // end of static block

    /**
     * Json output format.
     */
    public static final String JSON_FORMAT = "application/json";

    //Instance members
    /**
     * Vocabulary service.
     */
    @SpringBean
    private IVocabularyService vocabularyService;

    /**
     * JWT service.
     */
    @SpringBean
    private IJWTService jwtService;

    /**
     * API-Key service.
     */
    @SpringBean
    private IApiKeyService apiKeyService;

    /**
     * Vocabulary folder.
     */
    private VocabularyFolder vocabularyFolder;

    /**
     * RDF Import Service.
     */
    @SpringBean
    private IRDFVocabularyImportService vocabularyRdfImportService;

    /**
     * Action before param.
     */
    private String actionBefore;

    /**
     * Action param.
     */
    private String action;

    /**
     * Missing concepts action param.
     */
    private String missingConcepts;

    //Method definitions

    /**
     * Imports RDF contents into vocabulary.
     *
     * @return resolution
     * @throws eionet.meta.service.ServiceException when an error occurs
     */
    public Resolution uploadRdf() throws ServiceException {
        try {
            StopWatch timer = new StopWatch();
            timer.start();

            //Read RDF from request body and params from url
            HttpServletRequest request = getContext().getRequest();

            LOGGER.info("uploadRdf API - called with remote address: " + request.getRemoteAddr() + ", and remote host: " + request.getRemoteHost());

            String contentType = request.getHeader(CONTENT_TYPE_HEADER);
            if (!StringUtils.startsWithIgnoreCase(contentType, VALID_CONTENT_TYPE_FOR_RDF_UPLOAD)) {
                LOGGER.error("uploadRdf API - invalid content type: " + contentType);
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Invalid content-type for RDF upload", ErrorActionBean.RETURN_ERROR_EVENT);
            }

            String keyHeader = request.getHeader(JWT_API_KEY_HEADER);
            if (StringUtils.isNotBlank(keyHeader)) {
                String jsonWebToken = keyHeader;

                try {
                    JSONObject jsonObject = jwtService.verify(JWT_KEY, JWT_AUDIENCE, jsonWebToken);

                    long createdTimeInSeconds = jsonObject.getLong(TOKEN_CREATED_TIME_IDENTIFIER_IN_JSON);

                    long nowInSeconds = Calendar.getInstance().getTimeInMillis() / 1000l;
                    if (nowInSeconds > (createdTimeInSeconds + (JWT_TIMEOUT_IN_MINUTES * 60))) {
                        LOGGER.error("uploadRdf API - Deprecated token");
                        return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Deprecated token", ErrorActionBean.RETURN_ERROR_EVENT);
                    }

                    String apiKey = jsonObject.getString(API_KEY_IDENTIFIER_IN_JSON);

                    DDApiKey ddApiKey = apiKeyService.getApiKey(apiKey);

                    if (ddApiKey == null) {
                        LOGGER.error("uploadRdf API - Invalid key");
                        return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Invalid key", ErrorActionBean.RETURN_ERROR_EVENT);
                    }

                    //Note: Scope can also be used

                    if (ddApiKey.getExpires() != null) {
                        Date now = Calendar.getInstance().getTime();
                        if (now.after(ddApiKey.getExpires())) {
                            LOGGER.error("uploadRdf API - Expired key");
                            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Expired key", ErrorActionBean.RETURN_ERROR_EVENT);
                        }
                    }

                    String remoteAddr = ddApiKey.getRemoteAddr();
                    if (StringUtils.isNotBlank(remoteAddr)) {
                        if (!StringUtils.equals(remoteAddr, request.getRemoteAddr()) && !StringUtils.equals(remoteAddr, request.getRemoteHost())) {
                            LOGGER.error("uploadRdf API - Invalid remote end point");
                            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: Invalid remote end point", ErrorActionBean.RETURN_ERROR_EVENT);
                        }
                    }

                } catch (Exception e) {
                    LOGGER.error("uploadRdf API - Cannot verify key", e);
                    return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "Cannot authorize: " + e.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
                }
            } else {
                LOGGER.error("uploadRdf API - Key missing");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401, "API Key cannot be missing", ErrorActionBean.RETURN_ERROR_EVENT);
            }

            LOGGER.info("uploadRdf API - request authorized");

            //These lines are redundant but for any case, are kept in code. Stripes handle well, request parameters.
            if (this.actionBefore == null && request.getParameter(ACTION_BEFORE_REQ_PARAM) != null) {
                setActionBefore(request.getParameter(ACTION_BEFORE_REQ_PARAM));
            }

            if (this.missingConcepts == null && request.getParameter(MISSING_CONCEPTS_REQ_PARAM) != null) {
                setMissingConcepts(request.getParameter(MISSING_CONCEPTS_REQ_PARAM));
            }

            if (this.action == null && request.getParameter(ACTION_REQ_PARAM) != null) {
                setAction(request.getParameter(ACTION_REQ_PARAM));
            }

            //Validate parameters
            UploadActionBefore uploadActionBefore = null;
            UploadAction uploadAction = null;
            MissingConceptsAction missingConceptsAction = null;
            try {
                uploadActionBefore = validateAndGetUploadActionBefore(vocabularyRdfImportService.getSupportedActionBefore(true),
                        vocabularyRdfImportService.getDefaultActionBefore(true));
                uploadAction = validateAndGetUploadAction(vocabularyRdfImportService.getSupportedAction(true),
                        vocabularyRdfImportService.getDefaultAction(true));
                missingConceptsAction = validateAndGetMissingConceptsAction(vocabularyRdfImportService.getSupportedMissingConceptsAction(true),
                        vocabularyRdfImportService.getDefaultMissingConceptsAction(true));
            } catch (IllegalArgumentException e) {
                LOGGER.error("uploadRdf API - Illegal argument: " + e.getMessage());
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, e.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
            }

            LOGGER.info("uploadRdf API - parameters are valid");

            VocabularyFolder workingCopy = null;
            try {
                workingCopy = vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(),
                        vocabularyFolder.getIdentifier(), true);
            } catch (ServiceException e) {
                HashMap<String, Object> errorParameters = e.getErrorParameters();
                if (errorParameters == null ||
                        !ErrorActionBean.ErrorType.NOT_FOUND_404.equals(errorParameters.get(ErrorActionBean.ERROR_TYPE_KEY))) {
                    LOGGER.error("uploadRdf API - Vocabulary has working copy", e);
                    return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.FORBIDDEN_403, "Vocabulary should NOT have a working copy", ErrorActionBean.RETURN_ERROR_EVENT);
                }
            }

            if (workingCopy != null && workingCopy.isWorkingCopy()) {
                LOGGER.error("uploadRdf API - Vocabulary has working copy");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.FORBIDDEN_403, "Vocabulary should NOT have a working copy", ErrorActionBean.RETURN_ERROR_EVENT);
            }

            try {
                vocabularyFolder = vocabularyService.getVocabularyFolder(vocabularyFolder.getFolderName(),
                        vocabularyFolder.getIdentifier(), false);
            } catch (ServiceException e) {
                HashMap<String, Object> errorParameters = e.getErrorParameters();
                if (errorParameters != null &&
                        ErrorActionBean.ErrorType.NOT_FOUND_404.equals(errorParameters.get(ErrorActionBean.ERROR_TYPE_KEY))) {
                    LOGGER.error("uploadRdf API - Vocabulary can NOT be found", e);
                    return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.NOT_FOUND_404, "Vocabulary can NOT be found", ErrorActionBean.RETURN_ERROR_EVENT);
                }
            }

            if (vocabularyFolder.isWorkingCopy()) {
                LOGGER.error("uploadRdf API - Vocabulary has working copy");
                return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.FORBIDDEN_403, "Vocabulary should NOT have a working copy", ErrorActionBean.RETURN_ERROR_EVENT);
            }

            LOGGER.info("uploadRdf API - Starting RDF import operation");

            //Reader rdfFileReader = new InputStreamReader(this.sourceFile.getInputStream(), CharEncoding.UTF_8); //KL 151216: input stream reading from request
            Reader rdfFileReader = new InputStreamReader(request.getInputStream(), CharEncoding.UTF_8);

            final List<String> systemMessages = this.vocabularyRdfImportService.importRdfIntoVocabulary(rdfFileReader,
                    vocabularyFolder, uploadActionBefore, uploadAction, missingConceptsAction);
            for (String systemMessage : systemMessages) {
                addSystemMessage(systemMessage);
                LOGGER.info(systemMessage);
            }

            StreamingResolution result = new StreamingResolution(JSON_FORMAT) {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    VocabularyJSONOutputHelper.writeJSON(response.getOutputStream(), systemMessages);
                }
            };

            timer.stop();
            LOGGER.info("uploadRdf API - RDF import completed, total time of execution: " + timer.toString());
            return result;
        } catch (ServiceException e) {
            LOGGER.error("uploadRdf API - Failed to import vocabulary RDF into db", e);
            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INTERNAL_SERVER_ERROR, e.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
        } catch (Exception e) {
            LOGGER.error("uploadRdf API - Failed to import vocabulary RDF into db, unexpected exception: ", e);
            return super.createErrorResolutionWithoutRedirect(ErrorActionBean.ErrorType.INVALID_INPUT, "Failed to import vocabulary RDF into db, unexpected exception: " + e.getMessage(), ErrorActionBean.RETURN_ERROR_EVENT);
        }
    } // end of method uploadRDF

    /**
     * TODO: TEMP METHOD for testing, will be removed.
     */
    private Resolution testUploadRdf() throws ServiceException {
        PostMethod post = new PostMethod(Props.getRequiredProperty(PropsIF.DD_URL) + "/api/vocabulary/test/geography/uploadRdf");
        post.setRequestHeader(CONTENT_TYPE_HEADER, VALID_CONTENT_TYPE_FOR_RDF_UPLOAD);

        Map<String, String> jwtPayload = new HashMap<String, String>();
        jwtPayload.put(API_KEY_IDENTIFIER_IN_JSON, "TestingAPIKey");

        post.setRequestHeader(JWT_API_KEY_HEADER, jwtService.sign(JWT_KEY, JWT_AUDIENCE, jwtPayload, JWT_EXPIRATION_IN_MINUTES, JWT_SIGNING_ALGORITHM));
        HttpClient httpClient = new HttpClient();
        try {
            httpClient.executeMethod(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    } // end of method testUploadRdf

    /**
     * Validator and enum value converter for upload action before parameter.
     *
     * @param supportedUploadActionBefore supported action before list for this upload operation.
     * @param defaultValue                a default value if this action before param missing.
     * @return Converted enum value
     * @throws IllegalArgumentException when parameter is invalid or not supported.
     */
    private UploadActionBefore validateAndGetUploadActionBefore(List<UploadActionBefore> supportedUploadActionBefore,
                                                                UploadActionBefore defaultValue) throws IllegalArgumentException {
        if (defaultValue != null && (this.actionBefore == null || this.actionBefore.trim().length() < 1)) {
            return defaultValue;
        }

        try {
            UploadActionBefore uploadActionBefore = UploadActionBefore.valueOf(this.actionBefore);
            if (supportedUploadActionBefore != null && !supportedUploadActionBefore.contains(uploadActionBefore)) {
                throw new IllegalArgumentException("Not supported action before parameter: " + this.actionBefore);
            }
            return uploadActionBefore;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid action before parameter: " + this.actionBefore, e);
        }
    } // end of method validateUploadActionBefore

    /**
     * Validator and enum value converter for upload action parameter.
     *
     * @param supportedUploadAction supported action list for this upload operation.
     * @param defaultValue          a default value if this action param missing.
     * @return Converted enum value
     * @throws IllegalArgumentException when parameter is invalid or not supported.
     */
    private UploadAction validateAndGetUploadAction(List<UploadAction> supportedUploadAction,
                                                    UploadAction defaultValue)
            throws IllegalArgumentException {
        if (defaultValue != null && (this.action == null || this.action.trim().length() < 1)) {
            return defaultValue;
        }

        try {
            UploadAction uploadAction = UploadAction.valueOf(this.action);
            if (supportedUploadAction != null && !supportedUploadAction.contains(uploadAction)) {
                throw new IllegalArgumentException("Not supported action parameter: " + this.action);
            }
            return uploadAction;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid action parameter: " + this.action, e);
        }
    } // end of method validateAndGetUploadAction

    /**
     * Validator and enum value converter for upload missing concepts action parameter.
     *
     * @param supportedMissingConceptsAction supported missing concepts action list for this upload operation.
     * @param defaultValue                   a default value if this missing concepts action param missing.
     * @return Converted enum value
     * @throws IllegalArgumentException when parameter is invalid or not supported.
     */
    private MissingConceptsAction validateAndGetMissingConceptsAction(List<MissingConceptsAction> supportedMissingConceptsAction,
                                                                      MissingConceptsAction defaultValue) throws IllegalArgumentException {
        if (defaultValue != null && (this.missingConcepts == null || this.missingConcepts.trim().length() < 1)) {
            return defaultValue;
        }

        try {
            MissingConceptsAction missingConceptsAction = MissingConceptsAction.valueOf(this.missingConcepts);
            if (supportedMissingConceptsAction != null && !supportedMissingConceptsAction.contains(missingConceptsAction)) {
                throw new IllegalArgumentException("Not supported missing concepts action parameter: " + this.missingConcepts);
            }
            return missingConceptsAction;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid missing concepts action parameter: " + this.missingConcepts, e);
        }
    } // end of method validateAndGetMissingConceptsAction

    public void setActionBefore(String actionBefore) {
        this.actionBefore = actionBefore;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setMissingConcepts(String missingConcepts) {
        this.missingConcepts = missingConcepts;
    }

    /**
     * @return the vocabularyFolder
     */
    public VocabularyFolder getVocabularyFolder() {
        return vocabularyFolder;
    }

    /**
     * @param vocabularyFolder the vocabularyFolder to set
     */
    public void setVocabularyFolder(VocabularyFolder vocabularyFolder) {
        this.vocabularyFolder = vocabularyFolder;
    }
} // end of class VocabularyFolderApiActionBean
