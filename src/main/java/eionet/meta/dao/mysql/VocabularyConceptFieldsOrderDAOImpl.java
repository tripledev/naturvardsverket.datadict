package eionet.meta.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import eionet.meta.dao.IVocabularyConceptFieldsOrderDAO;
import eionet.meta.dao.domain.VocabularyConceptFieldsOrderElement.Property;
import eionet.util.Pair;

/**
 * Default implementation of {@link IVocabularyConceptFieldsOrderDAO}.
 *
 * @author Jaanus Heinlaid <jaanus.heinlaid@gmail.com>
 */
@Repository
public class VocabularyConceptFieldsOrderDAOImpl extends NamedParameterJdbcDaoSupport implements IVocabularyConceptFieldsOrderDAO {

    /** */
    private static final String SQL_GET_CONCEPT_FIELDS_ORDER = ""
            + "SELECT * FROM vocabulary_concept_fields_order WHERE vocabulary_id=:vocId ORDER BY POSITION";

    /** */
    private static final String SQL_DELETE_CONCEPT_FIELDS_ORDER = "" + "DELETE FROM vocabulary_concept_fields_order WHERE vocabulary_id=:vocId";

    /** */
    private static final String SQL_INSERT_CONCEPT_FIELDS_ORDER = ""
            + "INSERT INTO vocabulary_concept_fields_order (VOCABULARY_ID, POSITION, PROPERTY_NAME, BOUND_ELEM_ID) "
            + "VALUES (:vocId, :position, :propName, :boundElemId)";

    /** */
    private static final String SQL_MOVE_CONCEPT_FIELDS_ORDER = ""
            + "UPDATE vocabulary_concept_fields_order SET vocabulary_id=:targetVocId WHERE vocabulary_id=:sourceVocId";

    /**
     * Data source.
     */
    @Autowired
    private DataSource dataSource;

    /** Initializes the needed objects after bean creation */
    @SuppressWarnings("unused")
    @PostConstruct
    private void init() {
        super.setDataSource(dataSource);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.dao.IVocabularyConceptFieldsOrderDAO#getOrderElements(int)
     */
    @Override
    public List<Pair<Property, Integer>> getOrder(int vocabularyId) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocId", vocabularyId);

        List<Pair<Property, Integer>> resultList =
                getNamedParameterJdbcTemplate().query(SQL_GET_CONCEPT_FIELDS_ORDER, params, new RowMapper<Pair<Property, Integer>>() {
                    @Override
                    public Pair<Property, Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {

                        Property property = Property.fromName(rs.getString("PROPERTY_NAME"));
                        Object boundElemId = rs.getObject("BOUND_ELEM_ID");
                        return new Pair<Property, Integer>(property, boundElemId == null ? null : Integer.valueOf(boundElemId.toString()));
                    }
                });

        return resultList;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.dao.IVocabularyConceptFieldsOrderDAO#saveOrder(java.util.List)
     */
    @Override
    public void saveOrder(List<Pair<Property, Integer>> list, int vocabularyId) {

        if (list == null || list.isEmpty()) {
            return;
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocId", vocabularyId);

        getNamedParameterJdbcTemplate().update(SQL_DELETE_CONCEPT_FIELDS_ORDER, params);

        SqlParameterSource[] paramsArray = new SqlParameterSource[list.size()];
        int i = 0;
        for (Pair<Property, Integer> pair : list) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("vocId", vocabularyId);
            map.put("position", i + 1);
            map.put("propName", pair.getLeft() == null ? null : pair.getLeft().name());
            map.put("boundElemId", pair.getRight());
            paramsArray[i++] = new MapSqlParameterSource(map);
        }
        getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_CONCEPT_FIELDS_ORDER, paramsArray);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.dao.IVocabularyConceptFieldsOrderDAO#deleteOrder(int)
     */
    @Override
    public void deleteOrder(int vocabularyId) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocId", vocabularyId);
        getNamedParameterJdbcTemplate().update(SQL_DELETE_CONCEPT_FIELDS_ORDER, params);

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.dao.IVocabularyConceptFieldsOrderDAO#moveOrder(int, int)
     */
    @Override
    public void moveOrder(int sourceVocabularyId, int targetVocabularyId) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("targetVocId", targetVocabularyId);
        params.put("sourceVocId", sourceVocabularyId);
        getNamedParameterJdbcTemplate().update(SQL_MOVE_CONCEPT_FIELDS_ORDER, params);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.dao.IVocabularyConceptFieldsOrderDAO#copyOrder(int, int)
     */
    @Override
    public void copyOrder(int sourceVocabularyId, int targetVocabularyId) {

        List<Pair<Property, Integer>> order = getOrder(sourceVocabularyId);
        saveOrder(order, targetVocabularyId);
    }
}
