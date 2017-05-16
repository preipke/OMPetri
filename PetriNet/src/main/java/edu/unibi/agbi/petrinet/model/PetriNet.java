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
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author PR
 */
public class PetriNet
{
    private String author;
    private String name;
    private String description;
    
    private int nextPlaceId;
    private int nextTransitionId;
    
    private final Set<Colour> colors;
    private final Map<String,Parameter> parameters;
    
    private final Set<String> nodeIds;
    private final Map<String,Arc> arcs;
    private final Map<String,Place> places;
    private final Map<String,Transition> transitions;
    
    public PetriNet(int nextPlaceId, int nextTransitionId) {
        this.nextPlaceId = nextPlaceId;
        this.nextTransitionId = nextTransitionId;
        this.colors = new HashSet();
        this.colors.add(new Colour("DEFAULT", "Default colour"));
        this.parameters = new HashMap();
        this.nodeIds = new HashSet();
        this.arcs = new HashMap();
        this.places = new HashMap();
        this.transitions = new HashMap();
    }
    
    public void add(Colour color) {
        colors.add(color);
    }
    
    public void add(Parameter param) {
        parameters.put(param.getId(), param);
    }
    
    public void add(Arc arc) {
        arcs.put(arc.getId(), arc);
        arc.getSource().getArcsOut().add(arc);
        arc.getTarget().getArcsIn().add(arc);
    }
    
    public void add(INode node) {
        nodeIds.add(node.getId());
        if (node instanceof Place) {
            places.put(node.getId(), (Place) node);
        } else {
            transitions.put(node.getId(), (Transition) node);
        }
    }
    
    public boolean containsAndNotEqual(Arc arc) {
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
        if (!parameters.containsKey(param.getId())) {
            return false;
        }
        return !parameters.get(param.getId()).equals(param);
    }
    
    public IElement remove(IElement element) {
        if (element instanceof Arc) {
            return remove((Arc) element);
        } else {
            return remove((INode) element);
        }
    }
    
    private IArc remove(Arc arc) {
        arc.getParameters().values().forEach(param -> {
            parameters.remove(param.getId());
        });
        arc.getSource().getArcsOut().remove(arc);
        arc.getTarget().getArcsIn().remove(arc);
        return arcs.remove(arc.getId());
    }
    
    private INode remove(INode node) {
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
            node = places.remove(node.getId());
        } else {
            node = remove((Transition) node);
        }
        node.getParameters().values().forEach((param) -> {
            parameters.remove(param.getId());
        });
        return node;
    }
    
    private INode remove(Transition transition) {
        transition.getFunction().getParameterIds().forEach(id -> {
            parameters.get(id).getReferingNodes().remove(transition);
        });
        return transitions.remove(transition.getId());
    }
    
    public Parameter remove(Parameter param) {
        return parameters.remove(param.getId());
    }
    
    public Collection<Arc> getArcs() {
        return arcs.values();
    }
    
    public Set<Colour> getColours() {
        return colors;
    }
    
    public int getNextPlaceId() {
        return nextPlaceId++;
    }
    
    public int getNextTransitionId() {
        return nextTransitionId++;
    }
    
    public INode getNode(String id) {
        if (places.containsKey(id)) {
            return places.get(id);
        } else if (transitions.containsKey(id)) {
            return transitions.get(id);
        } else {
            return null;
        }
    }
    
    public Set<String> getNodeIds() {
        return nodeIds;
    }

    public Map<String, Parameter> getParameters() {
        return parameters;
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
