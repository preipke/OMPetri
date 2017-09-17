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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author PR
 */
public class Model
{
    private final Map<String, Colour> colors;
    private final Map<String, Parameter> parameters;

    private final Map<String, Arc> arcs;
    private final Map<String, Place> places;
    private final Map<String, Transition> transitions;

    public Model() {
        this.arcs = new HashMap();
        this.colors = new HashMap();
        this.parameters = new HashMap();
        this.places = new HashMap();
        this.transitions = new HashMap();
    }

    public void add(Colour color) {
        colors.put(color.getId(), color);
    }

    public void add(Parameter param) {
        parameters.put(param.getId(), param);
    }

    public void add(IArc arc) throws Exception {
        if (arcs.containsKey(arc.getId())) {
            throw new Exception("An arc has already been stored using the same identifier! (" + arc.getId() + ")");
        }
        arcs.put(arc.getId(), (Arc) arc);
        arc.getSource().getArcsOut().add(arc);
        arc.getTarget().getArcsIn().add(arc);
    }

    public void add(INode node) throws Exception {
        if (node instanceof Place) {
            if (places.containsKey(node.getId())) {
                throw new Exception("A place has already been stored using the same identifier! (" + node.getId() + ")");
            }
            places.put(node.getId(), (Place) node);
        } else {
            if (transitions.containsKey(node.getId())) {
                throw new Exception("A transition has already been stored using the same identifier! (" + node.getId() + ")");
            }
            transitions.put(node.getId(), (Transition) node);
        }
    }
    
    public void clear() {
        colors.clear();
        parameters.clear();
        arcs.clear();
        places.clear();
        transitions.clear();
    }
    
    public boolean contains(Parameter param) {
        return parameters.containsKey(param.getId());
    }
    
    public boolean contains(String nodeId) {
        if (places.containsKey(nodeId)) {
            return true;
        } else {
            return transitions.containsKey(nodeId);
        }
    }

    public boolean containsAndNotEqual(IArc arc) {
        if (!arcs.containsKey(arc.getId())) {
            return false;
        }
        return !arcs.get(arc.getId()).equals(arc);
    }

    public boolean containsAndNotEqual(INode node) {
        if (node.getElementType() == Element.Type.PLACE) {
            if (places.containsKey(node.getId())) {
                return !places.get(node.getId()).equals(node);
            }
        } else {
            if (transitions.containsKey(node.getId())) {
                return !transitions.get(node.getId()).equals(node);
            }
        }
        return false;
    }

    public IElement remove(IElement element) throws Exception {
        if (element instanceof Arc) {
            return remove((Arc) element);
        } else {
            return remove((INode) element);
        }
    }

    private IArc remove(Arc arc) throws Exception {
        for (Parameter param : arc.getRelatedParameters()) {
            if (param.getUsingElements().size() > 0) {
                throw new Exception("A related parameter is still being used by another element and cannot be deleted! "
                        + "(" + arc.getId() + " -> " + param.getId() + ")");
            }
        }
        for (Parameter param : arc.getRelatedParameters()) {
            parameters.remove(param.getId());
        }
        arc.getSource().getArcsOut().remove(arc);
        arc.getTarget().getArcsIn().remove(arc);
        return arcs.remove(arc.getId());
    }

    private INode remove(INode node) throws Exception {
        for (Parameter param : node.getRelatedParameters()) {
            if (param.getUsingElements().size() > 0) {
                if (!param.getUsingElements().iterator().next().equals(node)) {
                    throw new Exception("A related parameter is still being used by another element and cannot be deleted! "
                            + "(" + node.getId() + " -> " + param.getId() + ")");
                }
            }
        }
        for (Parameter param : node.getRelatedParameters()) {
            parameters.remove(param.getId());
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
        node.getRelatedParameters().forEach((param) -> {
            parameters.remove(param.getId());
        });
        return node;
    }

    private INode remove(Transition transition) {
        transition.getFunction().getParameterIds().forEach(id -> {
            parameters.get(id).getUsingElements().remove(transition);
        });
        return transitions.remove(transition.getId());
    }

    public Parameter remove(Parameter param) {
        param.getUsingElements().forEach(elem -> elem.getRelatedParameters().remove(param));
        return parameters.remove(param.getId());
    }

    public Collection<Arc> getArcs() {
        return arcs.values();
    }

    public List<Arc> getArcsCopy() {
        List<Arc> arcsCopy = new ArrayList();
        for (Arc arc : arcs.values()) {
            arcsCopy.add(arc);
        }
        return arcsCopy;
    }
    
    public Colour getColour(String id) {
        return colors.get(id);
    }

    public Collection<Colour> getColours() {
        return colors.values();
    }

    public IElement getElement(String id) {
        if (places.containsKey(id)) {
            return places.get(id);
        } else if (transitions.containsKey(id)) {
            return transitions.get(id);
        } else if (arcs.containsKey(id)) {
            return arcs.get(id);
        } else {
            return null;
        }
    }

    public Set<String> getNodeIds() {
        Set<String> nodeIds = new HashSet();
        nodeIds.addAll(places.keySet());
        nodeIds.addAll(transitions.keySet());
        return nodeIds;
    }

    public Parameter getParameter(String id) {
        return parameters.get(id);
    }

    public Set<String> getParameterIds() {
        return parameters.keySet();
    }

    public Collection<Parameter> getParameters() {
        return parameters.values();
    }

    public Collection<Place> getPlaces() {
        return places.values();
    }

    public List<Place> getPlacesCopy() {
        List<Place> placesCopy = new ArrayList();
        for (Place place : places.values()) {
            placesCopy.add(place);
        }
        return placesCopy;
    }

    public Collection<Transition> getTransitions() {
        return transitions.values();
    }

    public List<Transition> getTransitionsCopy() {
        List<Transition> transitionCopy = new ArrayList();
        for (Transition transition : transitions.values()) {
            transitionCopy.add(transition);
        }
        return transitionCopy;
    }
}
