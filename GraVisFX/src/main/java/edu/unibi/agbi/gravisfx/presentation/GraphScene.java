/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.presentation;

import edu.unibi.agbi.gravisfx.graph.Graph;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.SubScene;

/**
 *
 * @author PR
 */
public final class GraphScene extends SubScene
{
    private final Graph graph;
    private final GraphPane graphPane;

    private final List<Object> objects;

    public GraphScene(Graph graph) {

        super(null, 0, 0);

        this.graph = graph;
        this.graphPane = new GraphPane(graph.getTopLayer());

        objects = new ArrayList();

        setRoot(graphPane);
        graphPane.maxHeightProperty().bind(heightProperty());
        graphPane.maxWidthProperty().bind(widthProperty());
        setManaged(false); // must be set, otherwise scene will upscale parent but will not size down again on resizing the window
    }

    public Graph getGraph() {
        return graph;
    }

    public GraphPane getGraphPane() {
        return graphPane;
    }

    /**
     * Gets a list of objects. Can be used for anything, i.e. storing and
     * accessing controllers and services during testing.
     *
     * @return
     */
    public List<Object> getObjects() {
        return objects;
    }
}
