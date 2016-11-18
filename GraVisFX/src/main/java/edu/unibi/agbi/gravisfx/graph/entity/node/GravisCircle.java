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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;

/**
 *
 * @author PR
 */
public class GravisCircle extends GravisNode
{
    
    public GravisCircle(String id) {
        super(id , new Circle());
    }
    
    public GravisCircle(String id, Color color) {
        super(id , new Circle(), color);
    }
    
    /*
    public void init(double centerX , double centerY , Scale scaling) {
        shape.setCenterX(centerX);
        shape.setCenterY(centerY);
        shape.setRadius(PropertiesController.CIRCLE_RADIUS * scaling.getX());
    }

    public void relocate(double centerX , double centerY, Scale scaling) {
        shape.setCenterX(centerX);
        shape.setCenterY(centerY);
        shape.setRadius(PropertiesController.CIRCLE_RADIUS * scaling.getX());
    }*/
}
