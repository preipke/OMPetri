/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.exception.RelationChangeDeniedException;
import edu.unibi.agbi.gravisfx.graph.entity.impl.GravisCircle;
import edu.unibi.agbi.gravisfx.graph.layer.NodeLayer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;

/**
 *
 * @author PR
 */
public class GraphPlace extends GravisCircle implements IGraphNode
{
    private DataPlace dataElement;
    
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
            return GraphPlace.this;
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
            return GraphPlace.this;
        }
        @Override
        public String getName() {
            return PSEUDO_CLASS_HIGHLIGHT_IDENT;
        }
    };
    
    public GraphPlace() {
        super();
    }
    
    public GraphPlace(IDataNode dataNode) throws RelationChangeDeniedException {
        this();
        if (!(dataNode instanceof DataPlace)) {
            throw new RelationChangeDeniedException("Must assign DataPlace to GraphPlace! Action denied.");
        }
        this.dataElement = (DataPlace) dataNode;
    }

    @Override
    public void setRelatedElement(IDataNode dataNode) throws RelationChangeDeniedException {
        if (this.dataElement != null) {
            throw new RelationChangeDeniedException("Related data object has already been assigned! Action denied.");
        } else if (!(dataNode instanceof DataPlace)) {
            throw new RelationChangeDeniedException("Must assign DataPlace to GraphPlace! Action denied.");
        }
        this.dataElement = (DataPlace) dataNode;
    }
    
    @Override
    public DataPlace getRelatedDataNode() {
        return dataElement;
    }
    
    @Override
    public IDataElement getRelatedDataElement() {
        return getRelatedDataNode();
    }

    @Override
    public String getActiveStyleClass() {
        return activeStyleClass;
    }

    @Override
    public void setActiveStyleClass(String name) {
        getShape().getStyleClass().remove(activeStyleClass);
        activeStyleClass = name;
        getShape().getStyleClass().add(name);
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
    public void putOnTop() {
        NodeLayer nodeLayer = (NodeLayer) getParent();
        nodeLayer.getChildren().remove(this);
        nodeLayer.getChildren().add(this);
    }
}
