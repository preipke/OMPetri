/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import edu.unibi.agbi.petrinet.entity.IElement;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author PR
 */
public class Parameter
{
    private final String id;
    private final StringProperty value;
    private final StringProperty note;
    private final Type type;

    private final Set<IElement> referingNodes;

    public Parameter(String id, String note, String value, Type type) {
        this.id = id;
        this.note = new SimpleStringProperty(note);
        this.value = new SimpleStringProperty(value);
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
     * Sets the note for this parameter.
     *
     * @param note
     */
    public void setNote(String note) {
        this.note.set(note);
    }

    /**
     * Gets the note for this parameter.
     *
     * @return
     */
    public String getNote() {
        return note.get();
    }

    /**
     * Gets the note string property.
     *
     * @return
     */
    public StringProperty getNoteProperty() {
        return note;
    }

    /**
     * Sets the value for this parameter.
     *
     * @param value
     */
    public void setValue(String value) {
        this.value.set(value);
    }

    /**
     * Gets the value set to this parameter.
     *
     * @return
     */
    public String getValue() {
        return value.get();
    }

    /**
     * Gets the value string property.
     *
     * @return
     */
    public StringProperty getValueProperty() {
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
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Parameter)) {
            return false;
        }
        Parameter param = (Parameter) obj;
        if (!param.getId().contentEquals(id)) {
            return false;
        }
        if (!param.getValue().contentEquals(getValue())) {
            return false;
        }
        return param.getType() == type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
        hash = 97 * hash + Objects.hashCode(this.value);
        hash = 97 * hash + Objects.hashCode(this.type);
        return hash;
    }

    public enum Type
    {
        LOCAL, GLOBAL, REFERENCE;
    }
}
