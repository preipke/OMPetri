/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import edu.unibi.agbi.petrinet.entity.PN_Node;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import edu.unibi.agbi.petrinet.entity.impl.Transition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author PR
 */
public class PetriNet
{
    private String author;
    private String name;
    private String description;
    
    private final Map<String,Arc> arcs;
    private final Map<String,Colour> colours;
    // TODO remove some of the following, depending on if place id can be the same as transition id or not
    private final Map<String,Place> places;
    private final Map<String,Transition> transitions;
    private final Map<String,PN_Node> placesAndTransitions;
    
    public PetriNet() {
        arcs = new HashMap();
        colours = new HashMap();
        places = new HashMap();
        transitions = new HashMap();
        placesAndTransitions = new HashMap();
    }
    
    public boolean add(Arc arc) {
        if (arcs.containsKey(arc.getId())) {
            return false;
        } 
        arcs.put(arc.getId(), arc);
        return true;
    }
    
    public boolean add(Colour colour) {
        if (colours.containsKey(colour.getId())) {
            return false;
        } 
        colours.put(colour.getId(), colour);
        return true;
    }
    
    public boolean add(Place place) {
        if (placesAndTransitions.containsKey(place.getId())) {
            return false;
        } 
        placesAndTransitions.put(place.getId(), place);
        places.put(place.getId() , place);
        return true;
    }
    
    public boolean add(Transition transition) {
        if (placesAndTransitions.containsKey(transition.getId())) {
            return false;
        } 
        placesAndTransitions.put(transition.getId(), transition);
        transitions.put(transition.getId(), transition);
        return true;
    }
    
    public Collection<Arc> getArcs() {
        return arcs.values();
    }
    
    public Collection<Colour> getColours() {
        return colours.values();
    }
    
    public Collection<Place> getPlaces() {
        return places.values();
    }
    
    public Collection<Transition> getTransitions() {
        return transitions.values();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
