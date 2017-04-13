/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import edu.unibi.agbi.petrinet.entity.impl.Place;
import java.util.ArrayList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.INode;

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
    
    private final Map<String,IArc> arcs;
    private final Map<String,INode> places;
    private final Map<String,INode> transitions;
    private final Map<String,INode> placesAndTransitions; // TODO remove if places and transitions can have the same names
    
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
    
    public boolean add(IArc arc) {
        if (arcs.containsKey(arc.getId())) {
            return false;
        } 
        arcs.put(arc.getId(), arc);
        return true;
    }
    
    public boolean add(INode node) {
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
    
    public INode remove(INode node) {
        if (placesAndTransitions.remove(node.getId()) == null) {
            return null;
        } 
        if (node instanceof Place) {
            return places.remove(node.getId());
        } else {
            return transitions.remove(node.getId());
        }
    }
    
    public Collection<IArc> getArcs() {
        return arcs.values();
    }
    
    public List<Colour> getColours() {
        return colors;
    }
    
    public Collection<INode> getPlaces() {
        return places.values();
    }
    
    public Collection<INode> getPlacesAndTransitions() {
        return placesAndTransitions.values();
    }
    
    public Collection<INode> getTransitions() {
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
