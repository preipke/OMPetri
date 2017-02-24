/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import edu.unibi.agbi.petrinet.entity.IPN_Arc;
import edu.unibi.agbi.petrinet.entity.IPN_Node;
import edu.unibi.agbi.petrinet.entity.abstr.Place;
import java.util.ArrayList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PR
 */
public class PetriNet
{
    public final static Colour DEFAULT_COLOUR = new Colour("DEFAULT", "Default colour");
    
    private String author;
    private String name;
    private String description;
    
    private final List<Colour> colors;
    
    private final Map<String,IPN_Arc> arcs;
    private final Map<String,IPN_Node> places;
    private final Map<String,IPN_Node> transitions;
    private final Map<String,IPN_Node> placesAndTransitions; // TODO remove if places and transitions can have the same names
    
    public PetriNet() {
        colors = new ArrayList();
        colors.add(DEFAULT_COLOUR);
        
        arcs = new HashMap();
        places = new HashMap();
        transitions = new HashMap();
        placesAndTransitions = new HashMap();
    }
    
    public boolean add(Colour color) {
        if (colors.contains(color)) {
            return false;
        } 
        colors.add(color);
        return true;
    }
    
    public boolean add(IPN_Arc arc) {
        if (arcs.containsKey(arc.getId())) {
            return false;
        } 
        arcs.put(arc.getId(), arc);
        return true;
    }
    
    public boolean add(IPN_Node node) {
        if (placesAndTransitions.containsKey(node.getId())) {
            return false;
        } 
        placesAndTransitions.put(node.getId(), node);
        if (node instanceof Place) {
            places.put(node.getId(), node);
        } else {
            transitions.put(node.getId(), node);
        }
        return true;
    }
    
    public Collection<IPN_Arc> getArcs() {
        return arcs.values();
    }
    
    public List<Colour> getColours() {
        return colors;
    }
    
    public Collection<IPN_Node> getPlaces() {
        return places.values();
    }
    
    public Collection<IPN_Node> getPlacesAndTransitions() {
        return placesAndTransitions.values();
    }
    
    public Collection<IPN_Node> getTransitions() {
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
