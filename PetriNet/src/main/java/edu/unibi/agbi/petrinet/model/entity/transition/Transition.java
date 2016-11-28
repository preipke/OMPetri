/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.entity.transition;

import edu.unibi.agbi.petrinet.model.entity.place.Place;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.List;
import edu.unibi.agbi.petrinet.model.PNNode;
import edu.unibi.agbi.petrinet.model.PNNode;

/**
 *
 * @author PR
 */
public class Transition extends PNNode
{
    private String function;
    private List<Parameter> parameter;
    private boolean isEnabled;
    
    private List<Place> incomingPlaces;
    private List<Place> outgoingPlaces;
}
