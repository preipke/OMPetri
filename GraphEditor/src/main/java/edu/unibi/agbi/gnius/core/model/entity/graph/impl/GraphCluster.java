/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.entity.parent.node.GravisCircleRectangle;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class GraphCluster extends GravisCircleRectangle implements IGraphNode {
    
    private final DataCluster dataCluster;
    
    public GraphCluster(DataCluster dataCluster) {
        super();
        this.dataCluster = dataCluster;
    }

    @Override
    public DataCluster getDataElement() {
        return dataCluster;
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
