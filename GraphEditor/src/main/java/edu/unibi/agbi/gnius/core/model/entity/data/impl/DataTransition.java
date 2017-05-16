/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public final class DataTransition extends Transition implements IDataNode
{
    private final List<IGraphElement> shapes;
    
    private String description = "";
    
    public DataTransition(String id, Transition.Type type) {
        super(id);
        setTransitionType(type);
        this.name = this.id;
        this.shapes = new ArrayList();
    }

    @Override
    public List<IGraphElement> getShapes() {
        return shapes;
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
        for (IGraphElement shape : shapes) {
            ((IGraphNode)shape).getLabel().setText(text);
        }
    }
    
    @Override
    public String getLabelText() {
        return ((IGraphNode)shapes.get(0)).getLabel().getText();
    }

    @Override
    public String toString() {
        return "(" + id + ") " + name;
    }
}
