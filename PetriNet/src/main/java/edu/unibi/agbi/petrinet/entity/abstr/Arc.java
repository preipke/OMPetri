/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.abstr;

import edu.unibi.agbi.petrinet.entity.IPN_Node;
import edu.unibi.agbi.petrinet.entity.IPN_Node;
import edu.unibi.agbi.petrinet.exception.IllegalAssignmentException;
import edu.unibi.agbi.petrinet.entity.PN_Element;
import edu.unibi.agbi.petrinet.entity.PN_Element;
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
    
    private IPN_Node target;
    private IPN_Node source;
    
    private final Map<Colour,Weight> weights;
    
    public Arc(IPN_Node source, IPN_Node target) {
        
        type = PN_Element.Type.ARC;
        
        this.source = source;
        this.target = target;
        
        id = source.getId() + "_" + target.getId();
        
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

    public void setTarget(IPN_Node target) throws IllegalAssignmentException {
        if (source != null) {
            if (source instanceof Place && target instanceof Place) {
                throw new IllegalAssignmentException("Cannot assign target and source to be places!");
            } else if (source instanceof Transition && target instanceof Transition) {
                throw new IllegalAssignmentException("Cannot assign target and source to be transitions!");
            }
            this.id = source.getId() + "_" + target.getId();
        }
        this.target = target;
    }

    public void setSource(IPN_Node source) throws IllegalAssignmentException {
        if (target != null) {
            if (source instanceof Place && target instanceof Place) {
                throw new IllegalAssignmentException("Cannot assign target and source to be places!");
            } else if (source instanceof Transition && target instanceof Transition) {
                throw new IllegalAssignmentException("Cannot assign target and source to be transitions!");
            }
            this.id = source.getId() + "_" + target.getId();
        }
        this.source = source;
    }

    public IPN_Node getTarget() {
        return target;
    }

    public IPN_Node getSource() {
        return source;
    }
    
    public void setArcType(Type arcType) {
        this.arcType = arcType;
    }
    
    public Type getArcType() {
        return arcType;
    }
    
    public enum Type {
        READ, EQUAL, INHIBITORY, RESET;
    }
}
