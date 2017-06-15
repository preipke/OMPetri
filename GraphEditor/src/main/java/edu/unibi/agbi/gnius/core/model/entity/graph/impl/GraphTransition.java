/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.entity.parent.node.GravisRectangle;

/**
 *
 * @author PR
 */
public class GraphTransition extends GravisRectangle implements IGraphNode {
    
    private final DataTransition dataTransition;
    
    public GraphTransition(String id, DataTransition dataTransition) {
        super(id);
        this.dataTransition = dataTransition;
        this.dataTransition.getShapes().add(this);
    }
    
    @Override
    public DataTransition getDataElement() {
        return dataTransition;
    }
}
