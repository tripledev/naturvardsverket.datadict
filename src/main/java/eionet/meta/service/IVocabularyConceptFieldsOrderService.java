package eionet.meta.service;

import java.util.List;

import eionet.meta.dao.domain.VocabularyConceptFieldsOrderElement;

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
    List<VocabularyConceptFieldsOrderElement> getOrderElements(int vocabularyId);
}
