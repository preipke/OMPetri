/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.graph.entity.parent.node.GravisCircleDouble;

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
        
        getLabel().setText(getId());
    }
    
    @Override
    public DataPlace getDataElement() {
        return dataPlace;
    }
}
