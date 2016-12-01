/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import edu.unibi.agbi.petrinet.model.entity.Arc;
import edu.unibi.agbi.petrinet.model.entity.Place;
import edu.unibi.agbi.petrinet.model.entity.Transition;
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
}
