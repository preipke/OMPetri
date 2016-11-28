/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pr
 */
public abstract class PNNode {
    
    private static final String IDENT = "petriNode_";
    private static int COUNT = 0;
    
    private final String internalId;
    
    private String name;
    private String label;
    private String description;
    
    private final List<Object> shapes;
    
    public PNNode() {
        synchronized (IDENT) {
            COUNT++;
            internalId = IDENT + COUNT;
        }
        shapes = new ArrayList();
    }
    
    public String getInternalId() {
        return internalId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the shapes
     */
    public List<Object> getShapes() {
        return shapes;
    }

    /**
     * @param shape the shape to add
     */
    public void addShape(Object shape) {
        this.shapes.add(shape);
    }
    
    /**
     * 
     * @param shape the shape to remove
     */
    public void removeShape(Object shape) {
        this.shapes.remove(shape);
    }
    
}
