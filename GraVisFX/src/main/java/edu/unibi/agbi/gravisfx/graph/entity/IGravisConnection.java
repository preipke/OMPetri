/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity;

/**
 *
 * @author PR
 */
public interface IGravisConnection extends IGravisElement
{
    public IGravisNode getSource();
    public IGravisNode getTarget();
}
