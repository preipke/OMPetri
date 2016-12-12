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
    private IGraphArc[] edgeCopy;
    
    @Autowired
    public SelectionService(SelectionDao selectionDao) {
        this.selectionDao = selectionDao;
    }
    
    public void add(IGraphNode node) {
        selectionDao.add(node);
        node.setHighlight(true);
        node.putOnTop();
    }
    
    public void addAll(IGraphNode node) {
        IDataNode graphNode = node.getRelatedDataNode();
        for (IGraphNode shape : graphNode.getShapes()) {
            add(shape);
        }
    }
    
    public void add(IGraphElement selectable) {
        selectionDao.add(selectable);
        selectable.setHighlight(true);
        selectable.putOnTop();
    }
    
    public void add(IGraphArc arc) {
        selectionDao.add(arc);
        arc.setHighlight(true);
        arc.putOnTop();
    }
    
    public void copy() {
        nodesCopy = new IGraphNode[selectionDao.getSelectedNodes().size()];
        nodesCopy = selectionDao.getSelectedNodes().toArray(nodesCopy);
        edgeCopy = new IGraphArc[selectionDao.getSelectedArcs().size()];
        edgeCopy = selectionDao.getSelectedArcs().toArray(edgeCopy);
    }
    
    public boolean contains(IGraphNode node) {
        return selectionDao.contains(node);
    }
    
    public void clear() {
        for (IGraphArc arc : selectionDao.getSelectedArcs()) {
            arc.setHighlight(false);
        }
        for (IGraphNode node : selectionDao.getSelectedNodes()) {
            node.setHighlight(false);
        }
        for (IGraphElement selectable : selectionDao.getSelectedSelectables()) {
            selectable.setHighlight(false);
        }
        selectionDao.clear();
    }
    
    public List<IGraphArc> getEdges() {
        return selectionDao.getSelectedArcs();
    }
    
    public List<IGraphNode> getNodes() {
        return selectionDao.getSelectedNodes();
    }
    
    public IGraphArc[] getEdgesCopy() {
        return edgeCopy;
    }
    
    public IGraphNode[] getNodesCopy() {
        return nodesCopy;
    }
    
    public boolean remove(IGraphArc arc) {
        arc.setHighlight(false);
        return selectionDao.remove(arc);
    }
    
    public boolean remove(IGraphNode node) {
        node.setHighlight(false);
        return selectionDao.remove(node);
    }
    
    public boolean remove(IGraphElement selectable) {
        selectable.setHighlight(false);
        return selectionDao.remove(selectable);
    }
}
