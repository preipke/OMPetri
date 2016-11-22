/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.node;

/**
 *
 * @author PR
 */
public class GravisNodeType
{
    private final NodeType type;
    private final String typeName;

    public GravisNodeType(NodeType type , String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public NodeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return typeName;
    }

    public enum NodeType
    {
        CIRCLE, RECTANGLE, DEFAULT;
    }
}
