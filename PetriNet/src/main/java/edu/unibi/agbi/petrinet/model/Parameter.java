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
    private final StringProperty unit;
    private final Type type;

    private final String elementIdRelated;
    private final Set<IElement> elementsUsing;

    /**
     * Default constructor.
     *
     * @param id               identifier for this parameter
     * @param unit             an optional note
     * @param value            the parameter value
     * @param type             the type or scope of this parameter
     * @param elementIdRelated the ID of the related element in case this is
     *                         parameter is of type reference
     */
    public Parameter(String id, String unit, String value, Type type, String elementIdRelated) {
        this.id = id;
        this.unit = new SimpleStringProperty(unit);
        this.value = new SimpleStringProperty(value);
        this.type = type;
        this.elementIdRelated = elementIdRelated;
        this.elementsUsing = new HashSet();
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
     * Sets the unit for this parameter.
     *
     * @param note
     */
    public void setUnit(String note) {
        this.unit.set(note);
    }

    /**
     * Gets the unit for this parameter.
     *
     * @return
     */
    public String getUnit() {
        return unit.get();
    }

    /**
     * Gets the note string property.
     *
     * @return
     */
    public StringProperty getUnitProperty() {
        return unit;
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
     * Gets the indicator for the parameter's type and scope.
     *
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the ID for the element this parameter is related to.
     *
     * @return
     */
    public String getRelatedElementId() {
        return elementIdRelated;
    }

    /**
     * Gets the set of nodes currently using this parameter.
     *
     * @return
     */
    public Set<IElement> getUsingElements() {
        return elementsUsing;
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
