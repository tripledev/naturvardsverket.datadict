package eionet.meta.dao;

import java.util.List;

import eionet.util.Pair;

/**
 * A DAO for operating with vocabulary fields display order.
 *
 * @author Jaanus Heinlaid <jaanus.heinlaid@gmail.com>
 */
public interface IVocabularyConceptFieldsOrderDAO {

    /**
     * Returns the given vocabulary's concept fields order.
     *
     * @param vocabularyId
     * @return
     */
    List<Pair<String, Integer>> getOrderElements(int vocabularyId);
}
