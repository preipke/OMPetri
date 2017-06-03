/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.dao;

import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.model.Model;
import java.io.File;
import java.time.LocalDateTime;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author PR
 */
public class DataDao
{
    private final Model model;
    private final Graph graph;
    
    private LocalDateTime creationDateTime;
    private String id;
    private String author;
    private String description;
    private final StringProperty name;

    private File fileModel;
    private boolean hasChanges;
    
    private int nextNodeId;
    private int nextPlaceId;
    private int nextTransitionId;
    
    private int scalePower = 0;
    
    public DataDao() {
        model = new Model();
        graph = new Graph();
        name = new SimpleStringProperty();
        nextNodeId = 1;
        nextPlaceId = 1;
        nextTransitionId = 1;
    }
    
    public Model getModel() {
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    
    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }
    
    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setModelDescription(String description) {
        this.description = description;
    }
    
    public String getId() {
        return id;
    }
    
    public void setModelId(String id) {
        this.id = id;
    }

    public StringProperty getNameProperty() {
        return name;
    }

    public String getModelName() {
        return name.get();
    }

    public void setModelName(String name) {
        this.name.set(name);
    }
}
