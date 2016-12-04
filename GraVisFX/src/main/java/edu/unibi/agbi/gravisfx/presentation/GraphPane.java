/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.presentation;

import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;
import javafx.scene.layout.Pane;

/**
 *
 * @author PR
 */
public final class GraphPane extends Pane
{
    private final TopLayer topLayer;
    
    public GraphPane(TopLayer topLayer) {
        
        super();
        
        this.topLayer = topLayer;
        
        getChildren().add(topLayer);
    }
    
    public TopLayer getTopLayer() {
        return topLayer;
    }
}
