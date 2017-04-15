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
import edu.unibi.agbi.petrinet.exception.IdConflictException;

/**
 *
 * @author PR
 */
@Service
public class DataGraphService {

    @Autowired private Calculator calculator;
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
     * Changes the subtype for the given arc to the given type. Styles all
     * related shapes in the scene accordingly.
     * @param arc
     * @param type
     * @throws DataGraphServiceException
     */
    public void changeArcType(DataArc arc, DataArc.Type type) throws DataGraphServiceException {
        arc.setArcType(type);
        setArcStyle(arc);
    }

    /**
     * Changes the subtype for the given place to the given type. Styles all
     * related shapes in the scene accordingly.
     * @param place
     * @param type
     * @throws DataGraphServiceException
     */
    public void changePlaceType(DataPlace place, DataPlace.Type type) throws DataGraphServiceException {
        place.setPlaceType(type);
        setPlaceStyle(place);
    }

    /**
     * Changes the subtype for the given transition to the given type. Styles all
     * related shapes in the scene accordingly.
     * @param transition
     * @param type
     * @throws DataGraphServiceException
     */
    public void changeTransitionType(DataTransition transition, DataTransition.Type type) throws DataGraphServiceException {
        transition.setTransitionType(type);
        setTransitionStyle(transition);
    }

    /**
     * Clones the given node. Results in a node of the same type that references
     * the data object of the given node.
     * 
     * @param target
     * @return
     * @throws DataGraphServiceException
     */
    public IGraphNode clone(IGraphNode target) throws DataGraphServiceException {
        IDataNode node = target.getDataElement();
        switch (node.getElementType()) {
            case PLACE:
                return new GraphPlace((DataPlace) node);
            case TRANSITION:
                return new GraphTransition((DataTransition) node);
            default:
                throw new DataGraphServiceException("Cannot copy element of undefined type!");
        }
    }

    /**
     * Groups and hides all given nodes in a cluster element.
     * @param selected
     * @throws DataGraphServiceException
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
            throw new DataGraphServiceException("Cannot make a single element a cluster!");
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

                if (!nodes.contains(tmp.getSource())) { // source is OUTSIDE
                    if (!arcsToCluster.contains(tmp)) {
                        arcsToCluster.add(tmp);
                    }
                } else if (!nodes.contains(tmp.getTarget())) { // target is OUTSIDE
                    if (!arcsFromCluster.contains(tmp)) {
                        arcsFromCluster.add(tmp);
                    }
                } 
                if (!arcs.contains(tmp)) {
                    arcs.add(tmp);
                }
            }
        }
        
        // Create cluster object
        clusterData = new DataCluster(nodes, arcs);
        clusterShape = new GraphCluster(clusterData);
        
        clusterData.getGraphElements().add(clusterShape);
        graphDao.add(clusterShape);
        setNodeStyle(clusterData, clusterStyleClass, true);

        Point2D pos = calculator.getCenter(nodes);
        clusterShape.translateXProperty().set(pos.getX());
        clusterShape.translateYProperty().set(pos.getY());
        
        for (IGraphArc arc : arcsFromCluster) {
            
            arc.getDataElement().getGraphElements().remove(arc);
            
            arc = createConnection(clusterShape, arc.getTarget(), arc.getDataElement());
            add(arc);
        }
        for (IGraphArc arc : arcsToCluster) {
            
            arc.getDataElement().getGraphElements().remove(arc);
            
            arc = createConnection(arc.getSource(), clusterShape, arc.getDataElement());
            add(arc);
        }
        
        for (IGraphNode node : nodes) {
            graphDao.remove(node);
        }
        for (IGraphArc arc : arcs) {
            graphDao.remove(arc);
        }
    }
    
    /**
     * Connects the given graph nodes. Validates the connection, then creates
     * and adds a new graph arc to the scene.
     * @param source
     * @param target
     * @return
     * @throws DataGraphServiceException 
     */
    public IGraphArc connect(IGraphNode source, IGraphNode target) throws DataGraphServiceException {
        validateConnection(source, target);
        return add(createConnection(source, target, null));
    }

