/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.dao;

import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 *
 * @author PR
 */
@Repository
public class SelectionDao
{
    private List<IGraphNode> selectedNodes;
    private List<IGraphArc> selectedEdges;
    private List<IGraphElement> selectedSelectables;
    
    public SelectionDao() {
        selectedNodes = new ArrayList();
        selectedEdges = new ArrayList();
        selectedSelectables = new ArrayList();
    }
    
    public synchronized void add(IGraphNode node) {
        selectedNodes.add(node);
    }
    
    public synchronized void add(IGraphArc edge) {
        selectedEdges.add(edge);
    }
    
    public synchronized void add(IGraphElement node) {
        selectedSelectables.add(node);
    }
    
    public synchronized boolean contains(IGraphNode node) {
        return selectedNodes.contains(node);
    }
    
    public synchronized boolean contains(IGraphArc edge) {
        return selectedEdges.contains(edge);
    }
    
    public synchronized boolean contains(IGraphElement edge) {
        return selectedSelectables.contains(edge);
    }
    
    public synchronized List<IGraphArc> getSelectedArcs() {
        return selectedEdges;
    }
    
    public synchronized List<IGraphNode> getSelectedNodes() {
        return selectedNodes;
    }
    
    public synchronized List<IGraphElement> getSelectedSelectables() {
        return selectedSelectables;
    }
    
    public synchronized IGraphNode[] getSelectedNodesArray() {
        IGraphNode[] nodes = new IGraphNode[selectedNodes.size()];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = selectedNodes.get(i);
        }
        return nodes;
    }
    
    public synchronized IGraphArc[] getSelectedEdgesArray() {
        IGraphArc[] edges = new IGraphArc[selectedEdges.size()];
        for (int i = 0; i < edges.length; i++) {
            edges[i] = selectedEdges.get(i);
        }
        return edges;
    }
    
    public synchronized IGraphElement[] getSelectedSelectablesArray() {
        IGraphElement[] selectables = new IGraphElement[selectedSelectables.size()];
        for (int i = 0; i < selectables.length; i++) {
            selectables[i] = selectedSelectables.get(i);
        }
        return selectables;
    }
    
    public synchronized boolean remove(IGraphArc arc) {
        return selectedEdges.remove(arc);
    }
    
    public synchronized boolean remove(IGraphNode node) {
        return selectedNodes.remove(node);
    }
    
    public synchronized boolean remove(IGraphElement selectable) {
        return selectedSelectables.remove(selectable);
    }
    
    public synchronized void clear() {
        selectedNodes = new ArrayList();
        selectedEdges = new ArrayList();
        selectedSelectables = new ArrayList();
    }
}
