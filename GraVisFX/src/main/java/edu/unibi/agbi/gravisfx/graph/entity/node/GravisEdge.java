/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.node;

import javafx.scene.shape.Line;

/**
 *
 * @author PR
 */
public class GravisEdge extends Line
{
    private final IGravisNode source;
    private final IGravisNode target;
    
    public GravisEdge(IGravisNode source, IGravisNode target) {
        
        super();
        
        this.source = source;
        this.target = target;
        
        startXProperty().bind(source.getShape().translateXProperty().add(source.getOffsetX()));
        startYProperty().bind(source.getShape().translateYProperty().add(source.getOffsetY()));

        endXProperty().bind(target.getShape().translateXProperty().add(target.getOffsetX()));
        endYProperty().bind(target.getShape().translateYProperty().add(target.getOffsetY()));
        
    }
    
    public IGravisNode getSource() {
        return source;
    }
    
    public IGravisNode getTarget() {
        return target;
    }
}
