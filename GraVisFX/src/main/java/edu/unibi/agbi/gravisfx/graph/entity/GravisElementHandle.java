/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisElementHandle
{
    
    private String activeStyleClass;
    
    private static final String PSEUDO_CLASS_SELECTED_IDENT = "selected";
    private static final String PSEUDO_CLASS_HIGHLIGHT_IDENT = "highlighted";
    
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass(PSEUDO_CLASS_SELECTED_IDENT);
    private static final PseudoClass PSEUDO_CLASS_HIGHLIGHT = PseudoClass.getPseudoClass(PSEUDO_CLASS_HIGHLIGHT_IDENT);
    
    private final BooleanProperty isSelected = new BooleanPropertyBase(false) {
        @Override 
        protected void invalidated() {
            pseudoClassStateChanged(PSEUDO_CLASS_SELECTED , get());
        }
        @Override
        public Object getBean() {
            return GraphCurve.this;
        }
        @Override
        public String getName() {
            return PSEUDO_CLASS_SELECTED_IDENT;
        }
    };
    
    private final BooleanProperty isHighlighted = new BooleanPropertyBase(false) {
        @Override 
        protected void invalidated() {
            pseudoClassStateChanged(PSEUDO_CLASS_HIGHLIGHT , get());
        }
        @Override
        public Object getBean() {
            return GraphCurve.this;
        }
        @Override
        public String getName() {
            return PSEUDO_CLASS_HIGHLIGHT_IDENT;
        }
    };

    @Override
    public String getActiveStyleClass() {
        return activeStyleClass;
    }

    @Override
    public void setActiveStyleClass(String name) {
        for (Shape shape : getShapes()) {
            shape.getStyleClass().remove(activeStyleClass);
            shape.getStyleClass().add(name);
        }
        activeStyleClass = name;
    }

    @Override
    public void setSelected(boolean value) {
        isSelected.set(value);
    }
    
    @Override
    public void setHighlighted(boolean value) {
        isHighlighted.set(value);
    }
    
    @Override
    public boolean isSelected() {
        return isSelected.get();
    }
    
    @Override
    public boolean isHighlighted() {
        return isHighlighted.get();
    }
}
