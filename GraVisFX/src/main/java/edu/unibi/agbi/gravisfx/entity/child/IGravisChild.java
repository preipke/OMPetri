/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.child;

import edu.unibi.agbi.gravisfx.entity.IGravisElement;

/**
 * Interface for all elements that are related to a parent element.
 *
 * @author PR
 */
public interface IGravisChild extends IGravisElement
{
    public IGravisElement getParentShape();
}
