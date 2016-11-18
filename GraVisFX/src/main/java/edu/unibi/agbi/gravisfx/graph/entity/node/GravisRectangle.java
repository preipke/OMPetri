/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.node;

import edu.unibi.agbi.gravisfx.controller.PropertiesController;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;

/**
 * 
 * @author PR
 */
public class GravisRectangle extends GravisNode implements IGravisNode
{
    public GravisRectangle(String id) {
        super(id, new Rectangle());
    }
    public GravisRectangle(String id, Color color) {
        super(id, new Rectangle(), color);
    }
    
    /*
    @Override
    public void init(double centerX , double centerY , Scale scaling) {
        
        shape.setX(centerX - PropertiesController.RECTANGLE_WIDTH / 2 * scaling.getX());
        shape.setY(centerY - PropertiesController.RECTANGLE_HEIGHT / 2 * scaling.getY());
        shape.setWidth(PropertiesController.RECTANGLE_WIDTH * scaling.getX());
        shape.setHeight(PropertiesController.RECTANGLE_HEIGHT * scaling.getY());
        shape.setArcWidth(PropertiesController.RECTANGLE_ARC_WIDTH * scaling.getX());
        shape.setArcHeight(PropertiesController.RECTANGLE_ARC_HEIGHT * scaling.getY());
    }

    public void relocate(double centerX , double centerY, Scale scaling) {
        shape.setX(centerX - PropertiesController.RECTANGLE_WIDTH * scaling.getX());
        shape.setY(centerY - PropertiesController.RECTANGLE_HEIGHT * scaling.getY());
    }*/
}
