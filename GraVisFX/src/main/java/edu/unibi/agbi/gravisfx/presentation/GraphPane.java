/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.presentation;

import edu.unibi.agbi.gravisfx.graph.Graph;
import javafx.scene.layout.Pane;

/**
 *
 * @author PR
 */
public final class GraphPane extends Pane
{
    private final Graph graphRoot;
    private Graph graph;

    public GraphPane(Graph graph) {
        super();
        getChildren().add(graph);
        this.graphRoot = graph;
        this.graph = graph;
    }
    
    public void setGraph(Graph graph) {
        getChildren().remove(this.graph);
        getChildren().add(graph);
        this.graph = graph;
    }

    public Graph getGraph() {
        return graph;
    }
    
    public Graph getGraphRoot() {
        return graphRoot;
    }
}
