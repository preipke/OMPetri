/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.graph.entity.node.GravisNode;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author PR
 */
public class Model
{
    private final Map<String, GravisNode> nodes = new HashMap();
    
    public boolean addNode(GravisNode node) {
        if (nodes.containsKey(node.getId())) {
            return false;
        } else {
            nodes.put(node.getId() , node);
            return true;
        }
    }
    
    public boolean removeNode(GravisNode node) {
        node = nodes.remove(node.getId());
        return node != null;
    }
    
    public GravisNode getNode(String id) {
        return nodes.get(id);
    }
    
    public GravisNode[] getNodes() {
        GravisNode[] nodesArray = new GravisNode[nodes.size()];
        
        int index = 0;
        for (Map.Entry<String , GravisNode> entrySet : nodes.entrySet()) {
            nodesArray[index] = entrySet.getValue();
            index++;
        }
        return nodesArray;
    }
    
    public void connectNodes(GravisNode parent, GravisNode child) {
        nodes.get(parent.getId()).addChildNode(child);
    }
    
    public void connectNodes(String parentId, String childId) {
        // TODO check for null
        nodes.get(parentId).addChildNode(nodes.get(childId));
    }
}
