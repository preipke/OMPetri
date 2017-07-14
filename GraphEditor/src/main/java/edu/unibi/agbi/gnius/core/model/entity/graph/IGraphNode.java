/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;

/**
 *
 * @author PR
 */
public interface IGraphNode extends IGraphElement, IGravisNode
{
    @Override public IDataNode getData();
}
