/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity;

import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import java.util.Collection;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.shape.Shape;

/**
 * Basic interface for all items that can be visualized within a graph. This
 * includes nodes, edges, subelements, labels and so on.
 *
 * @author PR
 */
public interface IGravisItem
{
    /**
     * Parent in the JavaFX scene graph. Should be one of the layers defined in
     * the Graph.
     *
     * @return
     */
    public Parent getParent();

    /**
     * Shape objects associated to an item. Can be one or more shapes that are
     * combined into one element.
     *
     * @return
     */
    public Collection<Shape> getShapes();

    /**
     * Element handles associated to an item. Can be one or more handles
     * depending on the number of shapes associated to an item.
     *
     * @return
     */
    public List<GravisShapeHandle> getElementHandles();

    public void pseudoClassStateChanged(PseudoClass pseudoClass, boolean active);

    public ObservableList<String> getStyleClass();

    public DoubleProperty translateXProperty();

    public DoubleProperty translateYProperty();

    public double getCenterOffsetX();

    public double getCenterOffsetY();

    public void setDisable(boolean value);
}
