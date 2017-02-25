/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.exception.AssignmentDeniedException;
import edu.unibi.agbi.gravisfx.graph.entity.GravisCircle;

/**
 *
 * @author PR
 */
public class GraphPlace extends GravisCircle implements IGraphNode
{
    private DataPlace dataElement;
    
    public GraphPlace() {
        super();
    }
    
    public GraphPlace(IDataNode dataNode) throws AssignmentDeniedException {
        this();
        if (!(dataNode instanceof DataPlace)) {
            throw new AssignmentDeniedException("Must assign a place! Action denied.");
        }
        this.dataElement = (DataPlace) dataNode;
    }

    @Override
    public void setRelatedElement(IDataNode dataNode) throws AssignmentDeniedException {
        if (this.dataElement != null) {
            throw new AssignmentDeniedException("Related place has already been assigned! Change denied.");
        } else if (!(dataNode instanceof DataPlace)) {
            throw new AssignmentDeniedException("Must assign a place! Action denied.");
        }
        this.dataElement = (DataPlace) dataNode;
    }
    
    @Override
    public DataPlace getRelatedDataNode() {
        return dataElement;
    }
    
    @Override
    public IDataElement getRelatedDataElement() {
        return getRelatedDataNode();
    }
}
