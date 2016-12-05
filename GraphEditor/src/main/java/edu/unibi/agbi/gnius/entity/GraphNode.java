/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.entity;

/**
 *
 * @author PR
 */
public interface GraphNode
{
    public Type getGraphNodeType();
    
    public enum Type {
        EDGE, PLACE, TRANSITION;
    }
}
