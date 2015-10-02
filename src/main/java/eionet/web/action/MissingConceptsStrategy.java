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
    MAINTAIN_IGNORE,
    REMOVE,
    SET_STATUS_INVALID,
    SET_STATUS_DEPRECATED,
    SET_STATUS_DEPRECATED_RETIRED,
    SET_STATUS_DEPRECATED_SUPERSEDED;
    
    public StandardGenericStatus getStatus(){
        switch (this){
            case SET_STATUS_INVALID: return StandardGenericStatus.INVALID;
            case SET_STATUS_DEPRECATED: return StandardGenericStatus.DEPRECATED;
            case SET_STATUS_DEPRECATED_RETIRED: return StandardGenericStatus.DEPRECATED_RETIRED;
            case SET_STATUS_DEPRECATED_SUPERSEDED: return StandardGenericStatus.DEPRECATED_SUPERSEDED;
            default: {
                throw new IllegalArgumentException("No Status is linked with the given Strategy, "+this );
            }
        }
    }
}
