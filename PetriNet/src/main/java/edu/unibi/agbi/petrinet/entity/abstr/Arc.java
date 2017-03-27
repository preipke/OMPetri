/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.abstr;

import edu.unibi.agbi.petrinet.exception.IllegalAssignmentException;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.PetriNet;
import edu.unibi.agbi.petrinet.model.Weight;

import java.util.HashMap;
import java.util.Map;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.INode;

/**
 *
 * @author PR
 */
public abstract class Arc extends Element implements IArc
{
    private Type arcType;
    
    protected INode target;
    protected INode source;
    
    private final Map<Colour,Weight> weights;
    
    public Arc(INode source, INode target) {
        
        type = Element.Type.ARC;
        
        this.source = source;
        this.target = target;
        
        id = source.getId() + "_" + target.getId();
        
        weights = new HashMap();
        weights.put(PetriNet.DEFAULT_COLOUR, new Weight(PetriNet.DEFAULT_COLOUR));
    }

    public void setTarget(INode target) throws IllegalAssignmentException {
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

    public void setSource(INode source) throws IllegalAssignmentException {
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

    @Override
    public INode getTarget() {
        return target;
    }

    @Override
    public INode getSource() {
        return source;
    }
    
    public void setWeight(Weight weight) {
        weights.put(weight.getColour(), weight);
    }
    
    @Override
    public Weight getWeight(Colour colour) {
        return weights.get(colour);
    }
    
    @Override
    public Map<Colour,Weight> getWeightMap() {
        return weights;
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
