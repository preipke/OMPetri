/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.dao.SelectionDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;

import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;

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
    
    private IGravisNode[] nodesCopy;
    private IGravisEdge[] edgeCopy;
    
    @Autowired
    public SelectionService(SelectionDao selectionDao) {
        this.selectionDao = selectionDao;
    }
    
    public void add(IGravisNode node) {
        selectionDao.add(node);
    }
    
    public void addAll(IGravisNode node) {
        IDataNode graphNode = (IDataNode) node.getRelatedObject();
        for (Object shape : graphNode.getShapes()) {
            selectionDao.add((IGravisNode)shape);
        }
    }
    
    public void add(IGravisEdge edge) {
        selectionDao.add(edge);
    }
    
    public void copy() {
        nodesCopy = selectionDao.getSelectedNodesArray();
        edgeCopy = selectionDao.getSelectedEdgesArray();
    }
    
    public boolean contains(IGravisNode node) {
        return selectionDao.contains(node);
    }
    
    public void clear() {
        selectionDao.clear();
    }
    
    public List<IGravisEdge> getEdges() {
        return selectionDao.getSelectedEdges();
    }
    
    public List<IGravisNode> getNodes() {
        return selectionDao.getSelectedNodes();
    }
    
    public IGravisEdge[] getEdgesCopy() {
        return edgeCopy;
    }
    
    public IGravisNode[] getNodesCopy() {
        return nodesCopy;
    }
    
    public boolean remove(IGravisEdge node) {
        return selectionDao.remove(node);
    }
    
    public boolean remove(IGravisNode node) {
        return selectionDao.remove(node);
    }
}
