/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Weight;
import java.util.Map;

/**
 *
 * @author PR
 */
public interface IArc extends IElement
{
    public INode getSource();

    public INode getTarget();

    public Weight getWeight(Colour colour);

    public Map<Colour, Weight> getWeightMap();
}
