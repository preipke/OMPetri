/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

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
    
    public void connectNodes(IGravisNode parent, IGravisNode child) {
        nodes.get(parent.getId()).addChildNode(child);
    }
    
    public void connectNodes(String parentId, String childId) {
        // TODO check for null
        nodes.get(parentId).addChildNode(nodes.get(childId));
    }
}
