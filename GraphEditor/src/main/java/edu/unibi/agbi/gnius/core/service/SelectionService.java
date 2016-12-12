/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.dao.SelectionDao;
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
        arcCopy = new IGraphArc[selectionDao.getSelectedArcs().size()];
        arcCopy = selectionDao.getSelectedArcs().toArray(arcCopy);
        nodesCopy = new IGraphNode[selectionDao.getSelectedNodes().size()];
        nodesCopy = selectionDao.getSelectedNodes().toArray(nodesCopy);
    }
    
    /**
     * Clear selected and hightlighted elements.
     */
    public void clear() {
        for (IGraphArc arc : selectionDao.getSelectedArcs()) {
            arc.setSelected(false);
        }
        for (IGraphNode node : selectionDao.getSelectedNodes()) {
            node.setSelected(false);
        }
        for (IGraphElement hightlighted : selectionDao.getHightlightedElements()) {
            hightlighted.setHighlighted(false);
        }
        selectionDao.clear();
    }
    
    /**
     * Get selected arcs.
     * @return 
     */
    public List<IGraphArc> getSelectedArcs() {
        return selectionDao.getSelectedArcs();
    }
    
    /**
     * Get selected nodes.
     * @return 
     */
    public List<IGraphNode> getSelectedNodes() {
        return selectionDao.getSelectedNodes();
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
    
    /**
     * Highlight the given element.
     * @param element 
     */
    public void highlight(IGraphElement element) {
        selectionDao.add(element);
        element.setHighlighted(true);
    }
    
    /**
     * Highlight related objects. Only if those are not already selected.
     * @param node 
     */
    public void hightlightRelated(IGraphNode node) {
        IDataNode graphNode = node.getRelatedDataNode();
        for (IGraphNode relatedNode : graphNode.getShapes()) {
            if (!selectionDao.contains(relatedNode)) {
                highlight(relatedNode);
            }
        }
    }
    
    /**
     * Test wether node is already selected or not.
     * @param node
     * @return 
     */
    public boolean isSelected(IGraphNode node) {
        return selectionDao.contains(node);
    }
    
    /**
     * Remove arc selection.
     * @param arc
     * @return 
     */
    public boolean remove(IGraphArc arc) {
        arc.setSelected(false);
        return selectionDao.remove(arc);
    }
    
    /**
     * Remove node selection.
     * @param node
     * @return 
     */
    public boolean remove(IGraphNode node) {
        node.setSelected(false);
        return selectionDao.remove(node);
    }
    
    /**
     * Remove element highlight.
     * @param element
     * @return 
     */
    public boolean remove(IGraphElement element) {
        element.setHighlighted(false);
        return selectionDao.removeHighlight(element);
    }
    
    /**
     * Select arc.
     * @param arc 
     */
    public void select(IGraphArc arc) {
        if (selectionDao.removeHighlight(arc)) {
            arc.setHighlighted(false);
        }
        selectionDao.add(arc);
        arc.setSelected(true);
        arc.putOnTop();
    }
    
    /**
     * Select node.
     * @param node 
     */
    public void select(IGraphNode node) {
        if (selectionDao.removeHighlight(node)) {
            node.setHighlighted(false);
        }
        selectionDao.add(node);
        node.setSelected(true);
        node.putOnTop();
    }
    
    /**
     * Select node and all related.
     * @param node 
     */
    public void selectAll(IGraphNode node) {
        IDataNode dataNode = node.getRelatedDataNode();
        for (IGraphNode relatedNode : dataNode.getShapes()) {
            select(relatedNode);
        }
    }
}
