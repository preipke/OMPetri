/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.dao.GraphDao;
import edu.unibi.agbi.gnius.core.dao.PetriNetDao;
import edu.unibi.agbi.gnius.core.model.entity.GraphEdge;
import edu.unibi.agbi.gnius.core.model.entity.GraphNode;
import edu.unibi.agbi.gnius.core.model.entity.GraphPlace;
import edu.unibi.agbi.gnius.core.model.entity.GraphTransition;
import edu.unibi.agbi.gnius.core.service.exception.EdgeCreationException;
import edu.unibi.agbi.gnius.core.service.exception.NodeCreationException;
import edu.unibi.agbi.gnius.util.Calculator;

import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisCircle;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisRectangle;
import edu.unibi.agbi.petrinet.model.entity.PN_Element;

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
    @Autowired private Calculator calculator;
    @Autowired private SelectionService selectionService;
    
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
    public IGravisNode create(PN_Element.Type type, MouseEvent target, Point2D position) throws NodeCreationException {
        
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
        node.getShapes().add(shape);
        
        //Point2D pos = calculator.getCorrectedMousePosition(target);
        
        if (target != null) { // TODO remove later - for testing purposes
            //shape.setTranslate(pos.getX(), pos.getY());
            shape.setTranslate(
                    (target.getX() - graphDao.getTopLayer().translateXProperty().get()) / graphDao.getTopLayer().getScale().getX() - shape.getOffsetX(), 
                    (target.getY() - graphDao.getTopLayer().translateYProperty().get()) / graphDao.getTopLayer().getScale().getY() - shape.getOffsetY()
            );
        }
        
        graphDao.add(shape);
        petriNetDao.add(node);
        
        return shape; // TODO remove later - for testing purposes
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
        node.getShapes().add(shape);
        
        graphDao.add(shape);
        petriNetDao.add(node);
    }
    
    public void connect(IGravisNode source, IGravisNode target) {
        
    }
    
    public IGravisNode copy(IGravisNode target) throws NodeCreationException {
        
        GraphNode node = (GraphNode) target.getRelatedObject();
        IGravisNode copy;
        
        switch(node.getElementType()) {
            case PLACE:
                node = new GraphPlace();
                copy = new GravisCircle(node);
                break;
            case TRANSITION:
                node = new GraphTransition();
                copy = new GravisRectangle(node);
                break;
            default:
                throw new NodeCreationException("No suitable edge type selected!");
        }
        
        node.getShapes().add(copy);
        
        return copy;
    }
    
    public IGravisNode clone(IGravisNode target) throws NodeCreationException {
        
        GraphNode node = (GraphNode) target.getRelatedObject();
        IGravisNode clone;
        
        switch(node.getElementType()) {
            case PLACE:
                clone = new GravisCircle(node);
                break;
            case TRANSITION:
                clone = new GravisRectangle(node);
                break;
            default:
                throw new NodeCreationException("No suitable edge type selected!");
        }
        
        node.getShapes().add(clone);
        
        return clone;
    }
    
    public void paste(boolean clone) {
        
        IGravisNode[] nodes = selectionService.getNodesCopy();
        IGravisNode node;
        
        Point2D center = calculator.getCenter(nodes);
        Point2D position = calculator.getCorrectedMousePositionLatest();
        
        try {
            for (int i = 0; i < nodes.length; i++) {
                if (clone) {
                    node = clone(nodes[i]);
                } else {
                    node = copy(nodes[i]);
                }
                node.setActiveStyleClass(nodes[i].getActiveStyleClass());

                add(node);
                selectionService.addAll(node);
                
                node.setTranslate(
                        nodes[i].getTranslateX() - center.getX() + position.getX() ,
                        nodes[i].getTranslateY() - center.getY() + position.getY()
                );
            }
        } catch (NodeCreationException ex) {

        }
    }
    
    public boolean remove(IGravisEdge edge) {
        return graphDao.remove(edge);
    }
    
    public boolean remove(IGravisNode node) {
        ((GraphNode)node.getRelatedObject()).getShapes().remove(node);
        return graphDao.remove(node);
    }
    
    public void removeSelected() {
        for (IGravisEdge edge : selectionService.getEdges()) {
            remove(edge);
        }
        for (IGravisNode node : selectionService.getNodes()) {
            remove(node);
        }
        selectionService.clear();
    }
}
