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
    private final NodeLayer nodeLayer;
    private final EdgeLayer edgeLayer;
    private final SelectionLayer selectionLayer;
    
    public TopLayer() {
        nodeLayer = new NodeLayer();
        edgeLayer = new EdgeLayer();
        selectionLayer = new SelectionLayer();
        
        getChildren().add(nodeLayer);
        getChildren().add(edgeLayer);
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
