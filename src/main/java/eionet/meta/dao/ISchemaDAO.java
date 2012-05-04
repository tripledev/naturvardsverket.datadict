package eionet.meta.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eionet.meta.dao.domain.Schema;
import eionet.meta.service.data.SchemaFilter;
import eionet.meta.service.data.SchemasResult;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public interface ISchemaDAO {

    /**
     *
     * @param schema
     * @return
     */
    int createSchema(Schema schema);

    /**
     *
     * @param schemaSetId
     * @return
     */
    List<Schema> listForSchemaSet(int schemaSetId);

    /**
     * Returns schemas by their ids.
     *
     * @param ids
     * @return
     */
    List<Schema> getSchemas(List<Integer> ids);

    /**
     *
     * @param schemaId
     * @return
     */
    Schema getSchema(int schemaId);

    /**
     * Returns schema ids of the given schema sets.
     *
     * @param schemaSetIds
     * @return
     */
    List<Integer> getSchemaIds(List<Integer> schemaSetIds);

    /**
     *
     * @param schemaId
     * @param schemaSetId
     * @param fileName
     * @param userName
     * @return
     */
    int copyToSchemaSet(int schemaId, int schemaSetId, String fileName, String userName);

    /**
     *
     * @param replacedId
     * @param substituteId
     */
    void replaceId(int replacedId, int substituteId);

    /**
     * Deletes schema rows with given ids.
     *
     * @param ids
     */
    void deleteSchemas(List<Integer> ids);

    /**
     * Finds schemas.
     *
     * @param searchFilter
     * @return
     */
    SchemasResult searchSchemas(SchemaFilter searchFilter);

    /**
     *
     * @param schema
     */
    void updateSchema(Schema schema);

    /**
     *
     * @param schemaId
     * @param attributes
     */
    void updateSchemaAttributes(int schemaId, Map<Integer, Set<String>> attributes);

    /**
     *
     * @param userName
     * @return
     */
    List<Schema> getWorkingCopiesOf(String userName);

    /**
     *
     * @param checkedOutCopyId
     */
    void unlock(int checkedOutCopyId);

    /**
     *
     * @param schemaId
     * @param username
     * @param comment
     */
    void checkIn(int schemaId, String username, String comment);

    /**
     * Returns true if a schema by the given filename already exists, regardless of whether it
     * is a working copy or not. Otherwise return false.
     *
     * @param filename The filename to check.
     * @return The boolean in question.
     */
    boolean exists(String filename);
}
