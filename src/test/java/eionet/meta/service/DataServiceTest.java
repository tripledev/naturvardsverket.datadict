package eionet.meta.service;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eionet.meta.dao.domain.DataElement;

/**
 * DataService tests.
 *
 * @author Kaido Laine
 */
@SpringApplicationContext("mock-spring-context.xml")
public class DataServiceTest extends UnitilsJUnit4  {

    /**
     * Service instance.
     */
    @SpringBeanByType
    IDataService dataService;

    /**
     * Load seed data file.
     * @throws Exception if loading fails
     */
    @BeforeClass
    public static void loadData() throws Exception {
        DBUnitHelper.loadData("seed-dataelements.xml");
    }

    /**
     * Delete helper data.
     * @throws Exception if delete fails
     */
    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("seed-dataelements.xml");
    }

    /**
     * Test on getting common data elements.
     * @throws Exception if fail
     */
    @Test
    public void testGetCommonElements() throws Exception {
        List<DataElement> elements = dataService.getReleasedCommonDataElements();

        Assert.assertTrue(elements.size() == 2);

    }

    /**
     * test set element attribute values.
     * @throws Exception if fail
     */
    @Test
    public void testSetElementAttributes() throws Exception {
        DataElement elem1 = dataService.getDataElement(1);
        dataService.setDataElementAttributes(elem1);

        DataElement elem2 = dataService.getDataElement(2);
        dataService.setDataElementAttributes(elem2);

        DataElement elem3 = dataService.getDataElement(3);
        dataService.setDataElementAttributes(elem3);

        Assert.assertTrue(elem1.getElemAttributeValues().size() == 2);
        Assert.assertEquals(elem1.getName(), "Common element");

        Assert.assertTrue(elem2.getElemAttributeValues().size() == 0);
        Assert.assertTrue(elem3.getElemAttributeValues().size() == 1);
        Assert.assertEquals(elem3.getElemAttributeValues().get("Definition").get(0), "Third definition");


    }
}
