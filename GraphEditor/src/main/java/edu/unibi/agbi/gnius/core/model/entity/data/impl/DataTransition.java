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
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author PR
 */
public final class DataTransition extends Transition implements IDataNode
{
    private final Set<IGraphElement> shapes;

    private String description;
    private boolean isSticky = false;

    public DataTransition(String id, Transition.Type type) {
        super(id);
        setTransitionType(type);
        this.name = this.id;
        this.shapes = new HashSet();
    }

    @Override
    public Set<IGraphElement> getShapes() {
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

    @Override
    public String getLabelText() {
        if (shapes.isEmpty()) {
            return null;
        }
        return ((IGraphNode) shapes.iterator().next()).getLabel().getText();
    }

    /**
     * Sets the label text for all related shapes in the scene.
     *
     * @param text
     */
    @Override
    public void setLabelText(String text) {
        for (IGraphElement shape : shapes) {
            ((IGraphNode) shape).getLabel().setText(text);
        }
    }
    
    @Override
    public void setDisabled(boolean value) {
        super.setDisabled(value);
        for (IGraphElement shape : shapes) {
            ((IGraphNode) shape).getElementHandles().forEach(handle -> handle.setDisabled(value));
        }
    }

    @Override
    public boolean isSticky() {
        return isSticky;
    }

    @Override
    public void setSticky(boolean value) {
        isSticky = value;
    }
}
