package eionet.meta.service;

import java.util.ArrayList;
import java.util.List;

import eionet.meta.dao.domain.VocabularyConceptFieldsOrderElement;
import eionet.meta.dao.domain.VocabularyConceptFieldsOrderElement.Property;
import eionet.util.Pair;

/**
 * A service for operating with vocabulary fields display order.
 *
 * @author Jaanus Heinlaid <jaanus.heinlaid@gmail.com>
 */
public interface IVocabularyConceptFieldsOrderService {

    /**
     * Returns the given vocabulary's concept fields order.
     *
     * @param vocabularyId
     * @return
     */
    List<VocabularyConceptFieldsOrderElement> getOrder(int vocabularyId);

    /**
     * Saves given vocabulary's concept fields order as in the given list of pairs,
     * where the pair's left-side represents a concept property and the right side represents ID of a bound element.
     *
     * @param list
     */
    void saveOrder(ArrayList<Pair<Property, Integer>> list, int vocabularyId);
}
