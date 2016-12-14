/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.exception.RelationChangeDeniedException;
import edu.unibi.agbi.gravisfx.graph.entity.impl.GravisCurve;
import edu.unibi.agbi.gravisfx.graph.layer.EdgeLayer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;

/**
 *
 * @author PR
 */
public class GraphCurve extends GravisCurve implements IGraphArc
{
    private DataArc dataArc;
    
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
    
    public GraphCurve(IGraphNode source, IGraphNode target) {
        super(source , target);
    }

    public GraphCurve(IGraphNode source , IGraphNode target , IDataArc dataArc) throws RelationChangeDeniedException {
        this(source , target);
        if (!(dataArc instanceof DataArc)) {
            throw new RelationChangeDeniedException("Must assign DataArc to GraphArc! Action denied.");
        }
        this.dataArc = (DataArc) dataArc;
    }

    @Override
    public void setRelatedElement(IDataArc dataArc) throws RelationChangeDeniedException {
        if (this.dataArc != null) {
            throw new RelationChangeDeniedException("Related data object has already been assigned! Action denied.");
        } else if (!(dataArc instanceof DataArc)) {
            throw new RelationChangeDeniedException("Must assign DataArc to GraphArc! Action denied.");
        }
        this.dataArc = (DataArc) dataArc;
    }
    
    @Override
    public DataArc getRelatedDataArc() {
        return dataArc;
    }
    
    @Override
    public IDataElement getRelatedDataElement() {
        return getRelatedDataArc();
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
        EdgeLayer edgeLayer = (EdgeLayer) getParent();
        edgeLayer.getChildren().remove(this);
        edgeLayer.getChildren().add(this);
    }
}
