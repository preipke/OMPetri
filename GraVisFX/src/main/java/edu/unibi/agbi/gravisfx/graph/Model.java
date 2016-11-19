/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.graph.entity.node.GravisEdge;
import edu.unibi.agbi.gravisfx.graph.entity.node.IGravisNode;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author PR
 */
public class Model
{
    private final Map<String, IGravisNode> nodes = new HashMap();
    
    public boolean addNode(IGravisNode node) {
        if (nodes.containsKey(node.getId())) {
            return false;
        } else {
            nodes.put(node.getId() , node);
            return true;
        }
    }
    
    public boolean removeNode(IGravisNode node) {
        node = nodes.remove(node.getId());
        return node != null;
    }
    
    public boolean connectNodes(IGravisNode parent, IGravisNode child) {
        
        if (parent != null && child != null) {
            parent.addChildNode(child);
            child.addParentNode(parent);
            return true;
        }
        return false;
    }
    
    public boolean connectNodes(String parentId, String childId) {
        
        IGravisNode parent = nodes.get(parentId);
        IGravisNode child = nodes.get(childId);
        
        return connectNodes(parent, child);
    }
    
    public void disconnectNodes(IGravisNode parent, IGravisNode child) {
        
        parent.getChildren().remove(child);
        child.getParents().remove(parent);
    }
    
    public void disconnectNodes(GravisEdge edge) {
        
        disconnectNodes(edge.getSource(), edge.getTarget());
    }
    
    public void disconnectNodes(String parentId, String childId) {
        
        IGravisNode parent = nodes.get(parentId);
        IGravisNode child = nodes.get(childId);
        
        disconnectNodes(parent, child);
    }
    
    public IGravisNode getNode(String id) {
        return nodes.get(id);
    }
    
    public IGravisNode[] getNodes() {
        
        IGravisNode[] nodesArray = new IGravisNode[nodes.size()];
        
        int index = 0;
        for (Map.Entry<String , IGravisNode> entrySet : nodes.entrySet()) {
            nodesArray[index] = entrySet.getValue();
            index++;
        }
        return nodesArray;
    }
}
