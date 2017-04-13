/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.business.controller.ElementDetailsController;
import edu.unibi.agbi.gnius.core.model.dao.GraphDao;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataCluster;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCurveArrow;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphEdge;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphEdgeArrow;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.gnius.core.service.exception.ColourException;
import edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.exception.IllegalAssignmentException;
import edu.unibi.agbi.petrinet.model.Colour;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import edu.unibi.agbi.gravisfx.entity.IGravisChildElement;

/**
 *
 * @author PR
 */
@Service
public class DataGraphService {

    @Autowired private Calculator calculator;
    @Autowired private SelectionService selectionService;
    @Autowired private MessengerService messengerService;
    @Autowired private ElementDetailsController elementDetailsController;

    private final GraphDao graphDao;
    private final DataDao dataDao;

    @Value("${css.arc.default}")
    private String arcStyleClass;
    @Value("${css.cluster.default}")
    private String clusterStyleClass;
    @Value("${css.place.default}")
    private String placeStyleClass;
    @Value("${css.transition.default}")
    private String transitionDefaultStyleClass;
    @Value("${css.transition.stochastic}")
    private String transitionStochasticStyleClass;

    private DataArc.Type defaultArcType = DataArc.Type.READ;
    private DataPlace.Type defaultPlaceType = DataPlace.Type.CONTINUOUS;
    private DataTransition.Type defaultTransitionType = DataTransition.Type.CONTINUOUS;

    @Autowired
    public DataGraphService(GraphDao graphDao, DataDao dataDao) {
        this.graphDao = graphDao;
        this.dataDao = dataDao;
    }

    /**
     * Clusters and hides all given nodes in a new cluster.
     * 
     * @param selected
     * @throws edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException
     */
    public void cluster(List<IGraphElement> selected) throws DataGraphServiceException {
        
        if (selected.isEmpty()) {
            throw new DataGraphServiceException("Nothing was selected for clustering!");
        }
        
        List<IGraphNode> nodes = new ArrayList();

        for (IGraphElement element : selected) {
            if (element instanceof IGraphNode) {
                nodes.add((IGraphNode) element);
            } else {
                messengerService.addToLog("Unsuitable element for clustering! (" + element.toString() + ")");
            }
        }
        if (nodes.isEmpty()) {
            throw new DataGraphServiceException("No suitable elements selected for clustering!");
        } else if (nodes.size() == 1) {
            throw new DataGraphServiceException("It is not suitable to make a single element a cluster!");
        }
        
        List<IGraphArc> arcs = new ArrayList();
        List<IGraphArc> arcsToCluster = new ArrayList();
        List<IGraphArc> arcsFromCluster = new ArrayList();

        DataCluster clusterData;
        GraphCluster clusterShape;
        IGraphArc tmp;

        // Find connections to nodes outside of the cluster
        for (IGraphNode node : nodes) {
            
            for (IGravisConnection connection : node.getConnections()) {
                
                tmp = (IGraphArc) connection;

                if (!nodes.contains(tmp.getSource())) { // connection source is NOT INSIDE the cluster
                    if (!arcsToCluster.contains(tmp)) {
                        arcsToCluster.add(tmp);
                    }
                } else if (!nodes.contains(tmp.getTarget())) { // connection target is NOT INSIDE the cluster
                    if (!arcsFromCluster.contains(tmp)) {
                        arcsFromCluster.add(tmp);
                    }
                } 
                if (!arcs.contains(tmp)) {
                    arcs.add(tmp);
                }
            }
        }
        
        // Create cluster objects
        clusterData = new DataCluster(nodes, arcs);
        clusterShape = new GraphCluster(clusterData);
        setElementStyle(clusterData, clusterStyleClass, true);
        
        clusterData.getGraphElements().add(clusterShape);
        graphDao.add(clusterShape);

        Point2D pos = calculator.getCenter(nodes);
        clusterShape.translateXProperty().set(pos.getX());
        clusterShape.translateYProperty().set(pos.getY());
        
        for (IGraphArc arc : arcsFromCluster) {
            tmp = connect(clusterShape, arc.getTarget(), arc.getDataElement());
            arc.getDataElement().getGraphElements().add(tmp);
        }
        for (IGraphArc arc : arcsToCluster) {
            tmp = connect(arc.getSource(), clusterShape, arc.getDataElement());
            arc.getDataElement().getGraphElements().add(tmp);
        }
        
        for (IGraphNode node : nodes) {
            graphDao.remove(node);
        }
        for (IGraphArc arc : arcs) {
            graphDao.remove(arc);
        }
    }

