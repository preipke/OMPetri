/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.controller.tab.presentation;

import edu.unibi.agbi.gnius.exception.data.EdgeCreationException;
import edu.unibi.agbi.gnius.exception.data.NodeCreationException;
import edu.unibi.agbi.gnius.handler.MouseGestures;
import edu.unibi.agbi.gnius.handler.ZoomHandler;
import edu.unibi.agbi.gnius.model.EdgeType;
import edu.unibi.agbi.gnius.model.NodeType;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.node.IGravisSelectable;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisCircle;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisRectangle;
import edu.unibi.agbi.gravisfx.presentation.GraphScene;
import edu.unibi.agbi.gravisfx.presentation.layer.TopLayer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author PR
 */
public class PresentationPaneController implements Initializable
{
    @FXML private BorderPane presentationPane;
    
    private static Graph graph = null;

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        GraphScene graphScene = new GraphScene();
        graphScene.setId("presentationScene");
        graphScene.widthProperty().bind(presentationPane.widthProperty());
        graphScene.heightProperty().bind(presentationPane.heightProperty());
        
        presentationPane.setCenter(graphScene);
        
        PresentationPaneController.graph = (graphScene.getGraph());
            
        // register handler
        ZoomHandler.registerTo(graphScene.getGraphPane());
        MouseGestures.registerTo(graphScene.getGraphPane());
    }
    
    /**
     * 
     * @param type
     * @param event
     * @return
     * @throws NodeCreationException 
     */
    public static IGravisNode create(NodeType.Type type, MouseEvent event) throws NodeCreationException {
        
        IGravisNode node;
        
        switch(type) {
            case PLACE:
                node = new GravisCircle();
                node.getShape().getStyleClass().add("gravisCircle");
                break;
            case TRANSITION:
                node = new GravisRectangle();
                node.getShape().getStyleClass().add("gravisRectangle");
                break;
            default:
                throw new NodeCreationException("No suitable node type selected!");
        }
        
        if (event != null) {
            node.setTranslate(
                    (event.getX() - graph.getTopLayer().translateXProperty().get()) / graph.getTopLayer().getScaleTransform().getX() ,
                    (event.getY() - graph.getTopLayer().translateYProperty().get()) / graph.getTopLayer().getScaleTransform().getX()
            );
        }
        
        graph.add(node);
        
        return node;
    }
    
    /**
     * 
     * @param type
     * @param source
     * @param target
     * @return
     * @throws EdgeCreationException 
     */
    public static IGravisEdge create(EdgeType.Type type, IGravisNode source, IGravisNode target) throws EdgeCreationException {
        
        IGravisEdge edge;
        switch(type) {
            case EDGE:
                edge = new GravisEdge(source, target);
                edge.getShape().getStyleClass().add("gravisEdge");
                break;
            case ARC:
                edge = new GravisEdge(source, target);
                edge.getShape().getStyleClass().add("gravisArc");
                break;
            default:
                throw new EdgeCreationException("No suitable edge type selected!");
        }
        
        graph.add(edge);
        
        return edge;
    }
    
    /**
     * Copies the given nodes.
     * Creates a new shape for the given objects relative to the existing object.
     * The translation applied to the new nodes is the current mouse pointer position.
     * 
     * @param nodes
     * @param event
     * @return
     * @throws NodeCreationException 
     */
    public static List<IGravisNode> copy(IGravisNode[] nodes, MouseEvent event) throws NodeCreationException {
        
        List<IGravisNode> nodesCopied = new ArrayList();
        IGravisNode nodeCopy;
        
        double mouseX = event.getX();
        double mouseY = event.getY();
        
        TopLayer topLayer = null;

        for (int i = 0; i < nodes.length; i++) {

            if (GravisCircle.class.isAssignableFrom(nodes[i].getClass())) {
                nodeCopy = new GravisCircle();
                nodeCopy.getShape().getStyleClass().add("gravisCircle");
            } else if (GravisRectangle.class.isAssignableFrom(nodes[i].getClass())) {
                nodeCopy = new GravisRectangle();
                nodeCopy.getShape().getStyleClass().add("gravisRectangle");
            } else {
                throw new NodeCreationException("Node type cannot be recovered! Copying aborted.");
            }
            
            if (topLayer == null) {
                topLayer = ((TopLayer)nodes[i].getShape().getParent().getParent());
            }

            nodeCopy.setTranslate(
                    (mouseX - topLayer.translateXProperty().get()) / topLayer.getScaleTransform().getX() ,
                    (mouseY - topLayer.translateYProperty().get()) / topLayer.getScaleTransform().getX()
            );

            graph.add(nodeCopy);

            nodesCopied.add(nodeCopy);
        }
        
        return nodesCopied;
    }
    
    /**
     * Schwerpunkt von Punktwolke relativ zur Szene berechnen. 
     * Benutzen zum Verschieben, Kopieren, Einfügen usw.
     * 
     * @return 
     */
    private Point2D ComputeCenter(IGravisNode[] nodes) {
        
        double x = 0;
        double y = 0;
        
        double offsetX = graph.getTopLayer().getTranslateX();
        double offsetY = graph.getTopLayer().getTranslateY();
        
        for (IGravisNode node : nodes) {
            x += node.getTranslateX();
            y += node.getTranslateY();
        }
        
        System.out.println("X=" + x + " | Y=" + y);
        
        return new Point2D(x , y);
    }
    
    /**
     * 
     * @param nodes
     * @param event
     * @return
     * @throws NodeCreationException 
     */
    public static List<IGravisSelectable> clone(IGravisNode[] nodes, MouseEvent event) throws NodeCreationException {
        
        List<IGravisSelectable> nodesCopied = new ArrayList();
        IGravisNode nodeCopy;
        
        double mouseX = event.getX();
        double mouseY = event.getY();
        
        TopLayer topLayer = null;
        
        return nodesCopied;
    }
    
    
    public static void remove(IGravisNode node) {
        List<IGravisEdge> edges = node.getEdges();
        for (IGravisEdge edge : edges) {
            Platform.runLater(() -> {
                graph.remove(edge); // must be executed on application main thread.
            });
        }
        Platform.runLater(() -> {
            graph.remove(node);
        });
    }
    
    public static void remove(IGravisEdge edge) {
        Platform.runLater(() -> {
            graph.remove(edge);
        });
    }
    
    public static Graph getGraph() {
        return graph;
    }
}