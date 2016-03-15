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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import eionet.meta.dao.IVocabularyConceptFieldsOrderDAO;
import eionet.util.Pair;

/**
 * Default implementation of {@link IVocabularyConceptFieldsOrderDAO}.
 *
 * @author Jaanus Heinlaid <jaanus.heinlaid@gmail.com>
 */
@Repository
public class VocabularyConceptFieldsOrderDAOImpl extends NamedParameterJdbcDaoSupport implements IVocabularyConceptFieldsOrderDAO {

    /** */
    private static final String SQL_GET_VOCABULARY_CONCEPT_FIELDS_ORDER = ""
            + "SELECT * FROM vocabulary_concept_fields_order WHERE vocabulary_id=:vocId ORDER BY POSITION";

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
    public List<Pair<String, Integer>> getOrderElements(int vocabularyId) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocId", vocabularyId);

        List<Pair<String, Integer>> resultList =
                getNamedParameterJdbcTemplate().query(SQL_GET_VOCABULARY_CONCEPT_FIELDS_ORDER, params, new RowMapper<Pair<String, Integer>>() {
                    @Override
                    public Pair<String, Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {

                        return new Pair<String, Integer>(rs.getString("PROPERTY_NAME"), rs.getInt("BOUND_ELEM_ID"));
                    }
                });

        return resultList;
    }
}
