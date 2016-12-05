/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.selection;

/**
 *
 * @author PR
 */
public class LayoutSelectionChoice
{
    private final Type type;
    private final String typeName;

    public LayoutSelectionChoice(Type type , String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return typeName;
    }

    public enum Type
    {
        RANDOM, DEFAULT;
    }
}
