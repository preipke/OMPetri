/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import java.util.List;

/**
 *
 * @author PR
 */
public interface INode extends IElement
{
    public List<IArc> getArcsOut();

    public List<IArc> getArcsIn();

    public boolean isConstant();

    public void setConstant(boolean value);
}
