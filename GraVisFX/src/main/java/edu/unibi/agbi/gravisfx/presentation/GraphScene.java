/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.presentation;

import edu.unibi.agbi.gravisfx.graph.Graph;
import javafx.scene.SubScene;

/**
 *
 * @author PR
 */
public final class GraphScene extends SubScene
{
    private final GraphPane graphPane;

    public GraphScene(Graph graph) {

        super(null, 0, 0);

        graphPane = new GraphPane(graph);

        setRoot(graphPane);
        graphPane.maxHeightProperty().bind(heightProperty());
        graphPane.maxWidthProperty().bind(widthProperty());
        setManaged(false); // must be set, otherwise scene will upscale parent but will not size down again on resizing the window
    }

    public GraphPane getGraphPane() {
        return graphPane;
    }
}
