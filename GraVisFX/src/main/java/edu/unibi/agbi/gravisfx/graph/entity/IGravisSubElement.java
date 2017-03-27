/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity;

/**
 * Interface for all elements within a graph related to a parent element. 
 * @author PR
 */
public interface IGravisSubElement extends IGravisElement
{
    public IGravisElement getParentElement();
}
