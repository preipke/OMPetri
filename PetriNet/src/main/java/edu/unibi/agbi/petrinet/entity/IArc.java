/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Weight;
import java.util.Collection;

/**
 *
 * @author PR
 */
public interface IArc extends IElement
{
    public INode getSource();

    public INode getTarget();

    public void addWeight(Weight weight);

    public Weight getWeight(Colour colour);

    public Collection<Weight> getWeights();

    public void setArcType(Arc.Type arcType);

    public Arc.Type getArcType();
}
