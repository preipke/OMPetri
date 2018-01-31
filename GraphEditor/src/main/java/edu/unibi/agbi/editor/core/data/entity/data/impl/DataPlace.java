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
import edu.unibi.agbi.gravisfx.entity.child.GravisChildLabel;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import edu.unibi.agbi.petrinet.model.ConflictResolutionStrategy;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author PR
 */
public final class DataPlace extends Place implements IDataNode
{
    private final DataType dataType;
    private final Set<IGraphElement> shapes;

    private String description;
    private boolean isSticky = false;

    public DataPlace(String id, Place.Type type) {
        super(id, type, ConflictResolutionStrategy.PRIORITY);
        this.dataType = DataType.PLACE;
        this.shapes = new HashSet();
    }

    @Override
    public DataType getType() {
        return dataType;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
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
    
    public String getTokenLabelText() {
        if (shapes.isEmpty()) {
            return null;
        }
        return ((IGraphNode) shapes.iterator().next()).getLabels().get(1).getText();
    }
    
    public void setTokenLabelText(String text) {
        GravisChildLabel tokenLabel;
        for (IGraphElement shape : shapes) {
            tokenLabel = ((IGraphNode) shape).getLabels().get(1);
            tokenLabel.setText(text);
            tokenLabel.setTranslateY(tokenLabel.getBoundsInLocal().getHeight() / 4);
            tokenLabel.setTranslateX(- tokenLabel.getBoundsInLocal().getWidth() / 2);
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
