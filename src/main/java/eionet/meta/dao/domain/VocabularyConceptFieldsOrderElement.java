package eionet.meta.dao.domain;

import org.apache.commons.lang.StringUtils;

import eionet.util.Pair;

/**
 * A simple DTO for carrying vocabulary concept fields order element (i.e. as in VOCABULARY_CONCEPT_FIELDS_ORDER table).
 * One of {@link #getProperty()} or {@link #getBoundElement()} must always be null.
 *
 * @author Jaanus Heinlaid <jaanus.heinlaid@gmail.com>
 */
public class VocabularyConceptFieldsOrderElement {

    /** As in VOCABULARY_CONCEPT_FIELDS_ORDER.PROPERTY_NAME column and as represented by {@link Property} enum. */
    private Property property;

    /** A bound element that can be displayed on a vocabulary concept, as in VOCABULARY_CONCEPT_FIELDS_ORDER.BOUND_ELEM_ID column. */
    private DataElement boundElement;

    /**
     * Default constructor.
     *
     * @param property
     * @param boundElement
     */
    public VocabularyConceptFieldsOrderElement(Property property, DataElement boundElement) {

        if ((property == null && boundElement == null) || (property != null && boundElement != null)) {
            throw new IllegalArgumentException("One of the input constructor inputs must be null!");
        }

        this.property = property;
        this.boundElement = boundElement;
    }

    /**
     * @return the property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * @return the boundElement
     */
    public DataElement getBoundElement() {
        return boundElement;
    }

    /**
     *
     * @return
     */
    public String getLabel() {
        // In reality we don't expect both property and bound element to be null, but let's just be tolerant here to avoid any NPE.
        return property != null ? property.getLabel() : (boundElement != null ? boundElement.getIdentifier() : null);
    }

    /**
     *
     * @return
     */
    public String getId() {
        return String.format("%s|%s", property == null ? "" : property.name(), boundElement == null ? "" : String.valueOf(boundElement.getId()));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getId();
    }

    /**
     *
     * @param id
     * @return
     */
    public static Pair<Property, Integer> parseId(String id) {

        if (id == null || !id.contains("|")) {
            return null;
        }

        String propertyName = StringUtils.trimToNull(StringUtils.substringBefore(id, "|"));
        String boundElemIdStr = StringUtils.trimToNull(StringUtils.substringAfter(id, "|"));
        if ((propertyName == null && boundElemIdStr == null) || (propertyName != null && boundElemIdStr != null)) {
            throw new IllegalArgumentException("Illegal id for " + VocabularyConceptFieldsOrderElement.class.getSimpleName() + ": " + id);
        }

        Property property = propertyName == null ? null : Property.valueOf(propertyName);
        Integer boundElemId = boundElemIdStr == null ? null : Integer.valueOf(boundElemIdStr);
        return new Pair<Property, Integer>(property, boundElemId);
    }

    /**
     * An enum representing a vocabulary concept property (as in {@link VocabularyConcept}) that can be part of vocabulary concept
     * fields display order.
     *
     * @author Jaanus Heinlaid <jaanus.heinlaid@gmail.com>
     */
    public static enum Property {

        LABEL("label", "Label"), DEFINITION("definition", "Definition"), NOTATION("notation", "Notation"), STATUS("status", "Status"),
        STATUS_MODIFIED("statusModified", "Status modified"), ACCEPTED_DATE("acceptedDate", "Accepted date"), NOT_ACCEPTED_DATE("notAcceptedDate",
                "Not accepted date");

        /** The property's display label as in JSP pages where it is rendered. */
        private String label;

        /** Name of a {@link VocabularyConcept} property. */
        private String beanProperty;

        /**
         * Default constructor.
         *
         * @param label
         */
        Property(String beanProperty, String label) {
            this.beanProperty = beanProperty;
            this.label = label;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * @return the beanProperty
         */
        public String getBeanProperty() {
            return beanProperty;
        }

        /**
         *
         * @param name
         * @return
         */
        public static Property fromName(String name) {

            try {
                return Property.valueOf(name);
            } catch (NullPointerException e) {
                return null;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
