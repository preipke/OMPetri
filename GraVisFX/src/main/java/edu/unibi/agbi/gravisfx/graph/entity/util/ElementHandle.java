/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.util;

import edu.unibi.agbi.gravisfx.graph.entity.IGravisElement;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.Group;

/**
 * Helper class for handling element styling. The defined pseudo classes can
 * be used as identifiers within stylesheets. 
 * @author PR
 */
public class ElementHandle
{
    private static final String PSEUDO_CLASS_SELECTED_IDENT = "selected";
    private static final String PSEUDO_CLASS_HIGHLIGHT_IDENT = "highlighted";
    private static final String PSEUDO_CLASS_HOVER_IDENT = "hovered";
    
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass(PSEUDO_CLASS_SELECTED_IDENT);
    private static final PseudoClass PSEUDO_CLASS_HIGHLIGHT = PseudoClass.getPseudoClass(PSEUDO_CLASS_HIGHLIGHT_IDENT);
    private static final PseudoClass PSEUDO_CLASS_HOVER = PseudoClass.getPseudoClass(PSEUDO_CLASS_HOVER_IDENT);
    
    private final IGravisElement element;
    
    private String activeStyleClass;
    
    public ElementHandle(IGravisElement element) {
        this.element = element;
    }
    
    public IGravisElement getElement() {
        return element;
    }
    
    private final BooleanProperty isSelected = new BooleanPropertyBase(false) {
        @Override 
        protected void invalidated() {
            element.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED , get());
        }
        @Override
        public Object getBean() {
            return element.getBean();
        }
        @Override
        public String getName() {
            return PSEUDO_CLASS_SELECTED_IDENT;
        }
    };
    
    private final BooleanProperty isHighlighted = new BooleanPropertyBase(false) {
        @Override 
        protected void invalidated() {
            element.pseudoClassStateChanged(PSEUDO_CLASS_HIGHLIGHT , get());
        }
        @Override
        public Object getBean() {
            return element.getBean();
        }
        @Override
        public String getName() {
            return PSEUDO_CLASS_HIGHLIGHT_IDENT;
        }
    };
    
    private final BooleanProperty isHovered = new BooleanPropertyBase(false) {
        @Override 
        protected void invalidated() {
            element.pseudoClassStateChanged(PSEUDO_CLASS_HOVER , get());
        }
        @Override
        public Object getBean() {
            return element.getBean();
        }
        @Override
        public String getName() {
            return PSEUDO_CLASS_HOVER_IDENT;
        }
    };

    public String getActiveStyleClass() {
        return activeStyleClass;
    }

    public void setActiveStyleClass(String name) {
        
        element.getStyleClass().remove(activeStyleClass);
        element.getStyleClass().add(name);

        activeStyleClass = name;
    }

    public void setSelected(boolean value) {
        isSelected.set(value);
    }
    
    public void setHighlighted(boolean value) {
        isHighlighted.set(value);
    }
    
    public void setHovered(boolean value) {
        isHovered.set(value);
    }
    
    public boolean isSelected() {
        return isSelected.get();
    }
    
    public boolean isHighlighted() {
        return isHighlighted.get();
    }
    
    public boolean isHovered() {
        return isHovered.get();
    }
    
    public void putOnTop() {
        
        Group layer = (Group) element.getParent();

        layer.getChildren().remove(element.getShape());
        layer.getChildren().add(element.getShape());
    }
}
