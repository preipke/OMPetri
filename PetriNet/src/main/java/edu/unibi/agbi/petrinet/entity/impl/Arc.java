/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.impl;

import edu.unibi.agbi.petrinet.exception.IllegalAssignmentException;
import edu.unibi.agbi.petrinet.entity.PN_Element;
import edu.unibi.agbi.petrinet.entity.PN_Node;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Weight;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author PR
 */
public abstract class Arc extends PN_Element
{
    private Type arcType;
    
    private PN_Node target;
    private PN_Node source;
    
    private final Map<Colour,Weight> weights;
    
    public Arc() {
        
        type = PN_Element.Type.ARC;
        
        weights = new HashMap();
        weights.put(null, new Weight(null));
    }
    
    public void setWeight(Weight weight) {
        weights.put(weight.getColour(), weight);
    }
    
    public Weight getWeight(Colour colour) {
        return weights.get(colour);
    }
    
    public Collection<Weight> getWeights() {
        return weights.values();
    }
    
    public Map<Colour,Weight> getWeightMap() {
        return weights;
    }

    public void setTarget(PN_Node target) throws IllegalAssignmentException {
        if (source != null) {
            if (source instanceof Place && target instanceof Place) {
                throw new IllegalAssignmentException("Cannot assign target and source to be places!");
            } else if (source instanceof Transition && target instanceof Transition) {
                throw new IllegalAssignmentException("Cannot assign target and source to be transitions!");
            }
        }
        this.target = target;
        id = source.getId() + "_" + target.getId();
    }

    public void setSource(PN_Node source) throws IllegalAssignmentException {
        if (target != null) {
            if (source instanceof Place && target instanceof Place) {
                throw new IllegalAssignmentException("Cannot assign target and source to be places!");
            } else if (source instanceof Transition && target instanceof Transition) {
                throw new IllegalAssignmentException("Cannot assign target and source to be transitions!");
            }
        }
        this.source = source;
        id = source.getId() + "_" + target.getId();
    }

    public PN_Node getTarget() {
        return target;
    }

    public PN_Node getSource() {
        return source;
    }
    
    public Type getArcType() {
        return arcType;
    }
    
    public enum Type {
        DEFAULT, EQUAL, INHIBITORY, READ, RESET;
    }
}
