package eionet.meta.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.meta.DElemAttribute.ParentType;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.ComplexAttribute;
import eionet.meta.dao.domain.ComplexAttributeField;
import eionet.util.Pair;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
@Repository
public class AttributeDAOImpl extends GeneralDAOImpl implements IAttributeDAO {

    /** */
    private static final String COPY_SIMPLE_ATTRIBUTES_SQL =
        "insert into ATTRIBUTE (DATAELEM_ID,PARENT_TYPE,M_ATTRIBUTE_ID,VALUE) "
        + "select :newParentId, PARENT_TYPE, M_ATTRIBUTE_ID, VALUE from ATTRIBUTE where DATAELEM_ID=:parentId and PARENT_TYPE=:parentType";

    /**
     * @see eionet.meta.dao.IAttributeDAO#copySimpleAttributes(int, java.lang.String, int)
     */
    @Override
    public void copySimpleAttributes(int parentId, String parentType, int newParentId) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("parentId", parentId);
        params.put("newParentId", newParentId);
        params.put("parentType", parentType);

        getNamedParameterJdbcTemplate().update(COPY_SIMPLE_ATTRIBUTES_SQL, params);
    }

    @Override
    public void deleteAttributes(List<Integer> parentIds, String parentType) {

        // Delete simple attributes

        String sql = "DELETE FROM ATTRIBUTE WHERE DATAELEM_ID IN (:ids) AND PARENT_TYPE = :parentType";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", parentIds);
        parameters.put("parentType", parentType);

        getNamedParameterJdbcTemplate().update(sql, parameters);

        // Delete complex attributes

        sql = "select ROW_ID from COMPLEX_ATTR_ROW where PARENT_ID IN (:ids) and PARENT_TYPE=:parentType";

        List<String> rowIds = getNamedParameterJdbcTemplate().query(sql, parameters, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {

                return rs.getString(1);
            }
        });

        if (rowIds != null && !rowIds.isEmpty()) {
            parameters = new HashMap<String, Object>();
            parameters.put("rowIds", rowIds);
            getNamedParameterJdbcTemplate().update("delete from COMPLEX_ATTR_ROW where ROW_ID in (:rowIds)", parameters);
            getNamedParameterJdbcTemplate().update("delete from COMPLEX_ATTR_FIELD where ROW_ID in (:rowIds)", parameters);
        }
    }

    /** */
    private static final String REPLACE_SIMPLE_ATTR_PARENT_ID_SQL = "update ATTRIBUTE set DATAELEM_ID=:substituteId "
        + "where DATAELEM_ID=:replacedId and PARENT_TYPE=:parentType";
    /** */
    private static final String REPLACE_COMPLEX_ATTR_PARENT_ID_SQL = "update COMPLEX_ATTR_ROW set PARENT_ID=:substituteId "
        + "where PARENT_ID=:replacedId and PARENT_TYPE=:parentType";
    /** */
    private static final String REPLACE_COMPLEX_ATTR_ROW_ID_SQL = "update COMPLEX_ATTR_ROW set ROW_ID=:substituteId "
        + "where ROW_ID=:replacedId";
    /** */
    private static final String REPLACE_COMPLEX_ATTR_FIELD_ROW_ID_SQL = "update COMPLEX_ATTR_FIELD set ROW_ID=:substituteId "
        + "where ROW_ID=:replacedId";

    /**
     * @see eionet.meta.dao.IAttributeDAO#replaceParentId(int, int, eionet.meta.DElemAttribute.ParentType)
     */
    @Override
    public void replaceParentId(int replacedId, final int substituteId, final ParentType parentType) {

        Map<String, Object> prms = new HashMap<String, Object>();
        prms.put("replacedId", replacedId);
        prms.put("substituteId", substituteId);
        prms.put("parentType", parentType.toString());

        getNamedParameterJdbcTemplate().update(REPLACE_SIMPLE_ATTR_PARENT_ID_SQL, prms);

        String sql =
            "select M_COMPLEX_ATTR_ID, POSITION, ROW_ID from COMPLEX_ATTR_ROW "
            + "where PARENT_ID=:parentId and PARENT_TYPE=:parentType order by ROW_ID";

        prms = new HashMap<String, Object>();
        prms.put("parentId", replacedId);
        prms.put("parentType", parentType.toString());

        List<Pair<String, String>> pairs = getNamedParameterJdbcTemplate().query(sql, prms, new RowMapper<Pair<String, String>>() {
            @Override
            public Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {

                String oldRowId = rs.getString("ROW_ID");
                String newRowId = substituteId + parentType.toString() + rs.getString("M_COMPLEX_ATTR_ID") + rs.getInt("POSITION");
                return new Pair<String, String>(oldRowId, newRowId);
            }
        });

        prms = new HashMap<String, Object>();
        prms.put("replacedId", replacedId);
        prms.put("substituteId", substituteId);
        prms.put("parentType", parentType.toString());
        getNamedParameterJdbcTemplate().update(REPLACE_COMPLEX_ATTR_PARENT_ID_SQL, prms);

        for (Pair<String, String> pair : pairs) {

            prms = new HashMap<String, Object>();
            prms.put("replacedId", pair.getLeft());
            prms.put("substituteId", pair.getRight());
            getNamedParameterJdbcTemplate().update(REPLACE_COMPLEX_ATTR_ROW_ID_SQL, prms);
            getNamedParameterJdbcTemplate().update(REPLACE_COMPLEX_ATTR_FIELD_ROW_ID_SQL, prms);
        }
    }

    /**
     * @see eionet.meta.dao.IAttributeDAO#getAttributes(eionet.meta.DElemAttribute.ParentType, java.lang.String)
     */
    @Override
    public List<Attribute> getAttributes(DElemAttribute.ParentType parentType, String attributeType) throws DAOException {
        List<Attribute> result = new ArrayList<Attribute>();
        DDSearchEngine searchEngine = new DDSearchEngine(getConnection());

        LinkedHashMap<Integer, DElemAttribute> attributes = searchEngine.getObjectAttributes(0, parentType, attributeType);

        for (DElemAttribute dea : attributes.values()) {
            Attribute a = new Attribute();
            a.setId(Integer.parseInt(dea.getID()));
            a.setName(dea.getName());
            a.setShortName(dea.getShortName());
            result.add(a);
        }

        return result;
    }

    /**
     * @see eionet.meta.dao.IAttributeDAO#getAttributeValues(int, eionet.meta.DElemAttribute.ParentType)
     */
    @Override
    public Map<String, List<String>> getAttributeValues(int parentId, String parentType) {

        String sql =
            "select SHORT_NAME, VALUE from ATTRIBUTE, M_ATTRIBUTE"
            + " where DATAELEM_ID=:parentId and PARENT_TYPE=:parentType and ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID"
            + " order by SHORT_NAME, VALUE";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("parentId", parentId);
        params.put("parentType", parentType);

        final HashMap<String, List<String>> resultMap = new HashMap<String, List<String>>();
        getNamedParameterJdbcTemplate().query(sql, params, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String shortName = rs.getString("SHORT_NAME");
                String value = rs.getString("VALUE");
                List<String> values = resultMap.get(shortName);
                if (values == null) {
                    values = new ArrayList<String>();
                    resultMap.put(shortName, values);
                }
                values.add(value);
            }
        });

        return resultMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Attribute getAttributeByName(String shortName) {
        String sql = "select * from M_ATTRIBUTE where SHORT_NAME=:shortName";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("shortName", shortName);

        Attribute result = getNamedParameterJdbcTemplate().queryForObject(sql, params, new RowMapper<Attribute>() {

            @Override
            public Attribute mapRow(ResultSet rs, int rowNum) throws SQLException {
                Attribute attribute = new Attribute();
                attribute.setId(rs.getInt("M_ATTRIBUTE_ID"));
                attribute.setName(rs.getString("NAME"));
                attribute.setShortName(rs.getString("SHORT_NAME"));
                return attribute;
            }

        });
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComplexAttribute getComplexAttributeByName(String complexAttrName) {

        String sql =
            "select * from M_COMPLEX_ATTR as a, M_COMPLEX_ATTR_FIELD as f "
            + "where a.M_COMPLEX_ATTR_ID = f.M_COMPLEX_ATTR_ID and a.NAME= :attrName";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("attrName", complexAttrName);

        ComplexAttribute complexAttribute =
            getNamedParameterJdbcTemplate().query(sql, params, new ResultSetExtractor<ComplexAttribute>() {

                @Override
                public ComplexAttribute extractData(ResultSet rs) throws DataAccessException, SQLException {
                    ComplexAttribute complexAttribute = null;
                    while (rs.next()) {
                        if (complexAttribute == null) {
                            complexAttribute = new ComplexAttribute(rs.getInt("a.M_COMPLEX_ATTR_ID"), rs.getString("a.NAME"));
                        }
                        ComplexAttributeField field =
                            new ComplexAttributeField(rs.getInt("f.M_COMPLEX_ATTR_FIELD_ID"), rs.getString("f.NAME"));
                        complexAttribute.addField(field);
                    }
                    return complexAttribute;
                }
            });
        return complexAttribute;
    }

    /*
     * (non-Javadoc)
     * @see eionet.meta.dao.IAttributeDAO#copyComplexAttributes(int, java.lang.String, int)
     */
    @Override
    public void copyComplexAttributes(int parentId, final String parentType, final int newParentId) {

        String sqlQuery =
            "select M_COMPLEX_ATTR_ID, COMPLEX_ATTR_ROW.ROW_ID, POSITION, HARV_ATTR_ID, M_COMPLEX_ATTR_FIELD_ID, VALUE "
            + "from COMPLEX_ATTR_ROW, COMPLEX_ATTR_FIELD where PARENT_ID=:parentId and PARENT_TYPE=:parentType "
            + "and COMPLEX_ATTR_ROW.ROW_ID=COMPLEX_ATTR_FIELD.ROW_ID "
            + "order by COMPLEX_ATTR_ROW.ROW_ID, M_COMPLEX_ATTR_FIELD_ID";

        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("parentId", parentId);
        queryParams.put("parentType", parentType);

        final String sqlInsertRow =
            "insert into COMPLEX_ATTR_ROW " + "(PARENT_ID, PARENT_TYPE, M_COMPLEX_ATTR_ID, POSITION, HARV_ATTR_ID, ROW_ID) "
            + "values (:parentId, :parentType, :attrId, :position, :harvAttrId, :rowId)";

        final Map<String, Object> insertRowParams = new HashMap<String, Object>();
        insertRowParams.put("parentId", newParentId);
        insertRowParams.put("parentType", parentType);

        final String sqlInsertField =
            "insert into COMPLEX_ATTR_FIELD (ROW_ID, M_COMPLEX_ATTR_FIELD_ID, VALUE) " + "values (:rowId, :fieldId, :value)";

        final Map<String, Object> insertFieldParams = new HashMap<String, Object>();

        getNamedParameterJdbcTemplate().query(sqlQuery, queryParams, new RowCallbackHandler() {

            String previousRowId = "";
            String newRowId = null;

            @Override
            public void processRow(ResultSet rs) throws SQLException {

                int attrId = rs.getInt("M_COMPLEX_ATTR_ID");
                String rowId = rs.getString("COMPLEX_ATTR_ROW.ROW_ID");
                int fieldId = rs.getInt("M_COMPLEX_ATTR_FIELD_ID");
                String value = rs.getString("VALUE");
                int position = rs.getInt("POSITION");
                int harvAttrId = rs.getInt("HARV_ATTR_ID");

                if (!rowId.equals(previousRowId)) {

                    insertRowParams.put("attrId", attrId);
                    insertRowParams.put("position", position);
                    insertRowParams.put("harvAttrId", harvAttrId);

                    String md5Input = newParentId + parentType + attrId + position;
                    newRowId = DigestUtils.md5Hex(md5Input);
                    insertRowParams.put("rowId", newRowId);

                    getNamedParameterJdbcTemplate().update(sqlInsertRow, insertRowParams);
                    previousRowId = rowId;
                }

                insertFieldParams.put("rowId", newRowId);
                insertFieldParams.put("fieldId", fieldId);
                insertFieldParams.put("value", value);

                getNamedParameterJdbcTemplate().update(sqlInsertField, insertFieldParams);
            }
        });
    }
}
