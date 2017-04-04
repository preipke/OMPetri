/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.petrinet.entity.abstr.Arc;
import edu.unibi.agbi.petrinet.exception.IllegalAssignmentException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public final class DataArc extends Arc implements IDataArc
{
    private final List<IGraphElement> shapes;
    
    private String name;
    private String description;
    
    public DataArc(IDataNode source, IDataNode target, Arc.Type type) throws IllegalAssignmentException {
        super(source, target);
        setArcType(type);
        shapes = new ArrayList();
    }

    @Override
    public List<IGraphElement> getGraphElements() {
        return shapes;
    }

    @Override
    public IDataNode getSource() {
        return (IDataNode) this.source;
    }

    @Override
    public IDataNode getTarget() {
        return (IDataNode) this.target;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the label text for this data node and all related shapes in the scene.
     * @param text 
     */
    @Override
    public void setLabelText(String text) {
    }

    @Override
    public String getLabelText() {
        return "";
    }
}
