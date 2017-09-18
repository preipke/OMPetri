/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gravisfx.entity.root.connection.IGravisConnection;

/**
 *
 * @author PR
 */
public interface IGraphArc extends IGraphElement, IGravisConnection
{
    @Override public IDataArc getData();
    @Override public IGraphNode getSource();
    @Override public IGraphNode getTarget();
}
