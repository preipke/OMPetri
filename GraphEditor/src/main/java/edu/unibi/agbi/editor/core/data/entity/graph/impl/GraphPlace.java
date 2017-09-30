/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.data.entity.graph.impl;

import edu.unibi.agbi.editor.core.data.entity.data.impl.DataPlace;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.entity.child.GravisChildLabel;
import edu.unibi.agbi.gravisfx.entity.root.GravisType;
import edu.unibi.agbi.gravisfx.entity.root.node.GravisCircle;

/**
 *
 * @author PR
 */
public final class GraphPlace extends GravisCircle implements IGraphNode {

    private final DataPlace dataPlace;
    private boolean isDisabled = false;
    
    private final GravisChildLabel token;

    public GraphPlace(String id, DataPlace dataPlace) {
        super(id, GravisType.NODE);
        this.setInnerRectangleVisible(false);
        this.dataPlace = dataPlace;
        this.dataPlace.getShapes().add(this);
        this.token = new GravisChildLabel(this);
        this.token.xProperty().bind(translateXProperty());
        this.token.yProperty().bind(translateYProperty());
        this.getLabels().add(token);
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
