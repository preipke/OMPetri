/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.business.controller.tab.editor.EditorDetailsController;
import edu.unibi.agbi.gnius.core.model.dao.GraphDao;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
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
import edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisSubElement;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.exception.IllegalAssignmentException;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.PetriNet;
import java.util.Collection;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
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
    @Autowired private EditorDetailsController editorDetailsController;
    
    private final GraphDao graphDao;
    private final DataDao dataDao;
    
    private final String arcStyleClass = "arcDefault";
    private final String placeStyleClass = "placeDefault";
    private final String transitionDefaultStyleClass = "transitionDefault";
    private final String transitionStochasticStyleClass = "transitionStochastic";
    
    private DataArc.Type defaultArcType = DataArc.Type.READ;
    private DataPlace.Type defaultPlaceType = DataPlace.Type.CONTINUOUS;
    private DataTransition.Type defaultTransitionType = DataTransition.Type.CONTINUOUS;
    
    @Autowired
    public DataGraphService(GraphDao graphDao, DataDao petriNetDao) {
        this.graphDao = graphDao;
        this.dataDao = petriNetDao;
    }
    
    /**
     * Converts a given arc. 
     * @param shape
     * @throws AssignmentDeniedException 
     */
    private IGraphArc getConvertedGraphArc(IGraphArc shape) {
        
        DataArc data = shape.getDataElement();
        IGraphArc shapeConverted;
        
        if (shape instanceof GraphEdgeArrow) {
            shapeConverted = new GraphCurveArrow(shape.getSource() , shape.getTarget() , data);
        } else {
            shapeConverted = new GraphEdgeArrow(shape.getSource() , shape.getTarget() , data);
        }
        shapeConverted.getElementHandles().forEach(ele -> {
            ele.setActiveStyleClass(arcStyleClass);
        });
        
        return shapeConverted;
    }
    
    /**
     * Create a new arc.
     * @param source
     * @param target
     * @throws DataGraphServiceException 
     */
    public void connect(IGraphNode source , IGraphNode target) throws DataGraphServiceException {
        
        DataArc dataSourceToTarget;
        IGraphArc shapeSourceToTarget;
        
        try {
            dataSourceToTarget = new DataArc(source.getDataElement() , target.getDataElement() , defaultArcType);
        } catch (IllegalAssignmentException ex) {
            throw new DataGraphServiceException(ex.toString());
        }
        
        IDataNode dataSource = source.getDataElement();
        IDataNode dataTarget = target.getDataElement();
        
        IGraphNode relatedSourceShape;
        IGraphNode relatedSourceShapeChild;
        
        /**
         * Ensures the connection to be valid. 
         */
        if (source.getClass().equals(target.getClass())) {
            throw new DataGraphServiceException("Cannot connect nodes of the same type!");
        }
        if (source.getChildren().contains(target)) {
            throw new DataGraphServiceException("Nodes are already connected!");
        } 
        for (IGraphElement relatedSourceElement : dataSource.getShapes()) {
            
            relatedSourceShape = (IGraphNode) relatedSourceElement;
            
            for (int i = 0; i < relatedSourceShape.getChildren().size(); i++) {
                
                relatedSourceShapeChild = (IGraphNode) relatedSourceShape.getChildren().get(i);
                
                if (dataTarget == relatedSourceShapeChild.getDataElement()) {
                    throw new DataGraphServiceException("A related node already connects those nodes!");
                    // TODO : dialog to ask if connection should be removed and recreated at the current location
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
            for (int i = 0; i < source.getConnections().size(); i++) {
                if (source.getConnections().get(i).getSource().equals(target)) {
                    shapeTargetToSource = (IGraphArc) source.getConnections().get(i);
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
        shapeSourceToTarget.getElementHandles().forEach(ele -> { ele.setActiveStyleClass(arcStyleClass); });
        
        // Adding shape.
        addGraphArc(shapeSourceToTarget);
    }
    
    /**
     * Creates arc. Binds it to the target node.
     * @param source 
     * @return  
     */
    public GraphEdge createTemporaryArc(IGraphNode source) {
        
        GraphEdge shapeSourceToTarget;

        shapeSourceToTarget = new GraphEdge(source , null, null);
        shapeSourceToTarget.getElementHandles().forEach(ele -> {
            ele.setActiveStyleClass(arcStyleClass);
        });
        
        graphDao.add(shapeSourceToTarget);
        
        return shapeSourceToTarget;
    }
    
    /**
     * Creates a node of the specified type at the given event position.
     * @param type
     * @param target
     * @throws DataGraphServiceException 
     */
    public void create(Element.Type type , MouseEvent target) throws DataGraphServiceException {
        
        IGraphNode shape;
        
        switch(type) {
            case PLACE:
                shape = createPlaceShape(new DataPlace(defaultPlaceType));
                break;
            case TRANSITION:
                shape = createTransitionShape(new DataTransition(defaultTransitionType));
                break;
            default:
                throw new DataGraphServiceException("Cannot create element of an undefined type!");
        }
        
        Point2D pos = calculator.getCorrectedMousePosition(target);
        shape.translateXProperty().set(pos.getX());
        shape.translateYProperty().set(pos.getY());
        
        graphDao.add(shape);
        dataDao.add(shape.getDataElement());
    }
    
    /**
     * Copy the target node.
     * @param target
     * @return
     * @throws DataGraphServiceException
     */
    public IGraphNode copy(IGraphNode target) throws DataGraphServiceException {
        
        IGraphNode copy;
        IDataNode node = target.getDataElement();
        
        switch(node.getElementType()) {
            case PLACE:
                copy = createPlaceShape(new DataPlace(((DataPlace)node).getPlaceType()));
                break;
            case TRANSITION:
                copy = createTransitionShape(new DataTransition(((DataTransition)node).getTransitionType()));
                break;
            default:
                throw new DataGraphServiceException("Cannot copy element of an undefined type!");
        }
        
        return copy;
    }
    
    /**
     * Clone the target node.
     * @param target
     * @return
     * @throws DataGraphServiceException
     */
    public IGraphNode clone(IGraphNode target) throws DataGraphServiceException {
        
        IGraphNode clone;
        IDataNode node = target.getDataElement();
        
        switch(node.getElementType()) {
            case PLACE:
                clone = createPlaceShape((DataPlace)node);
                break;
            case TRANSITION:
                clone = createTransitionShape((DataTransition)node);
                break;
            default:
                throw new DataGraphServiceException("Cannot clone element of an undefined type!");
        }
        
        node.getShapes().add(clone);
        
        return clone;
    }
    
    /**
     * Pastes selected nodes. Either copies or clones nodes, inserting them
     * at the latest mouse pointer location.
     * @param clone
     * @throws DataGraphServiceException 
     */
    public void paste(boolean clone) throws DataGraphServiceException {
        
        List<IGraphNode> nodes = selectionService.getNodesCopy();
        IGraphNode shape;
        
        Point2D center = calculator.getCenter(nodes);
        Point2D position = calculator.getCorrectedMousePositionLatest();
        
        for (int i = 0; i < nodes.size(); i++) {
            
            if (clone) {
                shape = clone(nodes.get(i));
            } else {
                shape = copy(nodes.get(i));
            }

            graphDao.add(shape);
            selectionService.selectAll(shape);

            shape.translateXProperty().set(nodes.get(i).translateXProperty().get() - center.getX() + position.getX());
            shape.translateYProperty().set(nodes.get(i).translateYProperty().get() - center.getY() + position.getY());
        }
    }
    
    /**
     * Removes arc from the scene. Disconnects graph nodes and removes shapes 
     * from their associated data object. Converts curved to straight arc.
     * @param arc 
     * @return
     */
    public IGraphArc removeGraphArc(IGraphArc arc) {
        
        graphDao.remove(arc);
        
        IGraphNode source = arc.getSource();
        IGraphNode target = arc.getTarget();
        
        if (target != null) { // check null for temporary arcs

            arc.getDataElement().getShapes().remove(arc);

            source.getChildren().remove(target);
            source.removeConnection(arc);
            
            target.getParents().remove(source);
            target.removeConnection(arc);
            
            // Find and convert double linked arc
            IGraphArc reverseArc;
            if (target.getChildren().contains(arc.getSource())) {
                for (int i = 0; i < target.getConnections().size(); i++) {
                    if (target.getConnections().get(i).getTarget().equals(source)) {
                        reverseArc = (IGraphArc)target.getConnections().get(i);
                        
                        // Convert arc
                        removeGraphArc(reverseArc);
                        reverseArc = getConvertedGraphArc(reverseArc);
                        addGraphArc(reverseArc);
                        break;
                    }
                }
            }
            
            validateDataArc(arc);
        }
        
        return arc;
    }
    
    /**
     * Removes the given graph node from the scene and data storage.
     * @param node 
     */
    public void removeGraphNode(IGraphNode node) {
        
        graphDao.remove(node);
        node.getDataElement().getShapes().remove(node);
        
        for (int i = 0; i < node.getConnections().size(); i++) {
            if (node.getConnections().get(i).getSource() == node) {
                node.getConnections().get(i).getTarget().removeParent(node);
                node.getConnections().get(i).getTarget().removeConnection(node.getConnections().get(i));
            } else {
                node.getConnections().get(i).getSource().removeChild(node);
                node.getConnections().get(i).getSource().removeConnection(node.getConnections().get(i));
            }
        }
        node.getConnections().clear();
    }
    
    /**
     * Removes elements marked as selected from the scene and data.
     */
    public void removeSelectedShapes() {
        for (IGraphElement element : selectionService.getSelectedElements()) {
            if (element instanceof IGraphArc) {
                removeGraphArc((IGraphArc)element);
            } else {
                removeGraphNode((IGraphNode)element);
            }
        }
        selectionService.unselectAll();
    }
    
    /**
     * Sets the specified type within the given data element.
     * @param arc
     * @param type 
     * @throws edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException 
     */
    public void setTypeFor(DataArc arc, DataArc.Type type) throws DataGraphServiceException {
        
        // TODO
        
        switch (type) {
            case EQUAL:
                throw new DataGraphServiceException("Arc styling not yet implemented!");
            case INHIBITORY:
                throw new DataGraphServiceException("Arc styling not yet implemented!");
            case READ:
                throw new DataGraphServiceException("Arc styling not yet implemented!");
            case RESET:
                throw new DataGraphServiceException("Arc styling not yet implemented!");
            default:
                throw new DataGraphServiceException("Cannot create shape for an undefined arc type!");
        }

//        arc.setArcType(type);
    }
    
    /**
     * Sets the specified type for the given data element. Styles all related
     * shapes accordingly.
     * @param place
     * @param type 
     * @throws edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException 
     */
    public void setTypeFor(DataPlace place, DataPlace.Type type) throws DataGraphServiceException {

        switch (type) {
            case CONTINUOUS:
                setElementStyle(place, placeStyleClass, true);
                break;
            case DISCRETE:
                setElementStyle(place, placeStyleClass, false);
                break;
            default:
                throw new DataGraphServiceException("Cannot create shape for an undefined place type!");
        }
        
        place.setPlaceType(type);
    }
    
    /**
     * Sets the specified type for the given data element. Styles all related
     * shapes accordingly.
     * @param transition
     * @param type 
     * @throws edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException 
     */
    public void setTypeFor(DataTransition transition, DataTransition.Type type) throws DataGraphServiceException {

        switch (type) {
            case CONTINUOUS:
                setElementStyle(transition, transitionDefaultStyleClass, true);
                break;
            case DISCRETE:
                setElementStyle(transition, transitionDefaultStyleClass, false);
                break;
            case STOCHASTIC:
                setElementStyle(transition, transitionStochasticStyleClass, false);
                break;
            default:
                throw new DataGraphServiceException("Cannot create shape for an undefined transition type!");
        }
        
        transition.setTransitionType(type);
    }
    
    private IGraphNode createPlaceShape(DataPlace node) throws DataGraphServiceException {
        
        IGraphNode shape = new GraphPlace(node);
        node.getShapes().add(shape);
        
        setTypeFor(node, defaultPlaceType);
        
        return shape;
    }
    
    private IGraphNode createTransitionShape(DataTransition node) throws DataGraphServiceException {
        
        IGraphNode shape = new GraphTransition(node);
        node.getShapes().add(shape);
        
        setTypeFor(node, defaultTransitionType);
        
        return shape;
    }
    
    /**
     * Adds arc. Connects graph nodes and stores shape within the 
     * associated data object.
     * @param arc 
     */
    private void addGraphArc(IGraphArc arc) {
        
        IDataNode dataSource = arc.getDataElement().getSource();
        IDataNode dataTarget = arc.getDataElement().getTarget();
        
        /**
         * Checking for new connection.
         */
        if (dataDao.add(arc.getDataElement())) {
            dataSource.getArcsOut().add(arc.getDataElement());
            dataTarget.getArcsIn().add(arc.getDataElement());
        }
        
        arc.getSource().addChildNode(arc.getTarget());
        arc.getTarget().addParentNode(arc.getSource());
        arc.getSource().addConnection(arc);
        arc.getTarget().addConnection(arc);
        arc.getDataElement().getShapes().add(arc);
        graphDao.add(arc);
    }
    
    /**
     * Validates the data arc related to the given graph arc. Removes it when 
     * no remaining connection is found between any graph elements within the 
     * scene.
     * @param arc 
     */
    private void validateDataArc(IGraphArc arc) {
        
        IDataArc dataArc = arc.getDataElement();
        
        IDataNode source = (IDataNode) dataArc.getSource();
        IDataNode target = (IDataNode) dataArc.getTarget();
        
        IGraphNode relatedShape;
        IGraphNode relatedShapeChild;
        
        // Validate all related shapes for any still existing connections between source and target
        for (IGraphElement shape : source.getShapes()) {
            
            relatedShape = (IGraphNode) shape;
            
            // Check related shape's children 
            for (int i = 0; i < relatedShape.getChildren().size(); i++) {
                
                relatedShapeChild = (IGraphNode) relatedShape.getChildren().get(i);
                
                // Related shape has an existing connection between source and target
                if (relatedShapeChild.getDataElement().equals(target)) {
                    return; // this should never occur in the current state! PR
                }
            }
        }
        
        // No connection found. Remove!
        source.getArcsOut().remove(dataArc);
        target.getArcsIn().remove(dataArc);
        
        dataDao.getArcs().remove(dataArc);
    }
    
    /**
     * Sets the style for a data element and changes the style of all related
     * shapes accordingly.
     * @param dataElement
     * @param styleClass active css style
     * @param childrenEnabled wether scene element's children are to be shown
     */
    private void setElementStyle(IDataElement dataElement, String styleClass, boolean childrenEnabled) {
        
        for (IGraphElement shapeElement : dataElement.getShapes()) {
            
            shapeElement.getElementHandles().forEach(ele -> {
                ele.setActiveStyleClass(styleClass);
            });
            
            for (IGravisSubElement childShapes : ((IGraphNode)shapeElement).getChildElements()) {
                
                for (Shape shape : childShapes.getShapes()) {
                    shape.setVisible(childrenEnabled);
                }
            }
        }
    }
    
    public void UpdateData() throws DataGraphServiceException {
        editorDetailsController.StoreElementProperties();
    }
    
    public void add(Colour color) throws ColourException {
        if (!dataDao.add(color)) {
            throw new ColourException("The specified colour already exists!");
        }
    }
    
    public Collection<Colour> getColours() {
        return dataDao.getColours();
    }
    
    public PetriNet getDataDao() {
        return dataDao;
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
