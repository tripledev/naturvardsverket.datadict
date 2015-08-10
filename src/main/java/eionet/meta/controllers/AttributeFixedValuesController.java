package eionet.meta.controllers;

import eionet.meta.application.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.application.errors.fixedvalues.NotAFixedValueOwnerException;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SimpleAttribute;
import java.util.List;

/**
 * Controller containing business logic related to viewing/editing fixed values
 * for simple attributes.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface AttributeFixedValuesController {
   
    /**
     * Fetches info about an specific simple attribute that accepts fixed values.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerAttributeId the attribute id in {@link String} form.
     * 
     * @return a {@link SimpleAttribute} containing the requested info.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws MalformedIdentifierException if the attribute id cannot be parsed.
     * @throws FixedValueOwnerNotFoundException if the attribute cannot be found.
     * @throws NotAFixedValueOwnerException if the attribute cannot be associated to fixed values.
     */
    SimpleAttribute getOwnerAttribute(AppContextProvider contextProvider, String ownerAttributeId)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException;
    
    /**
     * Fetches all the info required about a fixed value and its respective
     * owner. The results are packed into a {@link CompoundDataObject} instance,
     * given the following specification:
     * <ul>
     * <li>{@value #PROPERTY_OWNER_ATTRIBUTE}: the {@link SimpleAttribute} instance</li>
     * <li>{@value #PROPERTY_FIXED_VALUE}: the {@link FixedValue} instance</li>
     * </ul>
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerAttributeId the attribute id in {@link String} format.
     * @param fixedValue the requested value.
     * 
     * @return a {@link CompoundDataObject} instance containing info about the 
     * fixed value, and the owner attribute.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws MalformedIdentifierException if the attribute id cannot be parsed.
     * @throws FixedValueOwnerNotFoundException if the attribute cannot be found.
     * @throws NotAFixedValueOwnerException if the attribute cannot be associated to fixed values.
     * @throws FixedValueNotFoundException if the requested fixed value is not found.
     */
    CompoundDataObject getSingleValueModel(AppContextProvider contextProvider, String ownerAttributeId, String fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException, FixedValueNotFoundException;
    
    /**
     * Fetches all the info required about the fixed values of a specific owner.
     * The results are packed into a {@link CompoundDataObject} instance, given 
     * the following specification:
     * <ul>
     * <li>{@value #PROPERTY_OWNER_ATTRIBUTE}: a {@link SimpleAttribute} instance</li>
     * <li>{@value #PROPERTY_FIXED_VALUES}: a {@link List} of {@link FixedValue} instances</li>
     * </ul>
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerAttributeId the attribute id in {@link String} format.
     * 
     * @return a {@link CompoundDataObject} instance containing info about the 
     * fixed values, and the owner attribute.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws MalformedIdentifierException if the attribute id cannot be parsed.
     * @throws FixedValueOwnerNotFoundException if the attribute cannot be found.
     * @throws NotAFixedValueOwnerException if the attribute cannot be associated to fixed values.
     */
    CompoundDataObject getAllValuesModel(AppContextProvider contextProvider, String ownerAttributeId)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException;
    
    /**
     * Persists the modified data of the fixed value of a specified owner. 
     * Performs an update operation if the value exists; otherwise it will 
     * perform a create operation.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerAttributeId the attribute id in {@link String} format.
     * @param originalValue the value of the {@link FixedValue} instance before the modification.
     * @param fixedValue the modification payload.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws MalformedIdentifierException if the attribute id cannot be parsed.
     * @throws FixedValueOwnerNotFoundException if the attribute cannot be found.
     * @throws NotAFixedValueOwnerException if the attribute cannot be associated to fixed values.
     * @throws FixedValueNotFoundException if the requested fixed value is not found.
     * @throws EmptyValueException if the value property of the payload is blank.
     * @throws DuplicateResourceException if the updated/created value already exists.
     */
    void saveFixedValue(AppContextProvider contextProvider, String ownerAttributeId, String originalValue, FixedValue fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException, FixedValueNotFoundException, 
                   EmptyValueException, DuplicateResourceException;
    
    /**
     * Deletes the requested fixed value of a specified owner.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerAttributeId the attribute id in {@link String} format.
     * @param fixedValue the value to be deleted.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws MalformedIdentifierException if the attribute id cannot be parsed.
     * @throws FixedValueOwnerNotFoundException if the attribute cannot be found.
     * @throws NotAFixedValueOwnerException if the attribute cannot be associated to fixed values.
     * @throws FixedValueNotFoundException if the requested fixed value is not found.
     */
    void deleteFixedValue(AppContextProvider contextProvider, String ownerAttributeId, String fixedValue)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException, FixedValueNotFoundException;
    
    /**
     * Deletes all fixed values of a specified owner.
     * 
     * @param contextProvider an object that provides for application context info.
     * @param ownerAttributeId the attribute id in {@link String} format.
     * 
     * @throws UserAuthenticationException if the user is not logged in.
     * @throws MalformedIdentifierException if the attribute id cannot be parsed.
     * @throws FixedValueOwnerNotFoundException if the attribute cannot be found.
     * @throws NotAFixedValueOwnerException if the attribute cannot be associated to fixed values.
     */
    void deleteFixedValues(AppContextProvider contextProvider, String ownerAttributeId)
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException;
    
    public static final String PROPERTY_OWNER_ATTRIBUTE = "owner";
    public static final String PROPERTY_FIXED_VALUE = "fixedValue";
    public static final String PROPERTY_FIXED_VALUES = "fixedValues";
    
}