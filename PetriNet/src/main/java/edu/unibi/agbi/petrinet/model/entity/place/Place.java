/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.entity.place;

import edu.unibi.agbi.petrinet.model.entity.transition.Transition;
import java.util.List;

/**
 *
 * @author PR
 */
public class Place
{
    private String name;
    private String description;
    
    private Object shape;
    
    private double token;
    private double tokenCapacity;
    
    private List<Transition> incomingTransitions;
    private List<Transition> outgoingTransitions;
}
