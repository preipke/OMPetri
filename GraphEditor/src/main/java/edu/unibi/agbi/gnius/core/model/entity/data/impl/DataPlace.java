/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public final class DataPlace extends Place implements IDataNode
{
    private final List<IGraphElement> shapes;

    private String description = "";

    public DataPlace(String id, Place.Type type) {
        super(id);
        setPlaceType(type);
        this.name = this.id;
        this.shapes = new ArrayList();
    }

    @Override
    public List<IGraphElement> getShapes() {
        return shapes;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
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
    public String getLabelText() {
        if (shapes.isEmpty()) {
            return null;
        }
        return ((IGraphNode) shapes.get(0)).getLabel().getText();
    }

    @Override
    public String toString() {
        return "(" + id + ") " + name;
    }
}
