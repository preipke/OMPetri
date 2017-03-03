/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.model.dao.GraphDao;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCurveArrow;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphEdge;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphEdgeArrow;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.gnius.core.service.exception.ColourException;
import edu.unibi.agbi.gnius.core.service.exception.EdgeCreationException;
import edu.unibi.agbi.gnius.core.service.exception.NodeCreationException;
import edu.unibi.agbi.gnius.core.service.exception.AssignmentDeniedException;
import edu.unibi.agbi.gnius.util.Calculator;

import edu.unibi.agbi.petrinet.entity.PN_Element;
import edu.unibi.agbi.petrinet.exception.IllegalAssignmentException;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.PetriNet;

import java.util.Collection;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class DataGraphService
{
    @Autowired private Calculator calculator;
    @Autowired private SelectionService selectionService;
    
    private DataArc.Type defaultArcType = DataArc.Type.READ;
    private DataPlace.Type defaultPlaceType = DataPlace.Type.DISCRETE;
    private DataTransition.Type defaultTransitionType = DataTransition.Type.DISCRETE;
    
    private final GraphDao graphDao;
    private final DataDao petriNetDao;
    
    @Autowired
    public DataGraphService(GraphDao graphDao, DataDao petriNetDao) {
        this.graphDao = graphDao;
        this.petriNetDao = petriNetDao;
    }
    
    /**
     * Converts a given arc. 
     * @param shape
     * @throws AssignmentDeniedException 
     */
    private IGraphArc getConvertedGraphArc(IGraphArc shape) throws AssignmentDeniedException {
        
        IDataArc data = shape.getRelatedDataArc();
        IGraphArc shapeConverted;
        
        if (shape instanceof GraphEdgeArrow) {
            shapeConverted = new GraphCurveArrow(shape.getSource() , shape.getTarget() , data);
        } else {
            shapeConverted = new GraphEdgeArrow(shape.getSource() , shape.getTarget() , data);
        }
        shapeConverted.getElementHandles().forEach(ele -> {
            ele.setActiveStyleClass("gravisArc");
        });
        
        return shapeConverted;
    }
    
    /**
     * Create a new arc.
     * @param source
     * @param target
     * @throws EdgeCreationException 
     * @throws AssignmentDeniedException 
     */
    public void connect(IGraphNode source , IGraphNode target) throws EdgeCreationException , AssignmentDeniedException {
        
        IDataArc dataSourceToTarget;
        IGraphArc shapeSourceToTarget;
        
        try {
            dataSourceToTarget = new DataArc(source.getRelatedDataNode() , target.getRelatedDataNode() , defaultArcType);
        } catch (IllegalAssignmentException ex) {
            throw new AssignmentDeniedException(ex);
        }
        
        IDataNode dataSource = source.getRelatedDataNode();
        IDataNode dataTarget = target.getRelatedDataNode();
        
        IGraphNode relatedSourceShape;
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
        for (IGraphElement relatedSourceElement : dataSource.getShapes()) {
            
            relatedSourceShape = (IGraphNode) relatedSourceElement;
            
            for (int i = 0; i < relatedSourceShape.getChildren().size(); i++) {
                
                relatedSourceShapeChild = (IGraphNode) relatedSourceShape.getChildren().get(i);
                
                if (dataTarget == relatedSourceShapeChild.getRelatedDataNode()) {
                    throw new EdgeCreationException("A related node already connects those nodes!");
                    // TODO
                    // dialog to ask if connection should be removed and recreated at the current location
                }
            }
        }
        
        /**
         * Creating graph shape.
         */
        if (!source.getParents().contains(target)) {
            
            // Creates straight arc. Source and target are not yet linked.
            shapeSourceToTarget = new GraphEdgeArrow(source , target , dataSourceToTarget);
            
        } else { 
            
            // Creates curved arc. Target is parent of source already. Remove
            // straight arc from target to source and replace by curved arc.
            IGraphArc shapeTargetToSource = null;
            for (int i = 0; i < source.getEdges().size(); i++) {
                if (source.getEdges().get(i).getSource().equals(target)) {
                    shapeTargetToSource = (IGraphArc) source.getEdges().get(i);
                    break;
                }
            }
            
            // Converts existing shape.
            removeGraphArc(shapeTargetToSource);
            shapeTargetToSource = getConvertedGraphArc(shapeTargetToSource);
            addGraphArc(shapeTargetToSource);
            
            // Creating new shape.
            shapeSourceToTarget = new GraphCurveArrow(source , target , dataSourceToTarget);
        }
        shapeSourceToTarget.getElementHandles().forEach(ele -> { ele.setActiveStyleClass("gravisArc"); });
        
        // Adding shape.
        addGraphArc(shapeSourceToTarget);
    }
    
    /**
     * Creates arc. Binds it to the target node.
     * @param source 
     * @return  
     * @throws AssignmentDeniedException 
     */
    public GraphEdge createTemporaryArc(IGraphNode source) throws AssignmentDeniedException {
        
        GraphEdge shapeSourceToTarget;

        shapeSourceToTarget = new GraphEdge(source , null);
        shapeSourceToTarget.getElementHandles().forEach(ele -> {
            ele.setActiveStyleClass("gravisArc");
        });
        
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
    public void create(PN_Element.Type type , MouseEvent target , Point2D position) throws NodeCreationException , AssignmentDeniedException {
        
        IDataNode node;
        IGraphNode shape;
        
        switch(type) {
            case PLACE:
                node = new DataPlace(defaultPlaceType);
                shape = new GraphPlace(node);
                shape.getElementHandles().forEach(ele -> {
                    ele.setActiveStyleClass("gravisCircle");
                });
                break;
            case TRANSITION:
                node = new DataTransition(defaultTransitionType);
                shape = new GraphTransition(node);
                shape.getElementHandles().forEach(ele -> {
                    ele.setActiveStyleClass("gravisRectangle");
                });
                break;
            default:
                throw new NodeCreationException("No suitable node type selected!");
        }
        node.getShapes().add(shape);
        
        Point2D pos = calculator.getCorrectedMousePosition(target);
        shape.translateXProperty().set(pos.getX());
        shape.translateYProperty().set(pos.getY());
        
        graphDao.add(shape);
        petriNetDao.add(node);
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
                node = new DataPlace(((DataPlace)node).getPlaceType());
                copy = new GraphPlace(node);
                break;
            case TRANSITION:
                node = new DataTransition(((DataTransition)node).getTransitionType());
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
        
        List<IGraphNode> nodes = selectionService.getNodesCopy();
        IGraphNode node;
        
        Point2D center = calculator.getCenter(nodes);
        Point2D position = calculator.getCorrectedMousePositionLatest();
        
        try {
            for (int i = 0; i < nodes.size(); i++) {
                if (clone) {
                    node = clone(nodes.get(i));
                } else {
                    node = copy(nodes.get(i));
                }
                final String styleClass = nodes.get(i).getElementHandles().get(0).getActiveStyleClass();
                node.getElementHandles().forEach(ele -> {
                    ele.setActiveStyleClass(styleClass);
                });

                graphDao.add(node);
                selectionService.selectAll(node);
                
                node.translateXProperty().set(nodes.get(i).translateXProperty().get() - center.getX() + position.getX());
                node.translateYProperty().set(nodes.get(i).translateYProperty().get() - center.getY() + position.getY());
            }
        } catch (NodeCreationException | AssignmentDeniedException ex) {

        }
    }
    
    /**
     * Adds arc. Connects graph nodes and stores shape within the 
     * associated data object.
     * @param arc 
     */
    private void addGraphArc(IGraphArc arc) {
        
        IDataNode dataSource = arc.getRelatedDataArc().getSource();
        IDataNode dataTarget = arc.getRelatedDataArc().getTarget();
        
        /**
         * Checking for new connection.
         */
        if (petriNetDao.add(arc.getRelatedDataArc())) {
            dataSource.getArcsOut().add(arc.getRelatedDataArc());
            dataTarget.getArcsIn().add(arc.getRelatedDataArc());
        }
        
        arc.getSource().addChildNode(arc.getTarget());
        arc.getTarget().addParentNode(arc.getSource());
        arc.getSource().addEdge(arc);
        arc.getTarget().addEdge(arc);
        arc.getRelatedDataArc().getShapes().add(arc);
        graphDao.add(arc);
    }
    
    
    /**
     * Removes arc in the graph. Disconnects graph nodes and removes shape from
     * the associated data object. Converts arcs to straight edges.
     * @param arc 
     * @return  
     * @throws AssignmentDeniedException  
     */
    public IGraphArc removeGraphArc(IGraphArc arc) throws AssignmentDeniedException {
        
        graphDao.remove(arc);
        
        IGraphNode source = arc.getSource();
        IGraphNode target = arc.getTarget();
        
        if (target != null) { // null for temporary arcs

            arc.getRelatedDataArc().getShapes().remove(arc);

            source.getChildren().remove(target);
            source.removeConnection(arc);
            
            // Find double linked arc
            IGraphArc reverseArc;
            if (target.getChildren().contains(arc.getSource())) {
                for (int i = 0; i < target.getEdges().size(); i++) {
                    if (target.getEdges().get(i).getTarget().equals(source)) {
                        reverseArc = (IGraphArc)target.getEdges().get(i);
                        
                        // Convert arc
                        removeGraphArc(reverseArc);
                        reverseArc = getConvertedGraphArc(reverseArc);
                        addGraphArc(reverseArc);
                        break;
                    }
                }
            }
            
            target.getParents().remove(source);
            target.removeConnection(arc);
            
            removeDataArc(arc);
        }
        
        return arc;
    }
    
    /**
     * Validates and removes connection when no 
     * existing connection is found between graph 
     * elements.
     * @param arc 
     */
    private void removeDataArc(IGraphArc arc) {
        
        IDataArc darc = arc.getRelatedDataArc();
        
        IDataNode source = (IDataNode) darc.getSource();
        IDataNode target = (IDataNode) darc.getTarget();
        
        IGraphNode relatedShape;
        IGraphNode relatedShapeChild;
        
        // Validate all related shapes for any still existing connections between source and target
        for (IGraphElement shape : source.getShapes()) {
            
            relatedShape = (IGraphNode) shape;
            
            // Check related shape's children 
            for (int i = 0; i < relatedShape.getChildren().size(); i++) {
                
                relatedShapeChild = (IGraphNode) relatedShape.getChildren().get(i);
                
                // Related shape has an existing between source and target
                if (relatedShapeChild.getRelatedDataNode().equals(target)) {
                    return;
                }
            }
        }
        
        // No connection found. Remove!
        source.getArcsOut().remove(darc);
        target.getArcsIn().remove(darc);
        
        petriNetDao.getArcs().remove(darc);
    }
    
    public void removeGraphNode(IGraphNode node) {
        
        graphDao.remove(node);
        node.getRelatedDataNode().getShapes().remove(node);
        
        for (int i = 0; i < node.getEdges().size(); i++) {
            if (node.getEdges().get(i).getSource() == node) {
                node.getEdges().get(i).getTarget().removeParent(node);
                node.getEdges().get(i).getTarget().removeConnection(node.getEdges().get(i));
            } else {
                node.getEdges().get(i).getSource().removeChild(node);
                node.getEdges().get(i).getSource().removeConnection(node.getEdges().get(i));
            }
        }
        node.getEdges().clear();
    }
    
    public void removeSelectedShapes() throws AssignmentDeniedException {
        for (IGraphElement element : selectionService.getSelectedElements()) {
            if (element instanceof IGraphArc) {
                removeGraphArc((IGraphArc)element);
            } else {
                removeGraphNode((IGraphNode)element);
            }
        }
        selectionService.unselectAll();
    }
    
    public void add(Colour color) throws ColourException {
        if (!petriNetDao.add(color)) {
            throw new ColourException("The specified colour already exists!");
        }
    }
    
    public Collection<Colour> getColours() {
        return petriNetDao.getColours();
    }
    
    public PetriNet getPetriNet() {
        return petriNetDao;
    }
    
    public void setArcTypeDefault(DataArc.Type type) {
        defaultArcType = type;
    }
    
    public void setPlaceTypeDefault(DataPlace.Type type) {
        defaultPlaceType = type;
    }
    
    public void setTransitionTypeDefault(DataTransition.Type type) {
        defaultTransitionType = type;
    }
}
