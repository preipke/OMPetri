/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data;

import edu.unibi.agbi.petrinet.entity.IArc;

/**
 *
 * @author PR
 */
public interface IDataArc extends IDataElement, IArc
{
    @Override public IDataNode getSource();
    @Override public IDataNode getTarget();
}
