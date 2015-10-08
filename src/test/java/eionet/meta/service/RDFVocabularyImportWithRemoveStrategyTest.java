/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.service;

import eionet.meta.dao.domain.DataElement;
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
public class RDFVocabularyImportWithRemoveStrategyTest extends VocabularyImportServiceTestBase {
    /**
     * Vocabulary folder RDF import service.
     */
    @SpringBeanByType
    private IRDFVocabularyImportService vocabularyImportService;
    
    public RDFVocabularyImportWithRemoveStrategyTest() {
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
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(6);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_strategy_test_2.rdf");
        
        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false, MissingConceptsStrategy.REMOVE);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        
        Assert.assertThat("Previous vocabulary concepts were removed.", updatedConcepts.size(), CoreMatchers.is( 1 ) );
        
        VocabularyConcept vcNew = findVocabularyConceptByIdentifier(updatedConcepts, "rdf_test_concept_4");
        Assert.assertNotNull("The Vocabulary only contains the new concept", vcNew);
    }
    
    @Test
    @Rollback    
    public void removeWhereConceptsArePredicateTargets() throws Exception{
        // get vocabulary folder
        VocabularyFolder vocabularyFolder = vocabularyService.getVocabularyFolder(TEST_VALID_VOCABULARY_ID);

        // get initial values of concepts with attributes
        List<VocabularyConcept> concepts = getVocabularyConceptsWithAttributes(vocabularyFolder);

        // get reader for RDF file
        Reader reader = getReaderFromResource("rdf_import/rdf_import_strategy_test_1.rdf");
        
        // import RDF into database
        vocabularyImportService.importRdfIntoVocabulary(reader, vocabularyFolder, false, false, MissingConceptsStrategy.REMOVE);
        Assert.assertFalse("Transaction rolled back (unexpected)", transactionManager.getTransaction(null).isRollbackOnly());

        List<VocabularyConcept> updatedConcepts = getVocabularyConceptsWithAttributes(vocabularyFolder);
        
        Assert.assertThat("Two Vocabulary Concepts identified by IDs 9 and 10 were removed, and one new concept was added", updatedConcepts.size(), CoreMatchers.is( concepts.size() - 2 + 1 ) );
        
        VocabularyConcept vc8 = findVocabularyConceptByIdentifier(updatedConcepts, "rdf_test_concept_1");
        List<List<DataElement>> vc8rels = vc8.getElementAttributes();
        Assert.assertThat("VC 8 maintains two relationships (element attributes):", vc8rels.size(), CoreMatchers.is( 2 ) );
        Assert.assertThat("1. skos:prefLabel", vc8rels.get(0).get(0).getIdentifier(), CoreMatchers.is( "skos:prefLabel" ) );
        Assert.assertThat("2. skos:broader", vc8rels.get(1).get(0).getIdentifier(), CoreMatchers.is( "skos:broader" ) );
        
        Assert.assertNull("Specifically the skos:broader relationship to the removed rdf_test_concept_2, is updated so that the Related Concept ID is null,", vc8rels.get(1).get(0).getRelatedConceptId() );
        Assert.assertThat("and the attribute value has the URI of the removed rdf_test_concept_2", vc8rels.get(1).get(0).getAttributeValue(), CoreMatchers.is( "http://127.0.0.1:8080/datadict/vocabulary/rdf_header_vs/rdf_header_vocab/rdf_test_concept_2" ) );
    }

}
