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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.meta.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.DElemAttribute;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IDataSetDAO;
import eionet.meta.dao.ITableDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.ComplexAttribute;
import eionet.meta.dao.domain.ComplexAttributeField;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.DataSetTable;
import eionet.meta.dao.domain.DatasetRegStatus;
import eionet.meta.service.data.DatasetFilter;
import eionet.meta.service.data.TableFilter;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * Table service.
 *
 * @author Juhan Voolaid
 */
@Service
@Transactional
public class TableServiceImpl implements ITableService {

    /** Table DAO. */
    @Autowired
    private ITableDAO tableDAO;

    /** Dataset DAO. */
    @Autowired
    private IDataSetDAO datasetDAO;

    /** The DAO for operations with attributes */
    @Autowired
    private IAttributeDAO attributeDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSetTable> searchTables(TableFilter tableFilter) throws ServiceException {
        try {
            return tableDAO.searchTables(tableFilter);
        } catch (Exception e) {
            throw new ServiceException("Failed to search tables: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Attribute> getTableAttributes() throws ServiceException {
        try {
            return attributeDAO.getAttributes(DElemAttribute.ParentType.TABLE, DElemAttribute.TYPE_SIMPLE);
        } catch (Exception e) {
            throw new ServiceException("Failed to get table attributes: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getNameAttribute(int tableId) throws ServiceException {
        try {
            Map<String, List<String>> result = attributeDAO.getAttributeValues(tableId, "T");
            return result.get("Name");
        } catch (Exception e) {
            throw new ServiceException("Failed to get table name attributes: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSetTable> getTablesForObligation(String obligationId, boolean releasedOnly) throws ServiceException {

        List<DataSet> datasets = new ArrayList<DataSet>();
        try {
            DatasetFilter datasetFilter = new DatasetFilter();
            if (releasedOnly) {
                datasetFilter.setRegStatuses(Arrays.asList(DatasetRegStatus.RELEASED.toString()));
            } else {
                datasetFilter.setRegStatuses(Arrays.asList(DatasetRegStatus.RELEASED.toString(),
                        DatasetRegStatus.RECORDED.toString()));
            }
            // Search datasets by ROD numeric IDs from DST2ROD table
            if (obligationId.startsWith(Props.getRequiredProperty(PropsIF.OUTSERV_ROD_OBLIG_URL))) {
                int rodId =
                    NumberUtils.toInt(StringUtils.substringAfter(obligationId,
                            Props.getRequiredProperty(PropsIF.OUTSERV_ROD_OBLIG_URL)));
                if (rodId > 0) {
                    List<Integer> rodIds = new ArrayList<Integer>();
                    rodIds.add(Integer.valueOf(rodId));
                    datasetFilter.setRodIds(rodIds);
                }
                // search datasets
                List<DataSet> datasets1 = datasetDAO.searchDatasets(datasetFilter);
                datasets.addAll(datasets1);
                datasetFilter.setRodIds(null);
            }

            // Search datasets by ROD URLs stored in complex attributes
            ComplexAttribute rodAttr = attributeDAO.getComplexAttributeByName("ROD");
            ComplexAttributeField field = rodAttr.getField("url");
            if (field != null) {
                field.setValue(obligationId);
                field.setExactMatchInSearch(true);
            }
            List<ComplexAttribute> complexAttributes = new ArrayList<ComplexAttribute>();
            complexAttributes.add(rodAttr);
            datasetFilter.setComplexAttributes(complexAttributes);

            // search datasets
            List<DataSet> datasets2 = datasetDAO.searchDatasets(datasetFilter);
            datasets.addAll(datasets2);

            if (datasets != null && datasets.size() > 0) {
                return tableDAO.listForDatasets(datasets);
            } else {
                return new ArrayList<DataSetTable>();
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to search tables for obligation: " + e.getMessage(), e);
        }
    }
}
