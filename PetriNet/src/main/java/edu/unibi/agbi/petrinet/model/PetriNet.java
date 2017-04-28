/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import edu.unibi.agbi.petrinet.entity.impl.Place;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.entity.INode;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import java.util.HashSet;
import java.util.Set;

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
    
    private final Set<Colour> colors;
    private final Map<String,Parameter> parameter;
    
    private final Set<String> nodeIds;
    private final Map<String,IArc> arcs;
    private final Map<String,INode> places;
    private final Map<String,INode> transitions;
    
    public PetriNet() {
        
        colors = new HashSet();
        colors.add(DEFAULT_COLOUR);
        parameter = new HashMap();
        
        nodeIds = new HashSet();
        arcs = new HashMap();
        places = new HashMap();
        transitions = new HashMap();
    }
    
    public void add(Colour color) {
        colors.add(color);
    }
    
    public void add(Parameter param) {
        parameter.put(param.getId(), param);
    }
    
    public void add(IArc arc) {
        arcs.put(arc.getId(), arc);
        arc.getSource().getArcsOut().add(arc);
        arc.getTarget().getArcsIn().add(arc);
    }
    
    public void add(INode node) {
        nodeIds.add(node.getId());
        if (node instanceof Place) {
            places.put(node.getId(), node);
        } else {
            transitions.put(node.getId(), node);
        }
    }
    
    public boolean containsAndNotEqual(IArc arc) {
        if (!arcs.containsKey(arc.getId())) {
            return false;
        }
        return !arcs.get(arc.getId()).equals(arc);
    }
    
    public boolean containsAndNotEqual(INode node) {
        if (!nodeIds.contains(node.getId())) {
            return false;
        }
        if (node.getElementType() == Element.Type.PLACE) {
            return !places.get(node.getId()).equals(node);
        } else {
            return !transitions.get(node.getId()).equals(node);
        }
    }
    
    public boolean containsAndNotEqual(Parameter param) {
        if (!parameter.containsKey(param.getId())) {
            return false;
        }
        return !parameter.get(param.getId()).equals(param);
    }
    
    public IArc remove(IArc arc) {
        arc.getSource().getArcsOut().remove(arc);
        arc.getTarget().getArcsIn().remove(arc);
        return arcs.remove(arc.getId());
    }
    
    public INode remove(INode node) {
        if (!nodeIds.remove(node.getId())) {
            return null;
        } 
        while (!node.getArcsIn().isEmpty()) {
            remove(node.getArcsIn().remove(0));
        }
        while (!node.getArcsOut().isEmpty()) {
            remove(node.getArcsOut().remove(0));
        }
        if (node instanceof Place) {
            return places.remove(node.getId());
        } else {
            return transitions.remove(node.getId());
        }
    }
    
    public Parameter remove(Parameter param) {
        for (IElement node : param.getReferingNodes()) {
            node.getParameters().remove(param);
        }
        return parameter.remove(param.getId());
    }
    
    public Collection<IArc> getArcs() {
        return arcs.values();
    }
    
    public Set<Colour> getColours() {
        return colors;
    }
    
    public Set<String> getNodeIds() {
        return nodeIds;
    }
    
    public Collection<Parameter> getParameter() {
        return parameter.values();
    }
    
    public Collection<INode> getPlaces() {
        return places.values();
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
