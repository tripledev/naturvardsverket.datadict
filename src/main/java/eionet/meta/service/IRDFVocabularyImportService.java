package eionet.meta.service;

import eionet.meta.dao.domain.VocabularyFolder;

import java.io.Reader;
import java.util.List;

/**
 * This interface contains methods to import csv contents to bulk edit a vocabulary.
 */
public interface IRDFVocabularyImportService {

    /**
     * A Transactional method to import RDF file contents into a vocabulary folder. User can request purging data first and then
     * inserting from scracth.
     *
     * @param contents            Reader object to read file content
     * @param vocabularyFolder    Vocabulary folder under bulk edit mode
     * @param purgeVocabularyData Purge all vocabulary concepts of folder
     * @param purgePredicateBasis Purge bound elements per predicate basis
     * @return List of log messages
     * @throws ServiceException Error if input is not valid
     */
    List<String> importRdfIntoVocabulary(Reader contents, VocabularyFolder vocabularyFolder, boolean purgeVocabularyData,
            boolean purgePredicateBasis) throws ServiceException;

} // end of interface IRDFVocabularyImportService