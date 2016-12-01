/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.entity;

import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.PNNode;
import java.util.List;

/**
 *
 * @author PR
 */
public class Transition extends PNNode
{
    private boolean isEnabled;
    
    private Function function;
    
    private List<Place> incomingPlaces;
    private List<Place> outgoingPlaces;
}
