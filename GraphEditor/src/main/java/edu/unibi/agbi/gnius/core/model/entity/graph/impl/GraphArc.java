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
import edu.unibi.agbi.gravisfx.exception.RelationChangeDeniedException;
import edu.unibi.agbi.gravisfx.graph.layer.EdgeLayer;
import edu.unibi.agbi.gravisfx.graph.entity.impl.GravisEdge;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;

/**
 *
 * @author PR
 */
public class GraphArc extends GravisEdge implements IGraphArc
{
    private DataArc dataArc;
    
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
            return GraphArc.this;
        }
        @Override
        public String getName() {
            return PSEUDO_CLASS_IDENT;
        }
    };
    
    public GraphArc(IGraphNode source, IGraphNode target) {
        super(source , target);
    }
    
    public GraphArc(IGraphNode source, IGraphNode target, IDataArc dataArc) throws RelationChangeDeniedException {
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
        EdgeLayer edgeLayer = (EdgeLayer) getParent();
        edgeLayer.getChildren().remove(this);
        edgeLayer.getChildren().add(this);
    }
}
