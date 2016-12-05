/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.presentation;

import edu.unibi.agbi.gravisfx.graph.Graph;

import javafx.scene.Parent;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;

/**
 *
 * @author PR
 */
public final class GraphScene extends SubScene
{
    private final Graph graph;
    private final GraphPane graphPane;
    
    public GraphScene(Graph graph) {
        super(null , 0 , 0);
        this.graph = graph;
        this.graphPane = new GraphPane(graph.getTopLayer());
        init();
    }
    
    public GraphScene(Graph graph, Parent root , double width , double height) {
        super(root , width , height);
        this.graph = graph;
        this.graphPane = new GraphPane(graph.getTopLayer());
        init();
    }

    public GraphScene(Graph graph, Parent root , double width , double height , boolean depthBuffer , SceneAntialiasing antiAliasing) {
        super(root , width , height , depthBuffer , antiAliasing);
        this.graph = graph;
        this.graphPane = new GraphPane(graph.getTopLayer());
        init();
    }
    
    public void init() {
        setRoot(graphPane);
        graphPane.maxHeightProperty().bind(heightProperty());
        graphPane.maxWidthProperty().bind(widthProperty());
        setManaged(false); // must be set, otherwise scene will upscale parent and will thereby not size down again on resizing the window
    }
    
    public Graph getGraph() {
        return graph;
    }
    
    public GraphPane getGraphPane() {
        return graphPane;
    }
}
