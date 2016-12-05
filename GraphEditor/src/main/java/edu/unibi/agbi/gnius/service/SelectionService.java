/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.service;

import edu.unibi.agbi.gnius.dao.SelectionDao;

import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.node.IGravisSelectable;

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
    @Autowired
    private SelectionDao selectionDao;
    
    private IGravisNode[] nodesCopy;
    private IGravisEdge[] edgeCopy;
    
    public void add(IGravisNode node) {
        selectionDao.add(node);
    }
    
    public void add(IGravisEdge edge) {
        selectionDao.add(edge);
    }
    
    public void copy() {
        nodesCopy = selectionDao.getSelectedNodesArray();
        edgeCopy = selectionDao.getSelectedEdgesArray();
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
