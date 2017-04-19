/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.impl;

import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.PetriNet;
import edu.unibi.agbi.petrinet.model.Weight;
import java.util.HashMap;
import java.util.Map;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.INode;
import edu.unibi.agbi.petrinet.entity.abstr.Element;

/**
 *
 * @author PR
 */
public class Arc extends Element implements IArc
{
    private Type arcType;
    
    protected INode target;
    protected INode source;
    
    private final Map<Colour,Weight> weights;
    
    public Arc(INode source, INode target) {
        
        id = source.getId() + "_" + target.getId();
        type = Element.Type.ARC;
        
        this.source = source;
        this.target = target;
        
        weights = new HashMap();
        weights.put(PetriNet.DEFAULT_COLOUR, new Weight(PetriNet.DEFAULT_COLOUR));
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
