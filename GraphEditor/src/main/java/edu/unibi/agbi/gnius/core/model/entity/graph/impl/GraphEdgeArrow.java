/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.exception.AssignmentDeniedException;
import edu.unibi.agbi.gravisfx.graph.entity.GravisEdgeArrow;

/**
 *
 * @author PR
 */
public class GraphEdgeArrow extends GravisEdgeArrow implements IGraphArc
{
    private DataArc dataArc;
    
    public GraphEdgeArrow(IGraphNode source, IGraphNode target) {
        super(source , target);
    }
    
    public GraphEdgeArrow(IGraphNode source, IGraphNode target, IDataArc dataArc) throws AssignmentDeniedException {
        this(source , target);
        if (!(dataArc instanceof DataArc)) {
            throw new AssignmentDeniedException("Must assign an arc! Action denied.");
        }
        this.dataArc = (DataArc) dataArc;
    }

    @Override
    public void setRelatedElement(IDataArc dataArc) throws AssignmentDeniedException {
        if (this.dataArc != null) {
            throw new AssignmentDeniedException("Related arc has already been assigned! Action denied.");
        } else if (!(dataArc instanceof DataArc)) {
            throw new AssignmentDeniedException("Must assign an arc! Action denied.");
        }
        this.dataArc = (DataArc) dataArc;
    }
    
    @Override
    public DataArc getRelatedDataArc() {
        return dataArc;
    }
    
    @Override
    public IDataElement getRelatedDataElement() {
        return getRelatedDataArc();
    }

    @Override
    public IGraphNode getSource() {
        return (IGraphNode) super.getSource();
    }

    @Override
    public IGraphNode getTarget() {
        return (IGraphNode) super.getTarget();
    }
}
