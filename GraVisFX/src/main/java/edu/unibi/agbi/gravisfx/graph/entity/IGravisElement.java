/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity;

import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public interface IGravisElement
{
    public List<Shape> getShapes();
    
    public void setTranslate(double positionX, double positionY);
    
    public double getTranslateX();
    public double getTranslateY();
    
    public DoubleProperty translateXProperty();
    public DoubleProperty translateYProperty();
    
    public void putOnTop();
}
