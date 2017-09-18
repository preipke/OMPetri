/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.shape.Shape;
import edu.unibi.agbi.gravisfx.entity.IGravisItem;

/**
 * Helper class for handling element styling. The defined pseudo classes can be
 * used as identifiers within css.
 *
 * @author PR
 */
public class GravisShapeHandle {
    private static final String PSEUDO_CLASS_DISABLED_IDENT = "disabled";
    private static final String PSEUDO_CLASS_HIGHLIGHTED_IDENT = "highlighted";
    private static final String PSEUDO_CLASS_HOVERED_IDENT = "hovered";
    private static final String PSEUDO_CLASS_SELECTED_IDENT = "selected";

    private static final PseudoClass PSEUDO_CLASS_DISABLED = PseudoClass.getPseudoClass(PSEUDO_CLASS_DISABLED_IDENT);
    private static final PseudoClass PSEUDO_CLASS_HIGHLIGHTED = PseudoClass.getPseudoClass(PSEUDO_CLASS_HIGHLIGHTED_IDENT);
    private static final PseudoClass PSEUDO_CLASS_HOVERED = PseudoClass.getPseudoClass(PSEUDO_CLASS_HOVERED_IDENT);
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass(PSEUDO_CLASS_SELECTED_IDENT);

    private final IGravisItem element;

    private String activeStyleClass;

    public GravisShapeHandle(IGravisItem element) {
        this.element = element;
    }

    public IGravisItem getElement() {
        return element;
    }

    private final BooleanProperty isDisabled = new BooleanPropertyBase(false) {
        @Override
        protected void invalidated() {
            element.pseudoClassStateChanged(PSEUDO_CLASS_DISABLED, get());
        }

        @Override
        public Object getBean() {
            return element;
        }

        @Override
        public String getName() {
            return PSEUDO_CLASS_DISABLED_IDENT;
        }
    };

    private final BooleanProperty isHighlighted = new BooleanPropertyBase(false) {
        @Override
        protected void invalidated() {
            element.pseudoClassStateChanged(PSEUDO_CLASS_HIGHLIGHTED, get());
        }

        @Override
        public Object getBean() {
            return element;
        }

        @Override
        public String getName() {
            return PSEUDO_CLASS_HIGHLIGHTED_IDENT;
        }
    };

    private final BooleanProperty isHovered = new BooleanPropertyBase(false) {
        @Override
        protected void invalidated() {
            element.pseudoClassStateChanged(PSEUDO_CLASS_HOVERED, get());
        }

        @Override
        public Object getBean() {
            return element;
        }

        @Override
        public String getName() {
            return PSEUDO_CLASS_HOVERED_IDENT;
        }
    };

    private final BooleanProperty isSelected = new BooleanPropertyBase(false) {
        @Override
        protected void invalidated() {
            element.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, get());
        }

        @Override
        public Object getBean() {
            return element;
        }

        @Override
        public String getName() {
            return PSEUDO_CLASS_SELECTED_IDENT;
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
    
    public void setDisabled(boolean value) {
        isDisabled.set(value);
    }

    public void setHighlighted(boolean value) {
        isHighlighted.set(value);
    }

    public void setHovered(boolean value) {
        isHovered.set(value);
    }

    public void setSelected(boolean value) {
        isSelected.set(value);
    }

    public boolean isDisabled() {
        return isDisabled.get();
    }

    public boolean isHighlighted() {
        return isHighlighted.get();
    }

    public boolean isHovered() {
        return isHovered.get();
    }

    public boolean isSelected() {
        return isSelected.get();
    }

    public void putOnTop() {
        if (element.getParent() != null) {
            Group layer = (Group) element.getParent();
            for (Shape shape : element.getShapes()) {
                layer.getChildren().remove(shape);
                layer.getChildren().add(shape);
            }
        }
    }
}
