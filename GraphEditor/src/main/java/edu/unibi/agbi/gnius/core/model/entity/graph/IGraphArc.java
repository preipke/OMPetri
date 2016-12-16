/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.service.exception.AssignmentDeniedException;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisEdge;
import javafx.beans.property.DoubleProperty;

/**
 *
 * @author PR
 */
public interface IGraphArc extends IGraphElement , IGravisEdge
{
    public IDataArc getRelatedDataArc();
    public void setRelatedElement(IDataArc dataNode) throws AssignmentDeniedException;
    public void setTarget(IGraphNode target) throws AssignmentDeniedException;
    public DoubleProperty endXProperty();
    public DoubleProperty endYProperty();
    @Override public IGraphNode getSource();
    @Override public IGraphNode getTarget();
}
