/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity;

import edu.unibi.agbi.gravisfx.graph.entity.abst.GravisElementHandle;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public interface IGravisElement
{
    public Object getBean();
    public List<GravisElementHandle> getElementHandles();
    public Parent getParent();
    public Shape getShape();
    public List<Shape> getAllShapes();
    public void pseudoClassStateChanged(PseudoClass pseudoClass, boolean active);
    public ObservableList<String> getStyleClass();
    public DoubleProperty translateXProperty();
    public DoubleProperty translateYProperty();
}
