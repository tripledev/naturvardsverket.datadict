/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.service;

import eionet.meta.dao.domain.StandardGenericStatus;
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
public class RDFVocabularyImportWithStrategyTest extends VocabularyImportServiceTestBase {
    /**
     * Vocabulary folder RDF import service.
     */
    @SpringBeanByType
    private IRDFVocabularyImportService vocabularyImportService;
    
    public RDFVocabularyImportWithStrategyTest() {
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
    
//    @Test
//    @Rollback    
//    public void remove() throws Exception{
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
//    }
    
    @Test
    @Rollback    
    public void _a_invalidNoPurge() throws Exception{
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);
        
        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        
        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_strategy_test_1.rdf");
        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false, MissingConceptsStrategy.UPDATE_TO_INVALID);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());
        
        List<VocabularyConcept> updatedValidConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertThat("The valid concepts are the two imported", updatedValidConcepts.size(), CoreMatchers.is( 2 ) );
        VocabularyConcept vcNew = findVocabularyConceptByIdentifier(updatedValidConcepts, "rdf_test_concept_4");
        Assert.assertNotNull("The concept 'rdf_test_concept_4' is added", vcNew );
        
        List<VocabularyConcept> updatedConcepts = getAllVocabularyConcepts(vocabularyFolder);
        
        Assert.assertThat("No vocabulary concept was removed, and one new concept was added", updatedConcepts.size(), CoreMatchers.is( concepts.size()+1 ) );
        
        VocabularyConcept vc9 = findVocabularyConceptByIdentifier(updatedConcepts, "rdf_test_concept_2");
        Assert.assertThat("The status of missing concept 'rdf_test_concept_2' is set to 'Invalid'", vc9.getStatus(), CoreMatchers.is( StandardGenericStatus.INVALID ));        
    }
    
    @Test
    @Rollback    
    public void _b_deprecatedPurgeAll() throws Exception{
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);
        
        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_strategy_test_1.rdf");
        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, true, false, MissingConceptsStrategy.UPDATE_TO_DEPRECATED);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        List<VocabularyConcept> updatedValidConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        Assert.assertThat("The valid concepts are the two imported", updatedValidConcepts.size(), CoreMatchers.is( 2 ) );
        VocabularyConcept vcNew = findVocabularyConceptByIdentifier(updatedValidConcepts, "rdf_test_concept_4");
        Assert.assertNotNull("The concept 'rdf_test_concept_4' is added", vcNew );
        
        List<VocabularyConcept> updatedConcepts = getAllVocabularyConcepts(vocabularyFolder);
        
        Assert.assertThat("All vocabulary concepts were removed, so only the two imported ones are added", updatedConcepts.size(), CoreMatchers.is( 2 ) );  
        
        VocabularyConcept vc = findVocabularyConceptByIdentifier(updatedConcepts, "rdf_test_concept_2");
        Assert.assertNull("The concept 'rdf_test_concept_2' is removed", vc );   
    }
    
    @Test
    @Rollback    
    public void _c_deprecatedSupersededPurgePredicate() throws Exception{
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);
        
        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        
        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_strategy_test_1.rdf");
        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, true, MissingConceptsStrategy.UPDATE_TO_DEPRECATED_SUPERSEDED);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        
        List<VocabularyConcept> updatedConcepts = getAllVocabularyConcepts(vocabularyFolder);
        Assert.assertThat("No vocabulary concept was removed, and one new concept was added", updatedConcepts.size(), CoreMatchers.is( concepts.size()+1 ) );
        
        VocabularyConcept vcNew = findVocabularyConceptByIdentifier(updatedConcepts, "rdf_test_concept_4");
        Assert.assertNotNull("The concept 'rdf_test_concept_4' is added", vcNew ); 
        
        VocabularyConcept vc9 = findVocabularyConceptByIdentifier(updatedConcepts, "rdf_test_concept_2");
        Assert.assertThat("The status of missing concept 'rdf_test_concept_2' is set to 'Deprecated - Superseded'", vc9.getStatus(), CoreMatchers.is( StandardGenericStatus.DEPRECATED_SUPERSEDED ));    
        
        VocabularyConcept vc10 = findVocabularyConceptByIdentifier(updatedConcepts, "rdf_test_concept_3");
        Assert.assertThat("The status of missing concept 'rdf_test_concept_3' is set to 'Deprecated - Superseded'", vc10.getStatus(), CoreMatchers.is( StandardGenericStatus.DEPRECATED_SUPERSEDED ));    
    
    }
}
