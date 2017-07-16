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
public class GraphArc extends GravisFlexEdge implements IGraphArc
{
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
        dataArc.setDisabled(value);
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
