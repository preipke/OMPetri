/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.entity.transition;

import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.entity.place.Place;
import java.util.List;

/**
 *
 * @author PR
 */
public class Transition
{
    private String name;
    private String label;
    private String description;
    
    private Object shape;
    
    private String function;
    private List<Parameter> parameter;
    private boolean isEnabled;
    
    private List<Place> incomingPlaces;
    private List<Place> outgoingPlaces;
}
