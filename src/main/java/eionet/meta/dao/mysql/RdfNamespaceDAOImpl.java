package eionet.meta.dao.mysql;

import eionet.meta.DataElement;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.IRdfNamespaceDAO;
import eionet.meta.dao.domain.RdfNamespace;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IRDFNamespaceDAO implementation in mysql.
 *
 * @author Kaido Laine
 */
@Repository
public class RdfNamespaceDAOImpl extends GeneralDAOImpl implements IRdfNamespaceDAO {

    @Override
    public boolean namespaceExists(String namespaceId) throws DAOException {
        String sql = "select * from T_RDF_NAMESPACE where NAME_PREFIX = :nsPrefix ";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("nsPrefix", namespaceId.toLowerCase());

        List<String> resultList = getNamedParameterJdbcTemplate().query(sql, parameters, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("NAME_PREFIX");
            }
        });

        return resultList.size() != 0;
    }

    @Override
    public RdfNamespace getNamespace(String namespaceId) throws DAOException {
        String sql = "select * from T_RDF_NAMESPACE where NAME_PREFIX = :nsPrefix ";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("nsPrefix", namespaceId);

        List<RdfNamespace> resultList = getNamedParameterJdbcTemplate().query(sql, parameters, new RowMapper<RdfNamespace>() {
            @Override
            public RdfNamespace mapRow(ResultSet rs, int rowNum) throws SQLException {
                RdfNamespace ns = new RdfNamespace();
                ns.setId(Integer.parseInt(rs.getString("id")));
                ns.setPrefix(rs.getString("NAME_PREFIX"));
                ns.setUri(rs.getString("URI"));

                return ns;
            }
        });

        return resultList.size() > 0 ? resultList.get(0) : null;
    }

    @Override
    public List<RdfNamespace> getElementExternalNamespaces(List<DataElement> elements) throws DAOException {
        ArrayList<RdfNamespace> nameSpaces = new ArrayList<RdfNamespace>();

        for (DataElement elem : elements) {
            if (elem.isExternalSchema()) {
                RdfNamespace ns = getNamespace(elem.getNameSpacePrefix());
                if (!nameSpaces.contains(ns)) {
                    nameSpaces.add(ns);
                }
            }
        }
        return nameSpaces;
    }

}
