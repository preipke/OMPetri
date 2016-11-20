/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.gui;

import edu.unibi.agbi.gravisfx.graph.entity.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;

/**
 *
 * @author PR
 */
public final class GraphPane extends Pane
{
    private final TopLayer topLayer;
    
    double scaleValue = 1.0;
    double scaleDelta = 0.1;
    
    private Double eventMousePressedX = null;
    private Double eventMousePressedY = null;
    
    public void setHandler() {
        
        setOnScroll(( ScrollEvent event ) -> {
            if (event.getDeltaY() < 0) {
                scaleValue -= scaleDelta;
            } else {
                scaleValue += scaleDelta;
            }
            topLayer.getScaleTransform().setX(scaleValue);
            topLayer.getScaleTransform().setY(scaleValue);
        });
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
                node.setPosition(
                        (event.getX() - topLayer.translateXProperty().get()) / topLayer.getScaleTransform().getX() , 
                        (event.getY() - topLayer.translateYProperty().get()) / topLayer.getScaleTransform().getX()
                );
            }
            else if (GraphPane.class.isAssignableFrom(event.getTarget().getClass())) {
                
                topLayer.setTranslateX((event.getX() - eventMousePressedX + topLayer.translateXProperty().get()));
                topLayer.setTranslateY((event.getY() - eventMousePressedY + topLayer.translateYProperty().get()));

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
