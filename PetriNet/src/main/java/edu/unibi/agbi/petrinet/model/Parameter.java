/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import edu.unibi.agbi.petrinet.entity.IElement;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author PR
 */
public class Parameter implements Comparable
{
    private final String id;
    private final String name;
    private final String value;
    private final String note;
    private final Type type;

    private final Set<IElement> referingNodes;

    public Parameter(String id, String name, String note, String value, Type type) {
        this.id = id;
        this.name = name;
        this.note = note;
        this.value = value;
        this.type = type;
        this.referingNodes = new HashSet();
    }

    /**
     * Gets the identifier for this parameter.
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the name of this parameter.
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the note set to describe this parameter.
     * @return 
     */
    public String getNote() {
        return note;
    }

    /**
     * Gets the value set to this parameter.
     * 
     * @return 
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the type indicating wether this parameter is used in a local or
     * global scope.
     *
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the set of nodes currently using this parameter.
     *
     * @return
     */
    public Set<IElement> getReferingNodes() {
        return referingNodes;
    }

    /**
     * Compares the name strings of two parameters lexicographically.
     * Uses the compareTo method for strings.
     * 
     * @param o
     * @return 
     */
    @Override
    public int compareTo(Object o) {
        Parameter param = (Parameter) o;
        return this.toString().compareTo(param.toString());
    }

    public enum Type
    {
        LOCAL, GLOBAL;
    }
}
