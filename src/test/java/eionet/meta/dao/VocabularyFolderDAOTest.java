package eionet.meta.dao;


import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.DBUnitHelper;
import eionet.util.Pair;
import eionet.util.Triple;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

/**
 * 
 * @author Lena KARGIOTI eka@eworx.gr
 */
@SpringApplicationContext("mock-spring-context.xml")
public class VocabularyFolderDAOTest extends UnitilsJUnit4 {
        
    @SpringBeanByType
    private IVocabularyFolderDAO vocabularyFolderDAO;

    
    @BeforeClass
    public static void setUp() throws Exception {
        DBUnitHelper.loadData("seed-vocabulary-relationships.xml");
    }
    @AfterClass
    public static void cleanUp() throws Exception {
        DBUnitHelper.deleteData("seed-vocabulary-relationships.xml");
    }
    
    @Test
    public void testGetVocabulariesRelation(){
        List<Triple<Integer,Integer,Integer>> triples = vocabularyFolderDAO.getVocabulariesRelation(1);
        
        Assert.assertThat("There are 3 relationships between Vocabulary ID:1 and other", triples.size(), CoreMatchers.is(3) );
        
        Assert.assertThat("1st triple: Vocabulary 1", triples.get(0).getLeft(), CoreMatchers.is(1));
        Assert.assertThat("1st triple: via data element with ID 6 is related to", triples.get(0).getCentral(), CoreMatchers.is(6));
        Assert.assertThat("1st triple: To Vocabulary 2", triples.get(0).getRight(), CoreMatchers.is(2));
        
        Assert.assertThat("1st triple: Vocabulary 1", triples.get(1).getLeft(), CoreMatchers.is(1));
        Assert.assertThat("1st triple: via data element with ID 5 is related to", triples.get(1).getCentral(), CoreMatchers.is(5));
        Assert.assertThat("1st triple: To Vocabulary 3", triples.get(1).getRight(), CoreMatchers.is(3));
        
        Assert.assertThat("2nd triple: Vocabulary 1", triples.get(2).getLeft(), CoreMatchers.is(1));
        Assert.assertThat("2nd triple: via data element with ID 5 is related to", triples.get(2).getCentral(), CoreMatchers.is(5));
        Assert.assertThat("2nd triple: To Vocabulary 2", triples.get(2).getRight(), CoreMatchers.is(2));
    }
    
    @Test
    public void testGetRelatedVocabularyConcepts(){
        List<Integer> concepts = vocabularyFolderDAO.getRelatedVocabularyConcepts(2, 5, 3);
        
        Assert.assertThat("There are 2 related concepts between Concept 2 and Data Element 5 and Vocabulary 3", concepts.size(), CoreMatchers.is(2) );
        
        Assert.assertThat("1st concept is with ID 8",  concepts.get(0), CoreMatchers.is(8) );
        Assert.assertThat("2nd concept is with ID 10", concepts.get(1), CoreMatchers.is(10) );
    }
    
    @Test
    public void testVocabularyConceptRelationshipsByTargetConcept(){
        int relatedVocabularyConceptId = 5;
        List<Pair<Integer,Integer>> rel = this.vocabularyFolderDAO.getVocabularyConceptRelationshipsByTargetConcept(relatedVocabularyConceptId);
        
        Assert.assertThat("The are two vocabulary relationships where the target is vocabulary concept with ID 5", rel.size(), CoreMatchers.is(2) );
        
        Assert.assertThat("1st: The related vocabulary concept is identified by ID 1", rel.get(0).getLeft(), CoreMatchers.is(1) );
        Assert.assertThat("1st: The data element which describes the relationship is identified by ID 6", rel.get(0).getRight(), CoreMatchers.is(6) );
        
        Assert.assertThat("2nd: The related vocabulary concept is identified by ID 3", rel.get(1).getLeft(), CoreMatchers.is(3) );
        Assert.assertThat("2nd: The data element which describes the relationship is identified by ID 5", rel.get(1).getRight(), CoreMatchers.is(5) );
    }
}
