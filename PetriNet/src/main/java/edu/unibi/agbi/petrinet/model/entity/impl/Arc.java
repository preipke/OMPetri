/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.entity.impl;

import edu.unibi.agbi.petrinet.exception.IllegalAssignmentException;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.entity.PN_Element;

/**
 *
 * @author PR
 */
public abstract class Arc extends PN_Element
{
    private Type arcType;
    
    private PN_Element target;
    private PN_Element source;
    
    public Arc() {
        
        type = PN_Element.Type.ARC;
        
        getParameter().add(new Parameter("Weight", null, null, Parameter.Type.COMPUTE));
    }

    public void setTarget(PN_Element target) throws IllegalAssignmentException {
        if (source != null) {
            if (source instanceof Place && target instanceof Place) {
                throw new IllegalAssignmentException("Cannot assign target and source to be places!");
            } else if (source instanceof Transition && target instanceof Transition) {
                throw new IllegalAssignmentException("Cannot assign target and source to be transitions!");
            }
        }
        this.target = target;
    }

    public void setSource(PN_Element source) throws IllegalAssignmentException {
        if (target != null) {
            if (source instanceof Place && target instanceof Place) {
                throw new IllegalAssignmentException("Cannot assign target and source to be places!");
            } else if (source instanceof Transition && target instanceof Transition) {
                throw new IllegalAssignmentException("Cannot assign target and source to be transitions!");
            }
        }
        this.source = source;
    }

    public PN_Element getTarget() {
        return target;
    }

    public PN_Element getSource() {
        return source;
    }
    
    public Type getArcType() {
        return arcType;
    }
    
    public enum Type {
        EQUAL, INHIBITORY, READ, RESET, DEFAULT;
    }
}
