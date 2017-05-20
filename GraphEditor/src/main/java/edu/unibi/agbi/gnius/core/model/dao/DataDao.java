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
}
