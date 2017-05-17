/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphCluster;
import edu.unibi.agbi.gravisfx.entity.parent.node.GravisCircle;

/**
 *
 * @author PR
 */
public class GraphCluster extends GravisCircle implements IGraphCluster {
    
    private final DataCluster dataCluster;
    
    public GraphCluster(String id, DataCluster dataCluster) {
        super(id);
        this.dataCluster = dataCluster;
        this.setInnerCircleVisible(false);
    }
    
    @Override
    public DataCluster getDataElement() {
        return dataCluster;
    }
}
