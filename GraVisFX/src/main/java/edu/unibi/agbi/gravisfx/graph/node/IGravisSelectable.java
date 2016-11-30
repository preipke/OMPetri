/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.node;

import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public interface IGravisSelectable
{
    public Shape getShape();
    public double getTranslateX();
    public double getTranslateY();
    public void setHighlight(boolean value);
    public void putOnTop();
}
