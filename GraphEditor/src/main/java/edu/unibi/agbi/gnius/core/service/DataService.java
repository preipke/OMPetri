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
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphEdge;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCurve;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.gnius.core.service.exception.ColourException;
import edu.unibi.agbi.gnius.core.service.exception.EdgeCreationException;
import edu.unibi.agbi.gnius.core.service.exception.NodeCreationException;
import edu.unibi.agbi.gnius.core.service.exception.AssignmentDeniedException;
import edu.unibi.agbi.gnius.util.Calculator;

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
     * Removes and replaces given arc. Takes care of graph object connections
     * and the integrity within the associated data object.
     * @param shape
     * @throws AssignmentDeniedException 
     */
    private void convert(IGraphArc shape) throws AssignmentDeniedException {
        
        IDataArc data = shape.getRelatedDataArc();
        IGraphArc shapeConverted;
        
        if (shape instanceof GraphEdge) {
            shapeConverted = new GraphCurve(shape.getSource() , shape.getTarget() , data);
        } else {
            shapeConverted = new GraphEdge(shape.getSource() , shape.getTarget() , data);
        }
        shapeConverted.setActiveStyleClass("gravisArc");
        
        remove(shape);
        add(shapeConverted);
    }
    
    /**
     * Create a new arc.
     * @param source
     * @param target
     * @throws EdgeCreationException 
     * @throws AssignmentDeniedException 
     */
    public void connect(IGraphNode source , IGraphNode target) throws EdgeCreationException , AssignmentDeniedException {
        
        IDataArc dataSourceToTarget = new DataArc();
        IDataNode dataSourceNode = source.getRelatedDataNode();
        IDataNode dataTargetNode = target.getRelatedDataNode();
        IGraphArc shapeSourceToTarget;
        IGraphNode relatedSourceShapeChild;
        
        /**
         * Ensures the connection to be valid. 
         */
        
        if (source.getClass().equals(target.getClass())) {
            throw new EdgeCreationException("Cannot connect nodes of the same type!");
        }
        if (source.getChildren().contains(target)) {
            throw new EdgeCreationException("Nodes are already connected!");
        } 
        for (IGraphNode relatedSourceShape : dataSourceNode.getShapes()) {
            
            for (int i = 0; i < relatedSourceShape.getChildren().size(); i++) {
                
                relatedSourceShapeChild = (IGraphNode) relatedSourceShape.getChildren().get(i);
                
                if (dataTargetNode == relatedSourceShapeChild.getRelatedDataNode()) {
                    throw new EdgeCreationException("A related node already connects those nodes!");
                }
            }
        }
        
        if (!source.getParents().contains(target)) {
            
            /**
             * Creates straight arc. Source and target are not yet linked.
             */
            
            shapeSourceToTarget = new GraphEdge(source , target , dataSourceToTarget);
            
        } else { 
            
            /**
             * Creates curved arc. Target is parent of source already. Remove
             * straight arc from target to source and replace by curved arc.
             */
            
            IGraphArc shapeTargetToSource = null;
            
            for (int i = 0; i < source.getEdges().size(); i++) {
                
                if (source.getEdges().get(i).getSource().equals(target)) {
                    
                    shapeTargetToSource = (IGraphArc) source.getEdges().get(i);
                    break;
                }
            }
            
            convert(shapeTargetToSource);
            
            shapeSourceToTarget = new GraphCurve(source , target , dataSourceToTarget);
        }
        shapeSourceToTarget.setActiveStyleClass("gravisArc");
        
        add(shapeSourceToTarget);
        petriNetDao.add(dataSourceToTarget);
    }
    
    /**
     * Creates arc. Binds it to the target node.
     * @param source 
     * @return  
     * @throws AssignmentDeniedException 
     */
    public IGraphArc createTemporaryArc(IGraphNode source) throws AssignmentDeniedException {
        
        IGraphArc shapeSourceToTarget;

        shapeSourceToTarget = new GraphEdge(source , null);
        shapeSourceToTarget.setActiveStyleClass("gravisArc");
        
        graphDao.add(shapeSourceToTarget);
        
        return shapeSourceToTarget;
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

                graphDao.add(node);
                selectionService.selectAll(node);
                
                node.setTranslate(
                        nodes[i].getTranslateX() - center.getX() + position.getX() ,
                        nodes[i].getTranslateY() - center.getY() + position.getY()
                );
            }
        } catch (NodeCreationException | AssignmentDeniedException ex) {

        }
    }
    
    /**
     * Adds arc. Connects graph nodes and stores shape within the 
     * associated data object.
     * @param arc 
     */
    private void add(IGraphArc arc) {
        arc.getSource().addChildNode(arc.getTarget());
        arc.getTarget().addParentNode(arc.getSource());
        arc.getSource().addEdge(arc);
        arc.getTarget().addEdge(arc);
        arc.getRelatedDataArc().getShapes().add(arc);
        graphDao.add(arc);
    }
    
    
    /**
     * Removes arc. Disconnects graph nodes and removes shape from the 
     * associated data object.
     * @param arc 
     * @return  
     */
    public IGraphArc remove(IGraphArc arc) {
        
        graphDao.remove(arc);
        
        IGraphNode source = arc.getSource();
        IGraphNode target = arc.getTarget();
        
        if (target != null) { // is null for temporary arcs

            arc.getRelatedDataArc().getShapes().remove(arc);

            source.getChildren().remove(target);
            target.getParents().remove(source);

            source.removeEdge(arc);
            target.removeEdge(arc);

        }
        
        return arc;
    }
    
    public void remove(IGraphNode node) {
        
        graphDao.remove(node);
        node.getRelatedDataNode().getShapes().remove(node);
        
        for (int i = 0; i < node.getEdges().size(); i++) {
            if (node.getEdges().get(i).getSource() == node) {
                node.getEdges().get(i).getTarget().removeParent(node);
                node.getEdges().get(i).getTarget().removeEdge(node.getEdges().get(i));
            } else {
                node.getEdges().get(i).getSource().removeChild(node);
                node.getEdges().get(i).getSource().removeEdge(node.getEdges().get(i));
            }
        }
        node.getEdges().clear();
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
