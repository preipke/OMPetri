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
public class DataDao
{
    private final PetriNet model;
    private final Graph graph;
    
    public DataDao() {
        model = new PetriNet();
        graph = new Graph();
    }
    
    public PetriNet getModel() {
        return model;
    }
    
    public Graph getGraph() {
        return graph;
    }
}
