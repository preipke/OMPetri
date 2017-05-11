/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.layer;

import javafx.scene.Group;
import javafx.scene.transform.Scale;

/**
 * Stores the label, node, connection.
 *
 * @author PR
 */
public class TopLayer extends Group
{
    private final Scale scale;

    private final LabelLayer labelLayer;
    private final NodeLayer nodeLayer;
    private final ConnectionLayer connectionLayer;

    public TopLayer() {
        scale = new Scale(1.0d, 1.0d);
        getTransforms().add(scale);

        connectionLayer = new ConnectionLayer();
        nodeLayer = new NodeLayer();
        labelLayer = new LabelLayer();

        // order matters!
        getChildren().add(connectionLayer);
        getChildren().add(nodeLayer);
        getChildren().add(labelLayer);
    }

    /**
     * Get the scale applied to the layer.
     *
     * @return
     */
    public Scale getScale() {
        return scale;
    }

    public NodeLayer getNodeLayer() {
        return nodeLayer;
    }

    public ConnectionLayer getConnectionLayer() {
        return connectionLayer;
    }

    public LabelLayer getLabelLayer() {
        return labelLayer;
    }
}
