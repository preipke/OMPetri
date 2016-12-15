/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.dao.GraphDao;
import edu.unibi.agbi.gnius.core.dao.PetriNetDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphEdge;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCurve;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.gnius.core.service.exception.ColourException;
import edu.unibi.agbi.gnius.core.service.exception.EdgeCreationException;
import edu.unibi.agbi.gnius.core.service.exception.NodeCreationException;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gnius.core.service.exception.AssignmentDeniedException;

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
     * Create a new arc.
     * @param source
     * @param target
     * @return 
     * @throws EdgeCreationException 
     * @throws AssignmentDeniedException 
     */
    public IGraphArc connect(IGraphNode source , IGraphNode target) throws EdgeCreationException , AssignmentDeniedException {
        
        DataArc node;
        IGraphArc shape;

        node = new DataArc();
        shape = new GraphEdge(source , target , node);
        shape.setActiveStyleClass("gravisArc");
//        shape = new GraphCurve(source , target , node);
//        shape.setActiveStyleClass("gravisArc");
        node.getShapes().add(shape);

        graphDao.add(shape);
        if (target != null) {
            petriNetDao.add(node);
        }
        
        return shape;
    }
    
    /**
     * Connect arc to node.
     * @param arc
     * @param target 
     * @throws AssignmentDeniedException 
     */
    public void connect(IGraphArc arc , IGraphNode target) throws AssignmentDeniedException {
        
        if (target != null && target != arc.getSource()) {
            arc.setTarget(target);
            petriNetDao.add(target.getRelatedDataNode());
        } else {
            graphDao.remove(arc);
        }
        
    }
    
    /**
     * Creates a new node.
     * @param type
     * @param target
     * @param position
     * @throws NodeCreationException 
     */
    public IGraphNode create(PN_Element.Type type , MouseEvent target , Point2D position) throws NodeCreationException , AssignmentDeniedException {
        
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
     * Copy the target node.
     * @param target
     * @return
     * @throws NodeCreationException
     * @throws AssignmentDeniedException 
     */
    public IGraphNode copy(IGraphNode target) throws NodeCreationException , AssignmentDeniedException {
        
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
    
    /**
     * Clone the target node.
     * @param target
     * @return
     * @throws NodeCreationException
     * @throws AssignmentDeniedException 
     */
    public IGraphNode clone(IGraphNode target) throws NodeCreationException , AssignmentDeniedException {
        
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
        } catch (NodeCreationException | AssignmentDeniedException ex) {

        }
    }
    
    public void add(IDataNode node) {
        petriNetDao.add(node);
    }
    
    public void add(IGraphNode node) {
        graphDao.add(node);
    }
    
    public void add(Colour color) throws ColourException {
        if (!petriNetDao.add(color)) {
            throw new ColourException("The specified colour already exists!");
        }
    }
    
    public Collection<Colour> getColours() {
        return petriNetDao.getColours();
    }
    
    public boolean remove(IGraphArc edge) {
        edge.getRelatedDataArc().getShapes().remove(edge);
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
        selectionService.unselectAll();
    }
}
