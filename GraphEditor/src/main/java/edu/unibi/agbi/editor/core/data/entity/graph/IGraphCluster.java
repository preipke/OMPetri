/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.data.entity.graph;

import edu.unibi.agbi.editor.core.data.entity.data.impl.DataCluster;
import edu.unibi.agbi.gravisfx.entity.root.node.IGravisCluster;

/**
 *
 * @author PR
 */
public interface IGraphCluster extends IGraphNode, IGravisCluster
{
    @Override public DataCluster getData();
}
