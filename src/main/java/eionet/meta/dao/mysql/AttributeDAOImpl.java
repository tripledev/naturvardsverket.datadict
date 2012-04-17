package eionet.meta.dao.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import eionet.meta.DElemAttribute.ParentType;
import eionet.meta.dao.IAttributeDAO;

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
        String sql = "DELETE FROM ATTRIBUTE WHERE DATAELEM_ID IN (:ids) AND PARENT_TYPE = :parentType";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", parentIds);
        parameters.put("parentType", parentType);

        getNamedParameterJdbcTemplate().update(sql, parameters);
    }

    /** */
    private static final String REPLACE_PARENT_ID_SQL = "update ATTRIBUTE set DATAELEM_ID=:substituteId "
        + "where DATAELEM_ID=:replacedId and PARENT_TYPE=:parentType";

    /**
     * @see eionet.meta.dao.IAttributeDAO#replaceParentId(int, int, eionet.meta.DElemAttribute.ParentType)
     */
    @Override
    public void replaceParentId(int replacedId, int substituteId, ParentType parentType) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("replacedId", replacedId);
        params.put("substituteId", substituteId);
        params.put("parentType", parentType.toString());

        getNamedParameterJdbcTemplate().update(REPLACE_PARENT_ID_SQL, params);
    }

    /**
     *
     * @param replacedToSubstituteIds
     * @param parentType
     */
    private void replaceParentIds(Map<Integer, Integer> replacedToSubstituteIds, ParentType parentType) {

        if (replacedToSubstituteIds == null || replacedToSubstituteIds.isEmpty()) {
            return;
        }

        ArrayList<Map> valueMaps = new ArrayList<Map>();
        for (Map.Entry<Integer, Integer> entry : replacedToSubstituteIds.entrySet()) {

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("replacedId", entry.getKey());
            params.put("substituteId", entry.getValue());
            params.put("parentType", parentType.toString());
            valueMaps.add(params);
        }

        SqlParameterSource[] batchArgs = SqlParameterSourceUtils.createBatch(valueMaps.toArray(new Map[valueMaps.size()]));
        getNamedParameterJdbcTemplate().batchUpdate(REPLACE_PARENT_ID_SQL, batchArgs);
    }
}
