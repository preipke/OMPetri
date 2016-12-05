/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.entity;

import java.util.List;

/**
 *
 * @author PR
 */
public interface GraphNode
{
    public Type getType();
    public void addShape(Object shape);
    public List<Object> getShapes();
    
    public enum Type {
        EDGE, PLACE, TRANSITION;
    }
}
