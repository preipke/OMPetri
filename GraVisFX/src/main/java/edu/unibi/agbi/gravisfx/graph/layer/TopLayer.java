/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.layer;

import javafx.scene.Group;

/**
 *
 * @author PR
 */
public class TopLayer extends Group
{
    private final EdgeLayer edgeLayer;
    private final NodeLayer nodeLayer;
    private final SelectionLayer selectionLayer;
    
    public TopLayer() {
        edgeLayer = new EdgeLayer();
        nodeLayer = new NodeLayer();
        selectionLayer = new SelectionLayer();
        
        // order matters!
        getChildren().add(edgeLayer);
        getChildren().add(nodeLayer);
        getChildren().add(selectionLayer);
    }

    public NodeLayer getNodeLayer() {
        return nodeLayer;
    }

    public EdgeLayer getEdgeLayer() {
        return edgeLayer;
    }

    public SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }
}
