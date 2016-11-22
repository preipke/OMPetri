/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.layout;

/**
 *
 * @author PR
 */
public class GravisLayoutType
{
    private final LayoutType type;
    private final String typeName;

    public GravisLayoutType(LayoutType type , String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public LayoutType getType() {
        return type;
    }

    @Override
    public String toString() {
        return typeName;
    }

    public enum LayoutType
    {
        RANDOM, DEFAULT;
    }
}
