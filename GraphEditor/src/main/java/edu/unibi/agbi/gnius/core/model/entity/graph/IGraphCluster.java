/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph;

import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataCluster;
import edu.unibi.agbi.gravisfx.graph.Graph;

/**
 *
 * @author PR
 */
public interface IGraphCluster extends IGraphNode {
    @Override public DataCluster getDataElement();
    public Graph getGraph();
}
