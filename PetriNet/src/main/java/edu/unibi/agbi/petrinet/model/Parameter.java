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
public class Parameter
{
    private final String id;
    private final Type type;
    
    private String value;
    private String unit;

    private final IElement element;
    private final Set<IElement> elementsUsing;

    /**
     * Default constructor.
     *
     * @param id      identifier for this parameter
     * @param unit    an optional note
     * @param value   the parameter value
     * @param type    the type or scope of this parameter
     * @param element the related element in case this is parameter is of type
     *                local or reference
     */
    public Parameter(String id, String value, String unit, Type type, IElement element) {
        this.id = id;
        this.unit = unit;
        this.value = value;
        this.type = type;
        this.element = element;
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
     * @param unit
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Gets the unit for this parameter.
     *
     * @return
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the value for this parameter.
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
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
    public IElement getRelatedElement() {
        return element;
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
    public String toString() {
        if (unit != null) {
            return id + " = " + getValue() + " [" + getUnit() + "]";
        } else {
            return id + " = " + getValue();
        }
    }

    public enum Type
    {
        LOCAL, GLOBAL, REFERENCE;
    }
}
