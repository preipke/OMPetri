/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.layer;

import javafx.scene.Group;
import javafx.scene.transform.Scale;

/**
 *
 * @author PR
 */
public class TopLayer extends Group
{
    private final Scale scale;
    
    private final EdgeLayer edgeLayer;
    private final NodeLayer nodeLayer;
    private final LabelLayer labelLayer;
    
    public TopLayer() {
        scale = new Scale(1.0d, 1.0d);
        getTransforms().add(scale);
        
        edgeLayer = new EdgeLayer();
        nodeLayer = new NodeLayer();
        labelLayer = new LabelLayer();
        
        // order matters!
        getChildren().add(edgeLayer);
        getChildren().add(nodeLayer);
        getChildren().add(labelLayer);
    }
    
    /**
     * Get the scale applied to the layer.
     * @return 
     */
    public Scale getScale() {
        return scale;
    }

    public NodeLayer getNodeLayer() {
        return nodeLayer;
    }

    public EdgeLayer getEdgeLayer() {
        return edgeLayer;
    }

    public LabelLayer getLabelLayer() {
        return labelLayer;
    }
}
