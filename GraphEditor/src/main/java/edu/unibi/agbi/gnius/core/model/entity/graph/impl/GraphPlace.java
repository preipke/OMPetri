/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.entity.GravisType;
import edu.unibi.agbi.gravisfx.entity.parent.node.GravisCircle;

/**
 *
 * @author PR
 */
public class GraphPlace extends GravisCircle implements IGraphNode {

    private final DataPlace dataPlace;
    private boolean isDisabled = false;

    public GraphPlace(String id, DataPlace dataPlace) {
        super(id, GravisType.NODE);
        this.dataPlace = dataPlace;
        this.dataPlace.getShapes().add(this);
        this.setInnerRectangleVisible(false);
    }

    @Override
    public DataPlace getData() {
        return dataPlace;
    }

    @Override
    public boolean isElementDisabled() {
        return isDisabled;
    }

    @Override
    public void setElementDisabled(boolean value) {
        if (isDisabled != value) {
            isDisabled = value;
            getElementHandles().forEach(handle -> handle.setDisabled(value));
            getConnections().forEach(conn -> {
                IGraphArc arc = (IGraphArc) conn;
                arc.getData().setDisabled(value);
            });
        }
    }
}
