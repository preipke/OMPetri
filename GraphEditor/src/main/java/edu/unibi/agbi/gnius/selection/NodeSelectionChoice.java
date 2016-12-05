/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.selection;

import edu.unibi.agbi.gnius.entity.GraphNode;

/**
 *
 * @author PR
 */
public class NodeSelectionChoice
{
    private final GraphNode.Type type;
    private final String typeName;

    public NodeSelectionChoice(GraphNode.Type type , String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public GraphNode.Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
