/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.service;

import edu.unibi.agbi.gnius.dao.GraphDao;
import edu.unibi.agbi.gnius.dao.PetriNetDao;
import edu.unibi.agbi.gnius.entity.GraphEdge;
import edu.unibi.agbi.gnius.entity.GraphNode;
import edu.unibi.agbi.gnius.entity.GraphPlace;
import edu.unibi.agbi.gnius.entity.GraphTransition;
import edu.unibi.agbi.gnius.selection.NodeSelectionChoice;
import edu.unibi.agbi.gnius.service.exception.EdgeCreationException;
import edu.unibi.agbi.gnius.service.exception.NodeCreationException;

import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisCircle;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisRectangle;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class DataService
{
    private final GraphDao graphDao;
    private final PetriNetDao petriNetDao;
    
    @Autowired
    public DataService(GraphDao graphDao, PetriNetDao petriNetDao) {
        this.graphDao = graphDao;
        this.petriNetDao = petriNetDao;
    }
    
    /**
     * Adds a IGravisNode to the graph.
     * @param node 
     */
    public void add(IGravisNode node) {
        graphDao.add(node);
    }
    
    /**
     * Creates a IGravisNode of the given NodeType at the MouseEvent target location.
     * 
     * @param type
     * @param target
     * @param position
     * @throws NodeCreationException 
     */
    public IGravisNode create(GraphNode.Type type, MouseEvent target, Point2D position) throws NodeCreationException {
        
        GraphNode node;
        IGravisNode shape;
        
        switch(type) {
            case PLACE:
                node = new GraphPlace();
                shape = new GravisCircle(node);
                shape.setActiveStyleClass("gravisCircle");
                break;
            case TRANSITION:
                node = new GraphTransition();
                shape = new GravisRectangle(node);
                shape.setActiveStyleClass("gravisRectangle");
                break;
            default:
                throw new NodeCreationException("No suitable node type selected!");
        }
        node.addShape(shape);
        
        if (target != null) { // remove later - for testing purposes
            double posX = (target.getX() - graphDao.getTopLayer().translateXProperty().get()) / graphDao.getTopLayer().getScale().getX();
            double posY = (target.getY() - graphDao.getTopLayer().translateYProperty().get()) / graphDao.getTopLayer().getScale().getY();

            shape.setTranslate(posX , posY);
        }
        
        graphDao.add(shape);
        petriNetDao.add(node);
        
        return shape; // remove later - for testing purposes
    }
    
    /**
     * Creates an edge of type EdgyType connecting source and target IGravisNode.
     * 
     * @param source
     * @param target
     * @throws EdgeCreationException 
     * @throws NodeCreationException 
     */
    public void create(IGravisNode source, IGravisNode target) throws EdgeCreationException , NodeCreationException {
        
        GraphNode node;
        IGravisEdge shape;

        node = new GraphEdge();
        shape = new GravisEdge(source , target , node);
        shape.setActiveStyleClass("gravisEdge");
        node.addShape(shape);
        
        graphDao.add(shape);
        petriNetDao.add(node);
    }
    
    public void connect(IGravisNode source, IGravisNode target) {
        
    }
    
    public IGravisNode copy(IGravisNode target) throws NodeCreationException {
        
        GraphNode node = (GraphNode) target.getRelatedObject();
        IGravisNode copy;
        
        switch(node.getType()) {
            case PLACE:
                node = new GraphPlace();
                copy = new GravisCircle(node);
                node.addShape(copy);
                break;
            case TRANSITION:
                node = new GraphTransition();
                copy = new GravisRectangle(node);
                node.addShape(copy);
                break;
            default:
                throw new NodeCreationException("No suitable edge type selected!");
        }
        
        return copy;
    }
    
    public IGravisNode clone(IGravisNode target) throws NodeCreationException {
        
        GraphNode node = (GraphNode) target.getRelatedObject();
        IGravisNode clone;
        
        switch(node.getType()) {
            case PLACE:
                clone = new GravisCircle(node);
                node.addShape(clone);
                break;
            case TRANSITION:
                clone = new GravisRectangle(node);
                node.addShape(clone);
                break;
            default:
                throw new NodeCreationException("No suitable edge type selected!");
        }
        
        return clone;
    }
    
    public boolean remove(IGravisEdge edge) {
        return graphDao.remove(edge);
    }
    
    public boolean remove(IGravisNode node) {
        ((GraphNode)node.getRelatedObject()).getShapes().remove(node);
        return graphDao.remove(node);
    }
    
    
    /**
     * Ideas for functionality.
     * 
     * unused shapes: remove all shapes not linked to node in petri net
     * node table overview: center view to on graph
     * 
     * copy node(s)
     * clone node(s)
     * delete node(s)
     * connect nodes
     * 
     * right clicking node: options for...
     */
}
