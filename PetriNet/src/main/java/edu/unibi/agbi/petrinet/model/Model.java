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
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import edu.unibi.agbi.petrinet.model.parameter.ReferencingParameter.ReferenceType;
import edu.unibi.agbi.petrinet.util.ParameterFactory;
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

    /**
     * Attempts to add a parameter.
     *
     * @param param
     * @throws Exception
     */
    public void add(Parameter param) throws Exception {

        switch (param.getType()) {

            case GLOBAL:

                if (containsParameter(param.getId())) {
                    throw new Exception("Conflict! Another parameter has already been stored using the same ID!");
                }
                parameters.put(param.getId(), param);
                break;

            case LOCAL:

                if (param.getRelatedElement().getLocalParameter(param.getId()) != null) {
                    throw new Exception("Conflict! Another parameter has already been stored using the same ID!");
                }
                param.getRelatedElement().addLocalParameter(param);
                break;

            case REFERENCE:

                param.getRelatedElement().getRelatedParameters().add(param);
                break;

            default:
                throw new Exception("Unhandled parameter type detected!");

        }

    }

    public void add(IElement element) throws Exception {
        if (containsAndNotEqual(element)) {
            throw new Exception("Another element has already been stored using the same identifier! (" + element.getId() + ")");
        }
        switch (element.getElementType()) {
            case ARC:
                add((Arc) element);
                break;
            case PLACE:
                places.put(element.getId(), (Place) element);
                break;
            case TRANSITION:
                transitions.put(element.getId(), (Transition) element);
                break;
            default:
                throw new Exception("Unhandled element type!");
        }
    }

    private void add(IArc arc) throws Exception {
        arcs.put(arc.getId(), (Arc) arc);
        arc.getSource().getArcsOut().add(arc);
        arc.getTarget().getArcsIn().add(arc);
    }

    public void clear() {
        colors.clear();
        parameters.clear();
        arcs.clear();
        places.clear();
        transitions.clear();
    }

    public boolean contains(IElement element) throws Exception {
        switch (element.getElementType()) {
            case ARC:
                return arcs.containsKey(element.getId());
            case PLACE:
                return places.containsKey(element.getId());
            case TRANSITION:
                return transitions.containsKey(element.getId());
            default:
                throw new Exception("Unhandled element type!");
        }
    }

    /**
     * Validates if an ID is used within a model. Checks arc, place, transition
     * and parameter IDs.
     *
     * @param id
     * @return
     */
    public boolean containsElement(String id) {
        if (arcs.containsKey(id)) {
            return true;
        } else if (places.containsKey(id)) {
            return true;
        } else {
            return transitions.containsKey(id);
        }
    }

    /**
     * Validates if an ID is used within a model. Checks arc, place, transition
     * and parameter IDs.
     *
     * @param id
     * @return
     */
    public boolean containsParameter(String id) {
        return parameters.containsKey(id);
    }

    /**
     * Indicates wether another element has been stored using the exact same
     * identifier as a given element or not.
     *
     * @param element
     * @return
     * @throws Exception
     */
    private boolean containsAndNotEqual(IElement element) throws Exception {
        
        switch (element.getElementType()) {
            
            case ARC:
                if (arcs.containsKey(element.getId())) {
                    return !arcs.get(element.getId()).equals(element);
                }
                return false;
                
            case PLACE:
                if (places.containsKey(element.getId())) {
                    return !places.get(element.getId()).equals(element);
                }
                return false;
                
            case TRANSITION:
                if (transitions.containsKey(element.getId())) {
                    return !transitions.get(element.getId()).equals(element);
                }
                return false;
                
            default:
                throw new Exception("Unhandled element type!");
        }
    }
    
    public void changeId(IElement element, String elementIdNew) throws Exception {

//        System.out.println("Changing ID: '" + element.getId() + "' -> '" + elementIdNew + "'");
        
        switch (element.getElementType()) {
            
            case ARC:
                if (arcs.containsKey(element.getId())) {
                    arcs.remove(element.getId());
                    arcs.put(elementIdNew, (Arc) element);
                } else {
                    throw new Exception("Trying to change ID for non-existing arc!");
                }
                break;
                
            case PLACE:
                if (places.containsKey(element.getId())) {
                    places.remove(element.getId());
                    places.put(elementIdNew, (Place) element);
                } else {
                    throw new Exception("Trying to change ID for non-existing place!");
                }
                break;
                
            case TRANSITION:
                if (transitions.containsKey(element.getId())) {
                    transitions.remove(element.getId());
                    transitions.put(elementIdNew, (Transition) element);
                } else {
                    throw new Exception("Trying to change ID for non-existing transition!");
                }
                break;

            default:
                throw new Exception("Trying to change ID for unhandled element type!");
        }
        
        element.setId(elementIdNew);
    }

    public Parameter findParameter(ParameterFactory parameterFactory, String paramId, IElement element) {

        Parameter param;
        param = null;

        // highest priority - LOCAL param
        if (element != null) {
            param = element.getLocalParameter(paramId);
        }

        // GLOBAL param
        if (param == null) {
            param = getParameter(paramId);
        }

        // REFERENCE param
        if (param == null) {
            param = findReferencingParameter(parameterFactory, paramId);
        }

        return param;
    }

    private Parameter findReferencingParameter(ParameterFactory parameterFactory, final String paramId) {

        ReferenceType referenceType;
        Parameter param;
        param = null;

        String elementId;
        elementId = parameterFactory.recoverElementIdFromReferencingParameterId(paramId);

        IElement element;
        element = getElement(elementId);

        if (element != null) {

            for (Parameter par : element.getRelatedParameters()) {

                // TODO use regex pattern to detect variations of referencing parameters for the same element
                if (par.getId().contentEquals(paramId)) {
                    param = par;
                }
            }

            if (param == null) {

                try {

                    referenceType = parameterFactory.recoverReferenceTypeFromParameterId(element, paramId);
                    param = parameterFactory.createReferencingParameter(element, paramId, referenceType);
                    add(param);

                } catch (Exception ex) {
                    System.out.println("Exception in generating referencing parameter: " + ex.getMessage());
                    param = null;
                }
            }
        }

        return param;
    }

    public IElement remove(IElement element) throws Exception {
        switch (element.getElementType()) {
            case ARC:
                return remove((Arc) element);
            case PLACE:
                return remove((INode) element);
            case TRANSITION:
                return remove((INode) element);
            default:
                throw new Exception("Unhandled element type!");
        }
    }

    private IArc remove(Arc arc) throws Exception {
        for (Parameter param : arc.getRelatedParameters()) {
            if (param.getUsingElements().size() > 0) {
                if (!param.getUsingElements().iterator().next().equals(arc)) {
                    throw new Exception("A related parameter is still being used by another element and cannot be deleted! "
                            + "(" + arc.getId() + " -> " + param.getId() + ")");
                }
            }
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

    public Collection<Transition> getTransitions() {
        return transitions.values();
    }
}
