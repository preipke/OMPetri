/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.entity.GravisType;
import edu.unibi.agbi.gravisfx.entity.parent.connection.GravisFlexEdge;

/**
 *
 * @author PR
 */
public class GraphArc extends GravisFlexEdge implements IGraphArc {

    private final IDataArc dataArc;

    public GraphArc(String id, IGraphNode source) {
        super(id, source, GravisType.CONNECTION);
        this.dataArc = null;
    }

    public GraphArc(String id, IGraphNode source, IGraphNode target, IDataArc dataArc) {
        super(id, source, target, GravisType.CONNECTION);
        this.dataArc = dataArc;
        this.dataArc.getShapes().add(this);
    }

    @Override
    public IDataArc getData() {
        return dataArc;
    }

    @Override
    public boolean isElementDisabled() {
        return dataArc.isDisabled();
    }

    @Override
    public void setElementDisabled(boolean value) {

        boolean active;

        dataArc.setDisabled(value);

        getElementHandles().forEach(handle -> handle.setDisabled(value));

        if (value) { // arc disabled

            if (!getSource().isElementDisabled()) { // source not disabled

                active = getSource().getConnections().stream() // for all related connections
                        .anyMatch(conn -> !((IGraphArc) conn).isElementDisabled()); // check if there is any enabled connection

                if (!active) { // no active connection found, indicate that node is disabled
                    getSource().setElementDisabled(value);
                }
            }

            if (!getTarget().isElementDisabled()) { // target not disabled

                active = getTarget().getConnections().stream() // for all related connections
                        .anyMatch(conn -> !((IGraphArc) conn).isElementDisabled()); // check if there is any enabled connection

                if (!active) { // no active connection found, indicate that node is disabled
                    getTarget().setElementDisabled(value);
                }
            }

        } else { // arc enabled

            if (!getSource().getData().isDisabled() // source data enabled
                    && getSource().isElementDisabled()) { // node disabled
                getSource().setElementDisabled(false);
            }
            if (!getTarget().getData().isDisabled() // target data enabled
                    && getTarget().isElementDisabled()) { // node disabled
                getTarget().setElementDisabled(false);
            }
        }
    }

    @Override
    public IGraphNode getSource() {
        return (IGraphNode) super.getSource();
    }

    @Override
    public IGraphNode getTarget() {
        return (IGraphNode) super.getTarget();
    }
}
