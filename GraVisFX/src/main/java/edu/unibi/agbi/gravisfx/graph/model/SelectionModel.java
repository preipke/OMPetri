/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.model;

import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.node.IGravisSelectable;

import java.util.ArrayList;
import java.util.List;

/**
 * Model used to store currently selected nodes within the graph.
 * @author PR
 */
public class SelectionModel
{
    private List<IGravisNode> selectedNodes;
    private List<IGravisEdge> selectedEdges;
    private List<IGravisSelectable> selectedSelectables;
    
    public SelectionModel() {
        selectedNodes = new ArrayList();
        selectedEdges = new ArrayList();
        selectedSelectables = new ArrayList();
    }
    
    public synchronized void add(IGravisNode node) {
        selectedNodes.add(node);
        node.setHighlight(true);
        node.putOnTop();
    }
    
    public synchronized void add(IGravisEdge edge) {
        selectedEdges.add(edge);
        edge.setHighlight(true);
        edge.putOnTop();
    }
    
    public synchronized void add(IGravisSelectable node) {
        selectedSelectables.add(node);
        node.setHighlight(true);
        node.putOnTop();
    }
    
    public synchronized void add(List<IGravisNode> nodes) {
        for (IGravisNode node : nodes) {
            this.add(node);
        }
    }
    
    public synchronized List<IGravisNode> getSelectedNodes() {
        return selectedNodes;
    }
    
    public synchronized List<IGravisEdge>  getSelectedEdges() {
        return selectedEdges;
    }
    
    public synchronized IGravisNode[] getSelectedNodesArray() {
        IGravisNode[] nodes = new IGravisNode[selectedNodes.size()];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = selectedNodes.get(i);
        }
        return nodes;
    }
    
    public synchronized IGravisEdge[]  getSelectedEdgesArray() {
        IGravisEdge[] edges = new IGravisEdge[selectedEdges.size()];
        for (int i = 0; i < edges.length; i++) {
            edges[i] = selectedEdges.get(i);
        }
        return edges;
    }
    
    public synchronized IGravisSelectable[]  getSelectedSelectables() {
        IGravisSelectable[] selectables = new IGravisSelectable[selectedSelectables.size()];
        for (int i = 0; i < selectables.length; i++) {
            selectables[i] = selectedSelectables.get(i);
        }
        return selectables;
    }
    
    public synchronized void remove(IGravisNode node) {
        selectedNodes.remove(node);
        node.setHighlight(false);
    }
    
    public synchronized void remove(IGravisEdge edge) {
        selectedEdges.remove(edge);
        edge.setHighlight(false);
    }
    
    public synchronized void remove(IGravisSelectable edge) {
        selectedSelectables.remove(edge);
        edge.setHighlight(false);
    }
    
    public synchronized boolean contains(IGravisNode node) {
        return selectedNodes.contains(node);
    }
    
    public synchronized boolean contains(IGravisEdge edge) {
        return selectedEdges.contains(edge);
    }
    
    public synchronized boolean contains(IGravisSelectable edge) {
        return selectedSelectables.contains(edge);
    }
    
    public synchronized void clear() {
        for (IGravisNode selected : selectedNodes) {
            selected.setHighlight(false);
        }
        selectedNodes = new ArrayList();
        
        for (IGravisEdge selected : selectedEdges) {
            selected.setHighlight(false);
        }
        selectedEdges = new ArrayList();
        
        for (IGravisSelectable selected : selectedSelectables) {
            selected.setHighlight(false);
        }
        selectedSelectables = new ArrayList();
    }
}