    /**
     * Restores and shows all the nodes stored within the given cluster(s).
     * @param selected
     * @throws edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException
     */
    public void uncluster(List<IGraphElement> selected) throws DataGraphServiceException {
        
        if (selected.isEmpty()) {
            throw new DataGraphServiceException("Nothing was selected for unclustering!");
        }
        
        List<GraphCluster> clusters = new ArrayList();

        for (IGraphElement element : selected) {
            if (element instanceof GraphCluster) {
                clusters.add((GraphCluster) element);
            } else {
                messengerService.addToLog("Unsuitable element selected for unclustering! (" + element.toString() + ")");
            }
        }
        if (clusters.isEmpty()) {
            throw new DataGraphServiceException("No cluster was selected!");
        } 
        
        List<IGraphArc> clusteredArcs;
        List<IGraphNode> clusteredNodes;
        List<IGraphArc> arcsToRemove;
        IGraphArc tmp;
        
        for (GraphCluster cluster : clusters) {
            
            for (IGraphArc arc : cluster.getGraphConnections()) {
                removeShape(arc);
            }
            
            graphDao.remove(cluster);
            
            clusteredArcs = cluster.getDataElement().getClusteredArcs();
            clusteredNodes = cluster.getDataElement().getClusteredNodes();
            
            for (IGraphArc arc : clusteredArcs) {
                if (arc.getDataElement().getGraphElements().size() > 1) {
                    arcsToRemove = new ArrayList();
                    for (IGraphElement element : arc.getDataElement().getGraphElements()) {
                        tmp = (IGraphArc) element;
                        if (graphDao.contains(tmp)) {
                            arcsToRemove.add(tmp);
                            graphDao.remove(tmp);
                        } else {
                            graphDao.add(tmp);
                        }
                    }
                    arc.getDataElement().getGraphElements().removeAll(arcsToRemove);
                } else {
                    graphDao.add(arc);
                }
            }
            for (IGraphNode node : clusteredNodes) {
                graphDao.add(node);
            }
        }
    }

