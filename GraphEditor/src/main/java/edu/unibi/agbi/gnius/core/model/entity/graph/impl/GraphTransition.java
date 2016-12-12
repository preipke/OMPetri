/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.exception.RelationChangeDeniedException;
import edu.unibi.agbi.gravisfx.graph.layer.NodeLayer;
import edu.unibi.agbi.gravisfx.graph.entity.impl.GravisRectangle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;

/**
 *
 * @author PR
 */
public class GraphTransition extends GravisRectangle implements IGraphNode
{
    private DataTransition dataTransition;
    
    private String activeStyleClass;
    
    private static final String PSEUDO_CLASS_IDENT = "selected";
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass(PSEUDO_CLASS_IDENT);
    private final BooleanProperty isSelected = new BooleanPropertyBase(false) {
        @Override 
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS , get());
        }
        @Override
        public Object getBean() {
            return GraphTransition.this;
        }
        @Override
        public String getName() {
            return PSEUDO_CLASS_IDENT;
        }
    };
    
    public GraphTransition() {
        super();
    }
    
    public GraphTransition(IDataNode dataNode) throws RelationChangeDeniedException {
        this();
        if (!(dataNode instanceof DataTransition)) {
            throw new RelationChangeDeniedException("Must assign DataTransition to GraphTransition! Action denied.");
        }
        this.dataTransition = (DataTransition) dataNode;
    }

    @Override
    public void setRelatedElement(IDataNode dataNode) throws RelationChangeDeniedException {
        if (this.dataTransition != null) {
            throw new RelationChangeDeniedException("Related data object has already been assigned!");
        } else if (!(dataNode instanceof DataTransition)) {
            throw new RelationChangeDeniedException("Must assign DataTransition to GraphTransition! Action denied.");
        }
        this.dataTransition = (DataTransition) dataNode;
    }
    
    @Override
    public DataTransition getRelatedDataNode() {
        return dataTransition;
    }
    
    @Override
    public IDataElement getRelatedDataElement() {
        return getRelatedDataNode();
    }

    @Override
    public void setActiveStyleClass(String name) {
        getShape().getStyleClass().remove(activeStyleClass);
        activeStyleClass = name;
        getShape().getStyleClass().add(name);
    }

    @Override
    public String getActiveStyleClass() {
        return activeStyleClass;
    }

    @Override
    public void setHighlight(boolean value) {
        isSelected.set(value);
    }
    
    @Override
    public void putOnTop() {
        NodeLayer nodeLayer = (NodeLayer) getParent();
        nodeLayer.getChildren().remove(this);
        nodeLayer.getChildren().add(this);
    }
}
