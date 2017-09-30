/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.data.entity.graph;

import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.gravisfx.entity.root.IGravisRoot;

/**
 *
 * @author PR
 */
public interface IGraphElement extends IGravisRoot
{
    public IDataElement getData();
    public boolean isElementDisabled();
    public void setElementDisabled(boolean value);
}
