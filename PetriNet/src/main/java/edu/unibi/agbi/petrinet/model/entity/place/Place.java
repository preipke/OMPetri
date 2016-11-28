/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.entity.place;

import edu.unibi.agbi.petrinet.model.PNNode;
import edu.unibi.agbi.petrinet.model.PNNode;
import edu.unibi.agbi.petrinet.model.entity.transition.Transition;
import edu.unibi.agbi.petrinet.model.entity.transition.Transition;
import java.util.List;

/**
 *
 * @author PR
 */
public class Place extends PNNode
{
    private double token;
    private double tokenCapacity;
    
    private List<Transition> incomingTransitions;
    private List<Transition> outgoingTransitions;
}
