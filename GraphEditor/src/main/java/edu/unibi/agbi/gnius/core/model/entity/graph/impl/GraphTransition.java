/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.entity.parent.node.GravisRectangleDouble;

/**
 *
 * @author PR
 */
public class GraphTransition extends GravisRectangleDouble implements IGraphNode {
    
    private final DataTransition dataTransition;
    
    public GraphTransition(DataTransition dataTransition) {
        super();
        this.dataTransition = dataTransition;
    }
    
    @Override
    public DataTransition getDataElement() {
        return dataTransition;
    }
}
