package eionet.meta.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.DElemAttribute.ParentType;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IDataSetDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.data.DataElementsFilter;
import eionet.meta.service.data.DataElementsResult;
import eionet.util.IrrelevantAttributes;

/**
 * Data Service implementation.
 *
 * @author Juhan Voolaid
 */
@Service
@Transactional
public class DataServiceImpl implements IDataService {

    /** Data set DAO. */
    @Autowired
    private IDataSetDAO dataSetDao;

    /** Attribute DAO. */
    @Autowired
    private IAttributeDAO attributeDao;

    /** Data element DAO. */
    @Autowired
    private IDataElementDAO dataElementDao;

    /** Vocabulary concept DAO. */
    @Autowired
    private IVocabularyConceptDAO vocabularyConceptDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSet> getDataSets() throws ServiceException {
        try {
            return dataSetDao.getDataSets();
        } catch (Exception e) {
            throw new ServiceException("Failed to get data sets: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Attribute getAttributeByName(String shortName) throws ServiceException {
        try {
            return attributeDao.getAttributeByName(shortName);
        } catch (Exception e) {
            throw new ServiceException("Failed to get the attribute for '" + shortName + "': " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataElementsResult searchDataElements(DataElementsFilter filter) throws ServiceException {
        try {
            return dataElementDao.searchDataElements(filter);
        } catch (Exception e) {
            throw new ServiceException("Failed search data elements: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Attribute> getDataElementAttributes() throws ServiceException {
        try {
            return dataElementDao.getDataElementAttributes();
        } catch (Exception e) {
            throw new ServiceException("Failed to get data element attributes: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FixedValue> getFixedValues(int dataElementId) throws ServiceException {
        try {
            return dataElementDao.getFixedValues(dataElementId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get data element's fixed values: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataElement getDataElement(int id) throws ServiceException {
        try {
            return dataElementDao.getDataElement(id);
        } catch (Exception e) {
            throw new ServiceException("Failed to get data element: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataElement> getDataElementsWithFixedValues() throws ServiceException {
        try {
            DataElementsFilter commonElementsFilter = new DataElementsFilter();
            commonElementsFilter.setElementType(DataElementsFilter.COMMON_ELEMENT_TYPE);
            commonElementsFilter.setRegStatus("Released");
            commonElementsFilter.setType("CH1");
            DataElementsResult commonResult = dataElementDao.searchDataElements(commonElementsFilter);

            DataElementsFilter nonCommonElementsFilter = new DataElementsFilter();
            nonCommonElementsFilter.setElementType(DataElementsFilter.NON_COMMON_ELEMENT_TYPE);
            nonCommonElementsFilter.setRegStatus("Released");
            nonCommonElementsFilter.setType("CH1");
            DataElementsResult nonCommonResult = dataElementDao.searchDataElements(nonCommonElementsFilter);

            List<DataElement> result = new ArrayList<DataElement>();
            result.addAll(commonResult.getDataElements());
            result.addAll(nonCommonResult.getDataElements());
            return result;
        } catch (Exception e) {
            throw new ServiceException("Failed to get data elements with fixed values: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDataElementDataType(int dataElementId) throws ServiceException {
        try {
            return dataElementDao.getDataElementDataType(dataElementId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get data element's data type: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, List<String>> getDataElementSimpleAttributeValues(int dataElementId) throws ServiceException {
        try {
            return attributeDao.getAttributeValues(dataElementId, ParentType.ELEMENT.toString());
        } catch (Exception e) {
            throw new ServiceException("Failed to get data element's attributes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DataElement> getReleasedCommonDataElements() throws ServiceException {

        DataElementsFilter commonElementsFilter = new DataElementsFilter();
        commonElementsFilter.setElementType(DataElementsFilter.COMMON_ELEMENT_TYPE);
        commonElementsFilter.setRegStatus("Released");
        commonElementsFilter.setIncludeOnlyInternal(true);

        DataElementsResult result = dataElementDao.searchDataElements(commonElementsFilter);

        return result.getDataElements();

    }

    @Override
    public int getCommonElementIdByIdentifier(String identifier) throws ServiceException {
        return dataElementDao.getCommonDataElementId(identifier);
    }

    @Override
    public void setDataElementAttributes(DataElement dataElement) throws ServiceException {
        Map<String, List<String>> attributeValues = dataElementDao.getDataElementAttributeValues(dataElement.getId());

        dataElement.setElemAttributeValues(attributeValues);

    }

    @Override
    public List<DataElement> getUnreleasedCommonElements(int datasetId) throws ServiceException {

        List<DataElement> datasetElements = dataElementDao.getDataSetElements(datasetId);
        List<DataElement> unreleasedElems = new ArrayList<DataElement>();
        for (DataElement elem : datasetElements) {
            if (!elem.getStatus().equalsIgnoreCase("Released") && elem.isCommonElement() && !elem.isWorkingCopy()) {
                unreleasedElems.add(elem);
            }
        }

        return unreleasedElems;
    }

    @Override
    public List<DataElement> getVocabularySourceElements(List<Integer> vocabularyIds) {
        return dataElementDao.getVocabularySourceElements(vocabularyIds);
    }

    @Override
    public List<VocabularyConcept> getElementVocabularyConcepts(int elementId) {
        DataElement elem = dataElementDao.getDataElement(elementId);
        List<VocabularyConcept> result = new ArrayList<VocabularyConcept>();
        Integer vocabularyId = elem.getVocabularyId();
        if (vocabularyId != null) {

            List<VocabularyConcept> concepts = vocabularyConceptDao.getVocabularyConcepts(vocabularyId);

            for (VocabularyConcept concept : concepts) {
                boolean conceptDateValid = true;
                if (!elem.getAllConceptsValid()) {
                    // TODO: update - check
                    conceptDateValid = concept.getStatus().isValid();
                }
                if (conceptDateValid) {
                    result.add(concept);
                }

            }

        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.service.IDataService#switchDataElemType(int, java.lang.String)
     */
    @Override
    public void switchDataElemType(int elemId, String newType) throws ServiceException {

        // Check if the new type is at all known.
        if (!Arrays.asList("CH1", "CH2", "CH3").contains(newType)) {
            throw new ServiceException("Unknown data element type: " + newType);
        }

        // Load the data element and compare to its current type.
        DataElement dataElement = dataElementDao.getDataElement(elemId);
        if (dataElement == null) {
            throw new ServiceException("Found no data element with this id: " + elemId);
        } else {
            if (newType.equals(dataElement.getType())) {
                throw new ServiceException("Data element (id=" + elemId + ") already has this type: " + newType);
            }
        }

        // Change type in database.
        dataElementDao.changeDataElemType(elemId, newType);

        // Remove simple attributes that are considered irrelevant for the new type.
        IrrelevantAttributes instance = IrrelevantAttributes.getInstance();
        Set<String> irrelevantAttrs = instance.get(newType);
        if (CollectionUtils.isNotEmpty(irrelevantAttrs)) {
            dataElementDao.removeSimpleAttrsByShortName(elemId, irrelevantAttrs.toArray(new String[irrelevantAttrs.size()]));
        }
    }
}
