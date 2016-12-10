/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import edu.unibi.agbi.petrinet.model.entity.impl.Arc;
import edu.unibi.agbi.petrinet.model.entity.impl.Place;
import edu.unibi.agbi.petrinet.model.entity.impl.Transition;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class PetriNet
{
    private String author;
    private String name;
    private String description;
    
    private List<Place> places;
    private List<Transition> transitions;
    private List<Arc> arcs;
    
    public PetriNet() {
        places = new ArrayList();
        transitions = new ArrayList();
        arcs = new ArrayList();
    }
    
    public void add(Place place) {
        places.add(place);
    }
    
    public void add(Transition transition) {
        transitions.add(transition);
    }
    
    public void add(Arc arc) {
        arcs.add(arc);
    }
}
