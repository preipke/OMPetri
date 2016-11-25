/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.transition;

import edu.unibi.agbi.petrinet.entity.place.Place;
import java.util.List;

/**
 *
 * @author PR
 */
public class Transition
{
    private String label;
    private String name;
    
    private Object shape;
    
    private List<Place> incomingPlaces;
    private List<Place> outgoingPlaces;
}
