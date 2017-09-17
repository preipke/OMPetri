/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity;

import edu.unibi.agbi.gravisfx.entity.parent.IGravisParent;
import edu.unibi.agbi.gravisfx.entity.child.GravisChildLabel;
import java.util.Collection;

/**
 * Interface for interactive nodes within the graph. Used by parent components
 * of a node only, i.e. Circle, Rectangle, DoubleCircle, DoubleRectanle.
 *
 * @author PR
 */
public interface IGravisNode extends IGravisElement, IGravisParent
{
    public Collection<IGravisNode> getParents();

    public Collection<IGravisNode> getChildren();

    public Collection<IGravisConnection> getConnections();

    public GravisChildLabel getLabel();

    public void setInnerCircleVisible(boolean value);

    public void setInnerRectangleVisible(boolean value);
}
