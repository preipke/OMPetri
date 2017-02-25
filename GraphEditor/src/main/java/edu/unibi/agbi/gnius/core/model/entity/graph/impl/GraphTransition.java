/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.exception.AssignmentDeniedException;
import edu.unibi.agbi.gravisfx.graph.entity.GravisRectangle;

/**
 *
 * @author PR
 */
public class GraphTransition extends GravisRectangle implements IGraphNode
{
    private DataTransition dataTransition;
    
    public GraphTransition() {
        super();
    }
    
    public GraphTransition(IDataNode dataNode) throws AssignmentDeniedException {
        this();
        if (!(dataNode instanceof DataTransition)) {
            throw new AssignmentDeniedException("Must assign transition! Action denied.");
        }
        this.dataTransition = (DataTransition) dataNode;
    }

    @Override
    public void setRelatedElement(IDataNode dataNode) throws AssignmentDeniedException {
        if (this.dataTransition != null) {
            throw new AssignmentDeniedException("Related transition has already been assigned! Change denied.");
        } else if (!(dataNode instanceof DataTransition)) {
            throw new AssignmentDeniedException("Must assign a transition! Action denied.");
        }
        this.dataTransition = (DataTransition) dataNode;
    }
    
    @Override
    public DataTransition getRelatedDataNode() {
        return dataTransition;
    }
    
    @Override
    public IDataElement getRelatedDataElement() {
        return getRelatedDataNode();
    }
}
