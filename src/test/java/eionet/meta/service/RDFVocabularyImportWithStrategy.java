/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.service;

import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import static eionet.meta.service.VocabularyImportServiceTestBase.TEST_VALID_VOCABULARY_ID;
import eionet.web.action.MissingConceptsStrategy;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.test.annotation.Rollback;
import org.unitils.spring.annotation.SpringBeanByType;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class RDFVocabularyImportWithStrategy extends VocabularyImportServiceTestBase {
    /**
     * Vocabulary folder RDF import service.
     */
    @SpringBeanByType
    private IRDFVocabularyImportService vocabularyImportService;
    
    public RDFVocabularyImportWithStrategy() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        DBUnitHelper.loadData("seed-emptydb.xml");
        DBUnitHelper.loadData("rdf_import/seed-vocabularyrdf-import.xml");
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        DBUnitHelper.deleteData("rdf_import/seed-vocabularyrdf-import.xml");
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * {@inheritDoc }
     * @return 
     * @throws java.lang.Exception
     */
    @Override
    protected Reader getReaderFromResource(String resourceLoc) throws Exception{
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceLoc);
        InputStreamReader reader = new InputStreamReader(is);
        return reader;
    }
    
    @Test
    @Rollback    
    public void remove() throws Exception{
//        // get vocabulary folder
//        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);
//
//        // get reader for RDF file
//        Reader reader = getReaderFromResource("rdf_import/rdf_import_strategy_test_1.rdf");
//        // import RDF into database
//        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false, MissingConceptsStrategy.REMOVE);
//        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());
//
//        // get initial values of concepts with attributes
//        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
//        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
//        
//        Assert.assertThat("Only two concepts remain in the vocabulary", updatedConcepts.size(), CoreMatchers.is(2) );
//        
        //TODO: How to handle the case where the removed concepts are predicate targets through vocabulary_concept_element?
    }
    
    @Test
    @Rollback    
    public void invalid() throws Exception{
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_strategy_test_1.rdf");
        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false, MissingConceptsStrategy.UPDATE_TO_INVALID);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        
        Assert.assertThat("No vocabulary concept was removed, and one new concept was added", updatedConcepts.size(), CoreMatchers.is( concepts.size()+1 ) );
        
    }
}