    /**
     * Creates a copy of the given node. Results in a node of the same type
     * as the given node.
     * 
     * @param target
     * @return
     * @throws DataGraphServiceException
     */
    public IGraphNode copy(IGraphNode target) throws DataGraphServiceException {
        IDataNode node = target.getDataElement();
        switch (node.getElementType()) {
            case PLACE:
                return new GraphPlace(new DataPlace(((DataPlace) node).getPlaceType()));
            case TRANSITION:
                return new GraphTransition(new DataTransition(((DataTransition) node).getTransitionType()));
            default:
                throw new DataGraphServiceException("Cannot copy element of undefined type!");
        }
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
                shape = new GraphPlace(new DataPlace(defaultPlaceType));
                break;
            case TRANSITION:
                shape = new GraphTransition(new DataTransition(defaultTransitionType));
                break;
            default:
                throw new DataGraphServiceException("Cannot create element of undefined type!");
        }
        Point2D pos = calculator.getCorrectedMousePosition(posX, posY);
        shape.translateXProperty().set(pos.getX());
        shape.translateYProperty().set(pos.getY());
        
        return add(shape);
    }

    /**
     * Creates arc. Binds it to the target node.
     *
     * @param source
     * @return
     */
    public GraphEdge createTemporaryArc(IGraphNode source) {

        GraphEdge edge;
        edge = new GraphEdge(source, null, null);
        edge.getElementHandles().forEach(ele -> {
            ele.setActiveStyleClass(arcStyleClass);
        });
        graphDao.add(edge);

        return edge;
    }
    
    /**
     * Removes the given graph arc from the scene.
     * @param arc
     * @return
     * @throws DataGraphServiceException 
     */
    public IGraphArc remove(IGraphArc arc) throws DataGraphServiceException {
        arc = removeShape(arc);
        validateData(arc.getDataElement());
        return arc;
    }
    
    /**
     * Removes the given graph node from the scene. Also removes all arcs 
     * connected to the given node.
     * @param node
     * @return
     * @throws DataGraphServiceException 
     */
    public IGraphNode remove(IGraphNode node) throws DataGraphServiceException {
        for (IGraphArc arc : node.getGraphConnections()) {
            remove(arc);
        }
        node = removeShape(node);
        validateData(node.getDataElement());
        return node;
    }

    /**
     * Removes the given graph elements from the scene. Also removes all 
     * connections to the given nodes.
     * @param elements
     * @throws DataGraphServiceException
     */
    public void remove(List<IGraphElement> elements) throws DataGraphServiceException {
        for (IGraphElement element : elements) {
            if (element instanceof IGraphArc) {
                remove((IGraphArc) element);
            } else {
                remove((IGraphNode) element);
            }
        }
    }

    /**
     * Restores and shows all the nodes stored within the given cluster(s).
     * @param selected
     * @throws DataGraphServiceException
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
        
        for (GraphCluster cluster : clusters) {
            
            clusteredArcs = cluster.getDataElement().getClusteredArcs();
            clusteredNodes = cluster.getDataElement().getClusteredNodes();
            
            for (IGraphArc arc : clusteredArcs) {
                if (dataDao.getArcs().contains(arc.getDataElement())) {
                    arc.getDataElement().getGraphElements().remove(0);
                    arc.getDataElement().getGraphElements().add(arc);
                    graphDao.add(arc);
                }
            }
            for (IGraphNode node : clusteredNodes) {
                if (dataDao.getPlacesAndTransitions().contains(node.getDataElement())) {
                    node.getDataElement().getGraphElements().remove(0);
                    node.getDataElement().getGraphElements().add(node);
                    graphDao.add(node);
                }
            }
            for (IGraphArc arc : cluster.getGraphConnections()) {
                removeShape(arc);
                validateData(arc.getDataElement());
            }
            graphDao.remove(cluster);
        }
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
            add(shape);

            shape.translateXProperty().set(nodes.get(i).translateXProperty().get() - center.getX() + position.getX());
            shape.translateYProperty().set(nodes.get(i).translateYProperty().get() - center.getY() + position.getY());
            
            shapes.add(shape);
        }
        return shapes;
    }

    /**
     * Adds arc to scene and data model. 
     * @param arc
     * @return 
     */
    private IGraphArc add(IGraphArc arc) throws DataGraphServiceException {
        if (arc.getDataElement() != null) {
            arc.getDataElement().getGraphElements().add(arc);
            try {
                dataDao.add(arc.getDataElement());
            } catch (IdConflictException ex) {
                throw new DataGraphServiceException(ex.getMessage());
            }
        }
        graphDao.add(arc);
        styleElement(arc);
        return arc;
    }
    
    /**
     * Adds node to scene and data model.
     * @param node
     * @return 
     */
    private IGraphNode add(IGraphNode node) throws DataGraphServiceException {
        if (node.getDataElement() != null) {
            node.getDataElement().getGraphElements().add(node);
            try {
                dataDao.add(node.getDataElement());
            } catch (IdConflictException ex) {
                throw new DataGraphServiceException(ex.getMessage());
            }
        }
        graphDao.add(node);
        styleElement(node);
        return node;
    }

    /**
     * Converts a given arc from straight to curved or curved to straight.
     * @param shape
     * @return 
     * @throws DataGraphServiceException
     */
    private IGraphArc convertArcShape(IGraphArc shape) throws DataGraphServiceException {
        remove(shape);
        if (shape instanceof GraphEdgeArrow) {
            shape = new GraphCurveArrow(shape.getSource(), shape.getTarget(), shape.getDataElement());
        } else {
            shape = new GraphEdgeArrow(shape.getSource(), shape.getTarget(), shape.getDataElement());
        }
        return add(shape);
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
    private IGraphArc createConnection(IGraphNode source, IGraphNode target, DataArc dataArc) throws DataGraphServiceException {
        
        try {
            if (dataArc == null) {
                dataArc = new DataArc(source.getDataElement(), target.getDataElement(), defaultArcType);
            }
        } catch (IllegalAssignmentException ex) {
            throw new DataGraphServiceException(ex.toString());
        }

        IGraphArc shapeSourceToTarget;

        /**
         * Creating shape.
         */
        if (target == null || !source.getParents().contains(target)) {

            // Create arc, source and target are not yet connected in any way.
            shapeSourceToTarget = new GraphEdgeArrow(source, target, dataArc);

        } else {

            // Find arc shape reversly connecting nodes
            IGraphArc shapeTargetToSource = null;
            for (int i = 0; i < source.getConnections().size(); i++) {
                if (source.getConnections().get(i).getSource().equals(target)) {
                    shapeTargetToSource = (IGraphArc) source.getConnections().get(i);
                    break;
                }
            }
            // Convert existing arc shape
            convertArcShape(shapeTargetToSource);

            // Create arc
            shapeSourceToTarget = new GraphCurveArrow(source, target, dataArc);
        }

        // Adding shape.
        return shapeSourceToTarget;
    }

    /**
     * Removes the given graph arc from the scene. In case of double linked
     * connections the remaining connection will be converted.
     *
     * @param arc
     * @return
     * @throws DataGraphServiceException
     */
    private IGraphArc removeShape(IGraphArc arc) throws DataGraphServiceException {

        graphDao.remove(arc);
        if (arc.getDataElement() != null) {
            arc.getDataElement().getGraphElements().remove(arc);
        }

        // Check null for temporary arcs
        if (arc.getTarget() == null) { 
            return arc;
        }
        IGraphNode source = arc.getSource();
        IGraphNode target = arc.getTarget();

        // Find and convert double linked arc
        IGraphArc reverseArc;
        if (target.getChildren().contains(arc.getSource())) {
            for (int i = 0; i < target.getConnections().size(); i++) {
                if (target.getConnections().get(i).getTarget().equals(source)) {
                    reverseArc = (IGraphArc) target.getConnections().get(i);
                    convertArcShape(reverseArc);
                    break;
                }
            }
        }
        return arc;
    }

    /**
     * Removes the given graph node from the scene.
     *
     * @param node
     * @return 
     * @throws edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException 
     */
    private IGraphNode removeShape(IGraphNode node) throws DataGraphServiceException {
        graphDao.remove(node);
        if (node.getDataElement() != null) {
            node.getDataElement().getGraphElements().remove(node);
        }
        return node;
    }

    /**
     * Sets the given style class to all elements in the scene that are related
     * to the given data element.
     * @param dataElement
     * @param styleClass
     * @param childrenEnabled wether scene element's children are to be shown
     */
    private void setNodeStyle(IDataElement dataElement, String styleClass, boolean childrenEnabled) {
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
     * Styles the graph element in the scene according to the type assigned to
     * its data element.
     * @param element
     * @throws DataGraphServiceException 
     */
    private void styleElement(IGraphElement element) throws DataGraphServiceException {
        switch (element.getDataElement().getElementType()) {
            case ARC:
                setArcStyle((DataArc)element.getDataElement());
                break;
            case PLACE:
                setPlaceStyle((DataPlace)element.getDataElement());
                break;
            case TRANSITION:
                setTransitionStyle((DataTransition)element.getDataElement());
                break;
            default:
                throw new DataGraphServiceException("Cannot style element of undefined type!");
        }
    }
    
    private void setArcStyle(DataArc arc) throws DataGraphServiceException {
        String styleClass;
        switch (arc.getArcType()) {
            case EQUAL:
                throw new DataGraphServiceException("Arc styling for EQUAL arcs not yet implemented!");
            case INHIBITORY:
                throw new DataGraphServiceException("Arc styling for INHIBITORY arcs not yet implemented!");
            case READ:
                styleClass = arcStyleClass;
                break;
            case RESET:
                throw new DataGraphServiceException("Arc styling for RESET arcs not yet implemented!");
            default:
                throw new DataGraphServiceException("Cannot create shape for an undefined arc type!");
        }
        for (IGraphElement element : arc.getGraphElements()) {
            element.getElementHandles().forEach(ele -> {
                ele.setActiveStyleClass(styleClass);
            });
        }
    }
    
    private void setPlaceStyle(DataPlace place) throws DataGraphServiceException {
        switch (place.getPlaceType()) {
            case CONTINUOUS:
                setNodeStyle(place, placeStyleClass, true);
                break;
            case DISCRETE:
                setNodeStyle(place, placeStyleClass, false);
                break;
            default:
                throw new DataGraphServiceException("Cannot create shape for an undefined place type!");
        }
    }
    
    private void setTransitionStyle(DataTransition transition) throws DataGraphServiceException {
        switch (transition.getTransitionType()) {
            case CONTINUOUS:
                setNodeStyle(transition, transitionDefaultStyleClass, true);
                break;
            case DISCRETE:
                setNodeStyle(transition, transitionDefaultStyleClass, false);
                break;
            case STOCHASTIC:
                setNodeStyle(transition, transitionStochasticStyleClass, false);
                break;
            default:
                throw new DataGraphServiceException("Cannot create shape for an undefined transition type!");
        }
    }
    
    /**
     * Validates a connection between two graph nodes. 
     * @param source
     * @param target
     * @throws DataGraphServiceException thrown in case the connection is not valid
     */
    private void validateConnection(IGraphNode source, IGraphNode target) throws DataGraphServiceException {

        IDataNode dataSource = source.getDataElement();
        IDataNode dataTarget = target.getDataElement();
        
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
                    throw new DataGraphServiceException("Another element already connects those nodes!");
                }
            }
        }
    }

    /**
     * Validates the given data node. Removes it from the data model when no 
     * remaining reference can be found within the scene.
     *
     * @param arc
     */
    private void validateData(IDataArc arc) throws DataGraphServiceException {
        if (arc == null) {
            return;
        }
        if (arc.getGraphElements().isEmpty()) {
            dataDao.remove(arc);
        }
    }
    
    /**
     * Validates the given data arc. Removes it from the data model when no
     * remaining reference can be found within the scene.
     *
     * @param arc
     */
    private void validateData(IDataNode node) {
        if (node == null) {
            return;
        }
        if (node.getGraphElements().isEmpty()) {
            dataDao.remove(node);
        }
    }

    public void UpdateData() throws DataGraphServiceException {
        elementDetailsController.StoreElementProperties();
    }

    public void add(Colour color) throws DataGraphServiceException {
        try {
            dataDao.add(color);
        } catch (IdConflictException ex) {
            throw new DataGraphServiceException(ex.getMessage());
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
