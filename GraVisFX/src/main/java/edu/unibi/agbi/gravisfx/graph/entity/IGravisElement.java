/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity;

import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public interface IGravisElement
{
    public Shape getShape();
    
    public double getTranslateX();
    public double getTranslateY();
}
