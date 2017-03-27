/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity;

import edu.unibi.agbi.gravisfx.graph.entity.util.ElementHandle;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.shape.Shape;

/**
 * Basic interface for all elements visible within the graph. This includes
 * i.e. nodes, edges, subelements and labels.
 * @author PR
 */
public interface IGravisElement
{
    public Parent getParent();
    public Object getBean();
    public Shape getShape();
    public List<Shape> getShapes();
    public List<ElementHandle> getElementHandles();
    public void pseudoClassStateChanged(PseudoClass pseudoClass, boolean active);
    public ObservableList<String> getStyleClass();
    public DoubleProperty translateXProperty();
    public DoubleProperty translateYProperty();
}
