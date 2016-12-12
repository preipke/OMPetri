/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.dao.GraphDao;
import edu.unibi.agbi.gnius.core.dao.PetriNetDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.gnius.core.service.exception.ColourException;
import edu.unibi.agbi.gnius.core.service.exception.EdgeCreationException;
import edu.unibi.agbi.gnius.core.service.exception.NodeCreationException;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gnius.core.service.exception.RelationChangeDeniedException;

import edu.unibi.agbi.petrinet.entity.PN_Element;
import edu.unibi.agbi.petrinet.model.Colour;
import java.util.Collection;

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
    public void add(IGraphNode node) {
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
    public IGraphNode create(PN_Element.Type type , MouseEvent target , Point2D position) throws NodeCreationException , RelationChangeDeniedException {
        
        IDataNode node;
        IGraphNode shape;
        
        switch(type) {
            case PLACE:
                node = new DataPlace();
                shape = new GraphPlace(node);
                shape.setActiveStyleClass("gravisCircle");
                break;
            case TRANSITION:
                node = new DataTransition();
                shape = new GraphTransition(node);
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
    public void create(IGraphNode source , IGraphNode target) throws EdgeCreationException , NodeCreationException , RelationChangeDeniedException {
        
        DataArc node;
        IGraphArc shape;

        node = new DataArc();
        shape = new GraphArc(source , target , node);
        shape.setActiveStyleClass("gravisEdge");
        node.getShapes().add(shape);
        
        graphDao.add(shape);
        petriNetDao.add(node);
    }
    
    public void connect(IGraphNode source, IGraphNode target) {
        
    }
    
    public IGraphNode copy(IGraphNode target) throws NodeCreationException , RelationChangeDeniedException {
        
        IDataNode node = target.getRelatedDataNode();
        IGraphNode copy;
        
        switch(node.getElementType()) {
            case PLACE:
                node = new DataPlace();
                copy = new GraphPlace(node);
                break;
            case TRANSITION:
                node = new DataTransition();
                copy = new GraphTransition(node);
                break;
            default:
                throw new NodeCreationException("No suitable edge type selected!");
        }
        
        node.getShapes().add(copy);
        
        return copy;
    }
    
    public IGraphNode clone(IGraphNode target) throws NodeCreationException , RelationChangeDeniedException {
        
        IDataNode node = target.getRelatedDataNode();
        IGraphNode clone;
        
        switch(node.getElementType()) {
            case PLACE:
                clone = new GraphPlace(node);
                break;
            case TRANSITION:
                clone = new GraphTransition(node);
                break;
            default:
                throw new NodeCreationException("No suitable edge type selected!");
        }
        
        node.getShapes().add(clone);
        
        return clone;
    }
    
    public void paste(boolean clone) {
        
        IGraphNode[] nodes = selectionService.getNodesCopy();
        IGraphNode node;
        
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
                selectionService.selectAll(node);
                
                node.setTranslate(
                        nodes[i].getTranslateX() - center.getX() + position.getX() ,
                        nodes[i].getTranslateY() - center.getY() + position.getY()
                );
            }
        } catch (NodeCreationException | RelationChangeDeniedException ex) {

        }
    }
    
    public boolean remove(IGraphArc edge) {
        return graphDao.remove(edge);
    }
    
    public boolean remove(IGraphNode node) {
        node.getRelatedDataNode().getShapes().remove(node);
        return graphDao.remove(node);
    }
    
    public void removeSelected() {
        for (IGraphArc edge : selectionService.getSelectedArcs()) {
            remove(edge);
        }
        for (IGraphNode node : selectionService.getSelectedNodes()) {
            remove(node);
        }
        selectionService.clear();
    }
    
    public void add(Colour color) throws ColourException {
        if (!petriNetDao.add(color)) {
            throw new ColourException("The specified colour already exists!");
        }
    }
    
    public Collection<Colour> getColours() {
        return petriNetDao.getColours();
    }
}
