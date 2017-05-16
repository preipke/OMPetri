/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.dao;

import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.model.PetriNet;

/**
 *
 * @author PR
 */
public class DataaaDao
{
    private final PetriNet model;
    private final Graph graph;
    
    public DataaaDao(int nextPlaceId, int nextTransitionId) {
        model = new PetriNet(nextPlaceId, nextTransitionId);
        model.setName("Untitled");
        model.setAuthor(System.getProperty("user.name"));
        model.setDescription("New model.");
        graph = new Graph();
    }
    
    public PetriNet getModel() {
        return model;
    }
    
    public Graph getGraph() {
        return graph;
    }
}
