/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.entity.parent.node.GravisCircleDouble;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class GraphPlace extends GravisCircleDouble implements IGraphNode
{
    private final DataPlace dataPlace;
    
    public GraphPlace(DataPlace dataPlace) {
        super();
        this.dataPlace = dataPlace;
    }
    
    @Override
    public DataPlace getDataElement() {
        return dataPlace;
    }

    @Override
    public List<IGraphArc> getGraphConnections() {
        List<IGraphArc> connections = new ArrayList();
        for (int i = 0; i < getConnections().size(); i++) {
            connections.add((IGraphArc) getConnections().get(i));
        }
        return connections;
    }
}
