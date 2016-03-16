package eionet.meta.dao;

import java.util.List;

import eionet.meta.dao.domain.VocabularyConceptFieldsOrderElement.Property;
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

    /**
     * Saves given vocabulary's concept fields order as in the given list of pairs,
     * where the pair's left-side represents a concept property and the right side represents ID of a bound element.
     *
     * @param list
     */
    void saveOrder(List<Pair<Property, Integer>> list, int vocabularyId);
}
