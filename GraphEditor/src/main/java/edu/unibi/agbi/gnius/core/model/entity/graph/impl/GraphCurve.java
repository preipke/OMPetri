/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.entity.parent.connection.GravisCurve;

/**
 *
 * @author PR
 */
public class GraphCurve extends GravisCurve implements IGraphArc
{
    private final DataArc dataArc;

    public GraphCurve(IGraphNode source , IGraphNode target , DataArc dataArc) {
        super(source , target);
        this.dataArc = dataArc;
        this.dataArc.getShapes().add(this);
    }
    
    @Override
    public DataArc getDataElement() {
        return dataArc;
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
