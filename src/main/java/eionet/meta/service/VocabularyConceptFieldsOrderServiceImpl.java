package eionet.meta.service;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IVocabularyConceptFieldsOrderDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConceptFieldsOrderElement;
import eionet.meta.dao.domain.VocabularyConceptFieldsOrderElement.Property;
import eionet.util.Pair;

/**
 * Default implementation of {@link IVocabularyConceptFieldsOrderService}.
 *
 * @author Jaanus Heinlaid <jaanus.heinlaid@gmail.com>
 */
@Service
@Transactional
public class VocabularyConceptFieldsOrderServiceImpl implements IVocabularyConceptFieldsOrderService {

    /** */
    private static final Logger LOGGER = Logger.getLogger(VocabularyConceptFieldsOrderServiceImpl.class);

    /** */
    @Autowired
    private IVocabularyConceptFieldsOrderDAO conceptFieldsOrderDAO;

    /** */
    @Autowired
    private IDataElementDAO dataElementDAO;

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.service.IVocabularyConceptFieldsOrderService#getOrderElements(int)
     */
    @Override
    public List<VocabularyConceptFieldsOrderElement> getOrder(int vocabularyId) {

        SortedMap<Integer, VocabularyConceptFieldsOrderElement> sortedMap = new TreeMap<Integer, VocabularyConceptFieldsOrderElement>();

        List<VocabularyConceptFieldsOrderElement> defaultOrder = getDefaultOrder(vocabularyId);
        List<Pair<Property, Integer>> orderPairs = conceptFieldsOrderDAO.getOrder(vocabularyId);

        int i = 900;
        for (VocabularyConceptFieldsOrderElement orderElement : defaultOrder) {

            Property property = orderElement.getProperty();

            DataElement boundElement = orderElement.getBoundElement();
            Integer boundElemId = boundElement == null ? null : boundElement.getId();

            int position = ++i;
            for (int j = 0; j < orderPairs.size(); j++) {

                Pair<Property, Integer> orderPair = orderPairs.get(j);

                if ((property != null && property.equals(orderPair.getLeft()))
                        || (boundElemId != null && boundElemId.equals(orderPair.getRight()))) {
                    position = j + 1;
                    break;
                }
            }

            sortedMap.put(position, orderElement);
        }

        List<VocabularyConceptFieldsOrderElement> resultList = new ArrayList<VocabularyConceptFieldsOrderElement>(sortedMap.values());
        return resultList;
    }

    /**
     *
     * @param vocabularyId
     * @return
     */
    private List<VocabularyConceptFieldsOrderElement> getDefaultOrder(int vocabularyId) {

        List<VocabularyConceptFieldsOrderElement> resultList = new ArrayList<VocabularyConceptFieldsOrderElement>();

        Property[] properties = VocabularyConceptFieldsOrderElement.Property.values();
        for (Property property : properties) {
            resultList.add(new VocabularyConceptFieldsOrderElement(property, null));
        }

        List<DataElement> boundElements = dataElementDAO.getVocabularyDataElements(vocabularyId);
        for (DataElement boundElement : boundElements) {
            resultList.add(new VocabularyConceptFieldsOrderElement(null, boundElement));
        }

        return resultList;
    }

    /*
     * (non-Javadoc)
     * @see eionet.meta.service.IVocabularyConceptFieldsOrderService#saveOrder(java.util.ArrayList)
     */
    @Override
    @Transactional
    public void saveOrder(ArrayList<Pair<Property, Integer>> list, int vocabularyId) {
        conceptFieldsOrderDAO.saveOrder(list, vocabularyId);
    }
}