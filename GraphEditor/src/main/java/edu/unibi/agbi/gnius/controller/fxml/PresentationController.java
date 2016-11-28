/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.controller.fxml;

import edu.unibi.agbi.gnius.model.NodeType;
import edu.unibi.agbi.gnius.exception.controller.GraphNotNullException;
import edu.unibi.agbi.gnius.exception.data.EdgeCreationException;
import edu.unibi.agbi.gnius.exception.data.NodeCreationException;
import edu.unibi.agbi.gnius.model.EdgeType;

import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisCircle;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisRectangle;

import javafx.scene.input.MouseEvent;

/**
 *
 * @author PR
 */
public class PresentationController
{
    private static Graph graph = null;
    
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
        
        graph.addNode(node);
        
        return node;
    }
    
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
        
        graph.addEdge(edge);
        
        return edge;
    }
    
    public static void remove(IGravisNode edge) {
        
        
        
        
    }
    
    public static void remove(IGravisEdge edge) {
        
        
        
    }
    
    
    
    
    
    public static void setGraph(Graph graph) throws GraphNotNullException {
        if (PresentationController.graph != null) {
            throw new GraphNotNullException("Graph has already been initialized!");
        } 
        PresentationController.graph = graph;
    }
    
    public static Graph getGraph() {
        return graph;
    }
}
