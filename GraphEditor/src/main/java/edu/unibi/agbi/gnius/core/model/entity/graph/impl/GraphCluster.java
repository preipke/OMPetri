/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphCluster;
import edu.unibi.agbi.gravisfx.entity.parent.node.GravisCircleRectangle;

/**
 *
 * @author PR
 */
public class GraphCluster extends GravisCircleRectangle implements IGraphCluster {
    
    private final DataCluster dataCluster;
    
    public GraphCluster(DataCluster dataCluster) {
        super();
        this.dataCluster = dataCluster;
    }
    
    @Override
    public DataCluster getDataElement() {
        return dataCluster;
    }
}
