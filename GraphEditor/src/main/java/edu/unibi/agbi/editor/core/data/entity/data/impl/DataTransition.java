/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.data.entity.data.impl;

import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author PR
 */
public final class DataTransition extends Transition implements IDataNode
{
    private final DataType dataType;
    private final Set<IGraphElement> shapes;

    private String description;
    private boolean isSticky = false;

    public DataTransition(String id, Transition.Type type) {
        super(id, type);
        super.name = super.id;
        this.dataType = DataType.TRANSITION;
        this.shapes = new HashSet();
    }

    @Override
    public DataType getType() {
        return dataType;
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
    public void setDisabled(boolean value) {
        super.setDisabled(value);
        for (IGraphElement shape : shapes) {
            shape.setElementDisabled(value);
        }
    }

    @Override
    public String getLabelText() {
        if (shapes.isEmpty()) {
            return null;
        }
        return ((IGraphNode) shapes.iterator().next()).getLabels().get(0).getText();
    }

    @Override
    public void setLabelText(String text) {
        for (IGraphElement shape : shapes) {
            ((IGraphNode) shape).getLabels().get(0).setText(text);
        }
    }

    @Override
    public Set<IGraphElement> getShapes() {
        return shapes;
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
