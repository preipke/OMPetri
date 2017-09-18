/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.root.connection;

import edu.unibi.agbi.gravisfx.entity.root.node.IGravisNode;
import javafx.beans.property.DoubleProperty;
import edu.unibi.agbi.gravisfx.entity.root.IGravisRoot;

/**
 * Interface for interactive connections within the graph. Used by parent
 * components of a connection only, i.e. GravisCurve, GravisCurveArrow,
 * GravisEdge, GravisEdgeArrow.
 *
 * @author PR
 */
public interface IGravisConnection extends IGravisRoot
{
    public DoubleProperty endXProperty();

    public DoubleProperty endYProperty();

    public IGravisNode getSource();

    public IGravisNode getTarget();

    public void setArrowHeadVisible(boolean value);

    public void setCircleHeadVisible(boolean value);
}
