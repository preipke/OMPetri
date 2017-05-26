/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.dao;

import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.model.PetriNet;
import java.io.File;

/**
 *
 * @author PR
 */
public class DataDao
{
    private final PetriNet model;
    private final Graph graph;
    
    private File fileModel;
    private boolean hasChanges;
    
    private int nextNodeId = 1;
    private int nextPlaceId = 1;
    private int nextTransitionId = 1;
    
    private int scalePower = 0;
    
    public DataDao() {
        model = new PetriNet();
        graph = new Graph();
        nextNodeId = 1;
        nextPlaceId = 1;
        nextTransitionId = 1;
    }
    
    public PetriNet getModel() {
        return model;
    }
    
    public Graph getGraph() {
        return graph;
    }
    
    public void clear() {
        graph.clear();
        model.clear();
    }
    
    public void setModelFile(File file) {
        fileModel = file;
    }
    
    public File getModelFile() {
        return fileModel;
    }
    
    public void setHasChanges(boolean value) {
        hasChanges = value;
    }
    
    public boolean hasChanges() {
        return hasChanges;
    }
    
    public int getNextNodeId() {
        return nextNodeId++;
    }
    
    public int getNextPlaceId() {
        return nextPlaceId++;
    }
    
    public int getNextTransitionId() {
        return nextTransitionId++;
    }
    
    public int getScalePower() {
        return scalePower;
    }
    
    public void setScalePower(int power) {
        scalePower = power;
    }
}
