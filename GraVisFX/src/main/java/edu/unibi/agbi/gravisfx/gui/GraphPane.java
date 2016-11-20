/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.gui;

import edu.unibi.agbi.gravisfx.graph.entity.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 *
 * @author PR
 */
public final class GraphPane extends Pane
{
    private final TopLayer topLayer;
    
    private Double eventMousePressedX = null;
    private Double eventMousePressedY = null;
    
    public void setHandler() {
        
        setOnMousePressed(( MouseEvent event ) -> {
            eventMousePressedX = event.getX();
            eventMousePressedY = event.getY();
        });
        setOnMouseReleased((MouseEvent event) -> {
            eventMousePressedX = null;
            eventMousePressedY = null;
        });
        setOnMouseDragged(( MouseEvent event ) -> {
            
            if (IGravisNode.class.isAssignableFrom(event.getTarget().getClass())) {
                IGravisNode node = (IGravisNode) event.getTarget();
                //TopLayer topLayer = (TopLayer) node.getShape().getParent().getParent();
                node.setPosition(
                        event.getX() - topLayer.translateXProperty().get() , 
                        event.getY() - topLayer.translateYProperty().get()
                );
            }
            else if (GraphPane.class.isAssignableFrom(event.getTarget().getClass())) {
                
                //GraphPane pane = (GraphPane) event.getTarget();
                
                //pane.getTopLayer().setTranslateX(event.getX() - eventMousePressedX + pane.getTopLayer().translateXProperty().get());
                //pane.getTopLayer().setTranslateY(event.getY() - eventMousePressedY + pane.getTopLayer().translateYProperty().get());
                
                topLayer.setTranslateX(event.getX() - eventMousePressedX + topLayer.translateXProperty().get());
                topLayer.setTranslateY(event.getY() - eventMousePressedY + topLayer.translateYProperty().get());

                eventMousePressedX = event.getX();
                eventMousePressedY = event.getY();
            }
        });
    }
    
    public GraphPane(TopLayer topLayer) {
        super();
        this.topLayer = topLayer;
        getChildren().add(topLayer);
        
        setHandler();
    }
    
    public TopLayer getTopLayer() {
        return topLayer;
    }
}
