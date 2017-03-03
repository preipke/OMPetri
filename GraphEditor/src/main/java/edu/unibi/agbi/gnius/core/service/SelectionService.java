/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.model.dao.SelectionDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;

import java.util.ArrayList;
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
    
    private List<IGraphNode> nodesCopy;
    
    @Autowired
    public SelectionService(SelectionDao selectionDao) {
        this.selectionDao = selectionDao;
    }
    
    /**
     * Copy and store selected nodes and arcs.
     */
    public void copy() {
        
        nodesCopy = new ArrayList();
        
        for (IGraphElement element : getSelectedElements()) {
            try {
                nodesCopy.add((IGraphNode)element);
            } catch (Exception ex) {
                System.out.println("Element not a node!");
                System.out.println(ex.toString());
            }
        }
    }
    
    /**
     * Clear selected and highlighted elements.
     */
    public void unselectAll() {
        for (IGraphElement selected : selectionDao.getSelectedElements()) {
            selected.getElementHandles().forEach(ele -> {
                ele.setSelected(false);
            });
        }
        for (IGraphElement hightlighted : selectionDao.getHightlightedElements()) {
            hightlighted.getElementHandles().forEach(ele -> {
                ele.setHighlighted(false);
            });
        }
        selectionDao.clear();
    }
    
    /**
     * Highlight the given element.
     * @param element 
     */
    public void highlight(IGraphElement element) {
        element.getElementHandles().forEach(ele -> {
            ele.setHighlighted(true);
        });
        selectionDao.addHighlight(element);
    }
    
    /**
     * Highlight related objects. Only if those are not already selected.
     * @param element 
     */
    public void highlightRelated(IGraphElement element) {
        IDataElement dataElement = element.getRelatedDataElement();
        for (IGraphElement shape : dataElement.getShapes()) {
            if (!shape.getElementHandles().get(0).isSelected()) {
                if (!shape.getElementHandles().get(0).isHighlighted()) {
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
        element.getElementHandles().forEach(ele -> {
            ele.setHighlighted(false);
        });
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
            if (relatedShape.getElementHandles().get(0).isSelected()) {
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
        if (element.getElementHandles().get(0).isHighlighted()) {
            selectionDao.removeHighlight(element);
            element.getElementHandles().forEach(ele -> {
                ele.setHighlighted(false);
            });
        }
        if (!element.getElementHandles().get(0).isSelected()) {
            selectionDao.addSelection(element);
            element.getElementHandles().forEach(ele -> {
                ele.setSelected(true);
            });
        }
        element.getElementHandles().forEach(ele -> {
            ele.putOnTop();
        });
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
        arc.getElementHandles().forEach(ele -> {
            ele.setSelected(false);
        });
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
     * Get copied nodes.
     * @return 
     */
    public List<IGraphNode> getNodesCopy() {
        return nodesCopy;
    }
}
