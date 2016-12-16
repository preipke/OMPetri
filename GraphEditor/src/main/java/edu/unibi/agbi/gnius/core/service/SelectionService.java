/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.dao.SelectionDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class SelectionService
{
    private final SelectionDao selectionDao;
    
    private IGraphNode[] nodesCopy;
    private IGraphArc[] arcCopy;
    
    @Autowired
    public SelectionService(SelectionDao selectionDao) {
        this.selectionDao = selectionDao;
    }
    
    /**
     * Copy and store selected nodes and arcs.
     */
    public void copy() {
//        arcCopy = new IGraphArc[selectionDao.getSelectedArcs().size()];
//        arcCopy = selectionDao.getSelectedArcs().toArray(arcCopy);
//        nodesCopy = new IGraphNode[selectionDao.getSelectedNodes().size()];
//        nodesCopy = selectionDao.getSelectedNodes().toArray(nodesCopy);
    }
    
    /**
     * Clear selected and highlighted elements.
     */
    public void unselectAll() {
        for (IGraphElement selected : selectionDao.getSelectedElements()) {
            selected.setSelected(false);
        }
        for (IGraphElement hightlighted : selectionDao.getHightlightedElements()) {
            hightlighted.setHighlighted(false);
        }
        selectionDao.clear();
    }
    
    /**
     * Highlight the given element.
     * @param element 
     */
    public void highlight(IGraphElement element) {
        element.setHighlighted(true);
        selectionDao.addHighlight(element);
    }
    
    /**
     * Highlight related objects. Only if those are not already selected.
     * @param element 
     */
    public void highlightRelated(IGraphElement element) {
        IDataElement dataElement = element.getRelatedDataElement();
        for (IGraphElement shape : dataElement.getShapes()) {
            if (!shape.isSelected()) {
                if (!shape.isHighlighted()) {
                    highlight(shape);
                }
            }
        }
    }
    
    /**
     * Remove element highlight.
     * @param element
     * @return 
     */
    public boolean unhighlight(IGraphElement element) {
        element.setHighlighted(false);
        return selectionDao.removeHighlight(element);
    }
    
    /**
     * Removes element highlight from all related. Only if no other related
     * element is selected anymore.
     * @param element 
     */
    public void unhighlightRelated(IGraphElement element) {
        
        IDataElement dataElement = element.getRelatedDataElement();
        
        boolean isStillSelected = false;
        for (IGraphElement relatedShape : dataElement.getShapes()) {
            if (relatedShape.isSelected()) {
                isStillSelected = true;
                break;
            }
        }
        if (!isStillSelected) {
            for (IGraphElement relatedShape : dataElement.getShapes()) {
                unhighlight(relatedShape);
            }
        }
    }
    
    /**
     * Select element.
     * @param element 
     */
    public void select(IGraphElement element) {
        if (element.isHighlighted()) {
            selectionDao.removeHighlight(element);
            element.setHighlighted(false);
        }
        if (!element.isSelected()) {
            selectionDao.addSelection(element);
            element.setSelected(true);
        }
        element.putOnTop();
    }
    
    /**
     * Select element and all related.
     * @param element 
     */
    public void selectAll(IGraphElement element) {
        IDataElement dataElement = element.getRelatedDataElement();
        for (IGraphElement relatedElement : dataElement.getShapes()) {
            select(relatedElement);
        }
    }
    
    /**
     * Remove selection.
     * @param arc
     * @return 
     */
    public boolean unselect(IGraphElement arc) {
        arc.setSelected(false);
        return selectionDao.removeSelection(arc);
    }
    
    /**
     * Get selected elements.
     * @return 
     */
    public List<IGraphElement> getSelectedElements() {
        return selectionDao.getSelectedElements();
    }
    
    /**
     * Get copied arcs.
     * @return 
     */
    public IGraphArc[] getEdgesCopy() {
        return arcCopy;
    }
    
    /**
     * Get copied nodes.
     * @return 
     */
    public IGraphNode[] getNodesCopy() {
        return nodesCopy;
    }
}
