/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.web.action;

import eionet.meta.dao.domain.StandardGenericStatus;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
public enum MissingConceptsStrategy {
    IGNORE,
    REMOVE,
    UPDATE_TO_INVALID,
    UPDATE_TO_DEPRECATED,
    UPDATE_TO_DEPRECATED_RETIRED,
    UPDATE_TO_DEPRECATED_SUPERSEDED;
    
    public StandardGenericStatus getStatus(){
        switch (this){
            case UPDATE_TO_INVALID: return StandardGenericStatus.INVALID;
            case UPDATE_TO_DEPRECATED: return StandardGenericStatus.DEPRECATED;
            case UPDATE_TO_DEPRECATED_RETIRED: return StandardGenericStatus.DEPRECATED_RETIRED;
            case UPDATE_TO_DEPRECATED_SUPERSEDED: return StandardGenericStatus.DEPRECATED_SUPERSEDED;
            default: {
                throw new IllegalArgumentException("No Status is linked with the given Strategy, "+this );
            }
        }
    }
}
