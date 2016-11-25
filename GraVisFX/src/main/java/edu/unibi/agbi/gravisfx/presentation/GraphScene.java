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
    private Graph graph;
    private GraphPane graphPane;
    
    public Graph getGraph() {
        return graph;
    }
    
    public GraphPane getGraphPane() {
        return graphPane;
    }
    
    public void init() {
        
        graph = new Graph();
        
        graphPane = new GraphPane(graph.getTopLayer());
        
        // TODO scale window down again after maximizing size
        graphPane.maxHeightProperty().bind(heightProperty());
        graphPane.maxWidthProperty().bind(widthProperty());
        
        String css = "-fx-background-color: white;"
                + "-fx-border-color: grey;"
        //        + "-fx-border-insets: 5;"
                + "-fx-border-width: 2;"
        //        + "-fx-border-style: dashed;"
        ;
        graphPane.setStyle(css);
        
        setRoot(graphPane);
        
        setManaged(false); // must be set, otherwise scene will upscale parent and will thereby not size down again on resizing the window
    }
    
    public GraphScene() {
        super(null , 0 , 0);
        init();
    }
    
    public GraphScene(Parent root , double width , double height) {
        super(root , width , height);
        init();
    }

    public GraphScene(Parent root , double width , double height , boolean depthBuffer , SceneAntialiasing antiAliasing) {
        super(root , width , height , depthBuffer , antiAliasing);
        init();
    }
}
