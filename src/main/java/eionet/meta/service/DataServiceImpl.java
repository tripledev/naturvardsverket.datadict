package eionet.meta.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IDataSetDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.data.DataElementsFilter;
import eionet.meta.service.data.DataElementsResult;

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
}