    /**
     * Creates an arc connecting the given nodes. Only works if nodes are not
     * already connected by any related nodes in the scene.
     *
     * @param source
     * @param target
     * @param dataArc
     * @return 
     * @throws DataGraphServiceException
     */
    public IGraphArc connect(IGraphNode source, IGraphNode target, DataArc dataArc) throws DataGraphServiceException {

        IDataNode dataSource = source.getDataElement();
        IDataNode dataTarget = target.getDataElement();

        try {
            if (dataArc == null) {
                dataArc = new DataArc(source.getDataElement(), target.getDataElement(), defaultArcType);
            }
        } catch (IllegalAssignmentException ex) {
            throw new DataGraphServiceException(ex.toString());
        }

        IGraphArc shapeSourceToTarget;
        IGraphNode relatedSourceShape;
        IGraphNode relatedSourceShapeChild;

        /**
         * Ensuring the connection to be valid.
         */
        if (source.getClass().equals(target.getClass())) {
            throw new DataGraphServiceException("Cannot connect nodes of the same type!");
        }
        if (source instanceof GraphCluster || target instanceof GraphCluster) {
            throw new DataGraphServiceException("Cannot connect to a cluster without specifying the exact nodes!");
        }
        if (source.getChildren().contains(target) || target.getParents().contains(source)) {
            throw new DataGraphServiceException("Nodes are already connected!");
        }
        for (IGraphElement relatedSourceElement : dataSource.getGraphElements()) {

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
         * Creating shape.
         */
        if (!source.getParents().contains(target)) {

            // Creates straight arc. Source and target are not yet linked.
            shapeSourceToTarget = new GraphEdgeArrow(source, target, dataArc);

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
            removeShape(shapeTargetToSource);
            shapeTargetToSource = getConvertedGraphArc(shapeTargetToSource);
            addShape(shapeTargetToSource);

            // Creating new shape.
            shapeSourceToTarget = new GraphCurveArrow(source, target, dataArc);
        }
        shapeSourceToTarget.getElementHandles().forEach(ele -> {
            ele.setActiveStyleClass(arcStyleClass);
        });

        // Adding shape.
        return addShape(shapeSourceToTarget);
    }

    /**
     * Creates arc. Binds it to the target node.
     *
     * @param source
     * @return
     */
    public GraphEdge createTemporaryArc(IGraphNode source) {

        GraphEdge shapeSourceToTarget;

        shapeSourceToTarget = new GraphEdge(source, null, null);
        shapeSourceToTarget.getElementHandles().forEach(ele -> {
            ele.setActiveStyleClass(arcStyleClass);
        });

        graphDao.add(shapeSourceToTarget);

        return shapeSourceToTarget;
    }

    /**
     * Creates a node of the specified type at the given event position.
     *
     * @param type
     * @param posX
     * @param posY
     * @return 
     * @throws DataGraphServiceException
     */
    public IGraphNode create(Element.Type type, double posX, double posY) throws DataGraphServiceException {

        IGraphNode shape;

        switch (type) {
            case PLACE:
                shape = createPlaceShape(new DataPlace(defaultPlaceType));
                break;
            case TRANSITION:
                shape = createTransitionShape(new DataTransition(defaultTransitionType));
                break;
            default:
                throw new DataGraphServiceException("Cannot create element of undefined type!");
        }

        Point2D pos = calculator.getCorrectedMousePosition(posX, posY);
        shape.translateXProperty().set(pos.getX());
        shape.translateYProperty().set(pos.getY());

        graphDao.add(shape);
        dataDao.add(shape.getDataElement());
        
        return shape;
    }

    /**
     * Copies the target node.
     *
     * @param target
     * @return
     * @throws DataGraphServiceException
     */
    public IGraphNode copy(IGraphNode target) throws DataGraphServiceException {

        IGraphNode copy;
        IDataNode node = target.getDataElement();

        switch (node.getElementType()) {
            case PLACE:
                copy = createPlaceShape(new DataPlace(((DataPlace) node).getPlaceType()));
                break;
            case TRANSITION:
                copy = createTransitionShape(new DataTransition(((DataTransition) node).getTransitionType()));
                break;
            default:
                throw new DataGraphServiceException("Cannot copy element of undefined type!");
        }

        return copy;
    }

    /**
     * Clones the target node.
     *
     * @param target
     * @return
     * @throws DataGraphServiceException
     */
    public IGraphNode clone(IGraphNode target) throws DataGraphServiceException {

        IGraphNode clone;
        IDataNode node = target.getDataElement();

        switch (node.getElementType()) {
            case PLACE:
                clone = createPlaceShape((DataPlace) node);
                break;
            case TRANSITION:
                clone = createTransitionShape((DataTransition) node);
                break;
            default:
                throw new DataGraphServiceException("Cannot clone element of undefined type!");
        }
        node.getGraphElements().add(clone);

        return clone;
    }

    /**
     * Pastes given node(s). Either copies or clones nodes, inserting them at
     * the latest mouse pointer location.
     *
     * @param nodes
     * @param clone
     * @return 
     * @throws DataGraphServiceException
     */
    public List<IGraphNode> paste(List<IGraphNode> nodes, boolean clone) throws DataGraphServiceException {

        Point2D center = calculator.getCenter(nodes);
        Point2D position = calculator.getCorrectedMousePositionLatest();

        List<IGraphNode> shapes = new ArrayList();
        IGraphNode shape;

        for (int i = 0; i < nodes.size(); i++) {

            if (clone) {
                shape = clone(nodes.get(i));
            } else {
                shape = copy(nodes.get(i));
            }

            graphDao.add(shape);

            shape.translateXProperty().set(nodes.get(i).translateXProperty().get() - center.getX() + position.getX());
            shape.translateYProperty().set(nodes.get(i).translateYProperty().get() - center.getY() + position.getY());
            
            shapes.add(shape);
        }
        return shapes;
    }

    /**
     * Adds arc. Connects graph nodes and stores shape within the associated
     * data object.
     *
     * @param arc
     * @return 
     */
    public IGraphArc addShape(IGraphArc arc) {

        if (arc.getDataElement() != null) {

            IDataNode dataSource = arc.getDataElement().getSource();
            IDataNode dataTarget = arc.getDataElement().getTarget();
            
            /**
             * Checking for new connection.
             */
            if (dataDao.add(arc.getDataElement())) {
                dataSource.getArcsOut().add(arc.getDataElement());
                dataTarget.getArcsIn().add(arc.getDataElement());
            }
            arc.getDataElement().getGraphElements().add(arc);
        }
        graphDao.add(arc);
        
        return arc;
    }

    /**
     * Converts a given arc from straight to curved / curved to straight.
     *
     * @param shape
     * @throws AssignmentDeniedException
     */
    private IGraphArc getConvertedGraphArc(IGraphArc shape) {

        DataArc data = shape.getDataElement();
        IGraphArc shapeConverted;

        if (shape instanceof GraphEdgeArrow) {
            shapeConverted = new GraphCurveArrow(shape.getSource(), shape.getTarget(), data);
        } else {
            shapeConverted = new GraphEdgeArrow(shape.getSource(), shape.getTarget(), data);
        }
        shapeConverted.getElementHandles().forEach(ele -> {
            ele.setActiveStyleClass(arcStyleClass);
        });

        return shapeConverted;
    }

    /**
     * Removes arc from the scene. Disconnects graph nodes and removes shapes
     * from their associated data object. Converts curved to straight arc.
     *
     * @param arc
     * @return
     */
    public IGraphArc removeShape(IGraphArc arc) {

        graphDao.remove(arc);

        IGraphNode source = arc.getSource();
        IGraphNode target = arc.getTarget();

        if (target == null) { // check null for temporary arcs
            return arc;
        }

        if (arc.getDataElement() != null) {
            arc.getDataElement().getGraphElements().remove(arc);
        }

        // Find and convert double linked arc
        IGraphArc reverseArc;
        if (target.getChildren().contains(arc.getSource())) {
            for (int i = 0; i < target.getConnections().size(); i++) {
                if (target.getConnections().get(i).getTarget().equals(source)) {
                    reverseArc = (IGraphArc) target.getConnections().get(i);

                    // Convert arc
                    removeShape(reverseArc);
                    reverseArc = getConvertedGraphArc(reverseArc);
                    addShape(reverseArc);
                    break;
                }
            }
        }
        validateArc(arc);

        return arc;
    }

    /**
     * Removes the given graph node from the scene and data storage.
     *
     * @param node
     * @return 
     */
    public IGraphNode removeShape(IGraphNode node) {
        for (IGraphArc arc : node.getGraphConnections()) {
            removeShape(arc);
        }
        node.getDataElement().getGraphElements().remove(node);
        graphDao.remove(node);
        validateNode(node);
        return node;
    }

    /**
     * Removes the given elements from the scene.
     * @param shapes
     */
    public void removeShapes(List<IGraphElement> shapes) {
        for (IGraphElement element : shapes) {
            if (element instanceof IGraphArc) {
                removeShape((IGraphArc) element);
            } else {
                removeShape((IGraphNode) element);
            }
        }
    }
    
    /**
     * Validates the data related to the given graph node. Removes it when no
     * remaining reference is found to any graph element within the scene.
     *
     * @param arc
     */
    private void validateNode(IGraphNode node) {

        IDataNode dataNode = node.getDataElement();
        
        if (dataNode == null) {
            return;
        }
        
        if (dataNode.getGraphElements().isEmpty()) {
            dataDao.remove(dataNode);
        }
    }

    /**
     * Validates the data related to the given graph arc. Removes it when no
     * remaining reference is found to any graph element within the scene.
     *
     * @param arc
     */
    private void validateArc(IGraphArc arc) {

        IDataArc dataArc = arc.getDataElement();
        
        if (dataArc == null) {
            return;
        }

        IDataNode source = (IDataNode) dataArc.getSource();
        IDataNode target = (IDataNode) dataArc.getTarget();

        IGraphNode relatedShape;
        IGraphNode relatedShapeChild;

        // Validate all related shapes for any existing connections between source and target
        for (IGraphElement shape : source.getGraphElements()) {

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
     *
     * @param dataElement
     * @param styleClass active css style
     * @param childrenEnabled wether scene element's children are to be shown
     */
    private void setElementStyle(IDataElement dataElement, String styleClass, boolean childrenEnabled) {

        for (IGraphElement shapeElement : dataElement.getGraphElements()) {

            shapeElement.getElementHandles().forEach(ele -> {
                ele.setActiveStyleClass(styleClass);
            });

            for (IGravisChildElement childShapes : ((IGraphNode) shapeElement).getChildElements()) {

                for (Shape shape : childShapes.getShapes()) {
                    shape.setVisible(childrenEnabled);
                }
            }
        }
    }

    /**
     * Sets the specified type within the given data element.
     *
     * @param arc
     * @param type
     * @throws
     * edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException
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
     *
     * @param place
     * @param type
     * @throws
     * edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException
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
     *
     * @param transition
     * @param type
     * @throws
     * edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException
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
        node.getGraphElements().add(shape);

        setTypeFor(node, defaultPlaceType);

        return shape;
    }

    private IGraphNode createTransitionShape(DataTransition node) throws DataGraphServiceException {

        IGraphNode shape = new GraphTransition(node);
        node.getGraphElements().add(shape);

        setTypeFor(node, defaultTransitionType);

        return shape;
    }

    public void UpdateData() throws DataGraphServiceException {
        elementDetailsController.StoreElementProperties();
    }

    public void add(Colour color) throws ColourException {
        if (!dataDao.add(color)) {
            throw new ColourException("The specified colour already exists!");
        }
    }

    public Collection<Colour> getColours() {
        return dataDao.getColours();
    }

    public DataDao getDataDao() {
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
