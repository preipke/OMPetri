/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.core.exception.ParameterServiceException;
import edu.unibi.agbi.gnius.core.model.dao.GraphDao;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataCluster;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataClusterArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCurve;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphEdge;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class DataGraphService
{
    private static final String ID_PREFIX_PLACE = "P";
    private static final String ID_PREFIX_TRANSITION = "T";
    
    @Autowired private Calculator calculator;
    @Autowired private MessengerService messengerService;
    @Autowired private ParameterService parameterService;

    @Value("${css.arc.default}") private String styleArcDefault;
    @Value("${css.arc.default.child}") private String styleArcDefaultHead;
    @Value("${css.arc.inhi}") private String styleArcInhi;
    @Value("${css.arc.inhi.child}") private String styleArcInhiHead;
    @Value("${css.arc.test}") private String styleArcTest;
    @Value("${css.arc.test.child}") private String styleArcTestHead;

    @Value("${css.cluster.default}") private String stylceCluster;
    @Value("${css.clusterarc.default}") private String styleClusterArc;

    @Value("${css.place.default}") private String stylePlace;

    @Value("${css.transition.default}") private String styleTransitionDefault;
    @Value("${css.transition.stochastic}") private String styleTransitionStoch;

    private final GraphDao graphDao;
    private final DataDao dataDao;

    private DataArc.Type defaultArcType = DataArc.Type.NORMAL;
    private DataPlace.Type defaultPlaceType = DataPlace.Type.CONTINUOUS;
    private DataTransition.Type defaultTransitionType = DataTransition.Type.CONTINUOUS;

    @Autowired
    public DataGraphService(GraphDao graphDao, DataDao dataDao) {
        this.graphDao = graphDao;
        this.dataDao = dataDao;
    }

    /**
     * Adds a colour.
     *
     * @param colour
     * @throws DataGraphServiceException
     */
    public void add(Colour colour) throws DataGraphServiceException {
        if (dataDao.getColours().contains(colour)) {
            throw new DataGraphServiceException("Conflict! Another colour has already been stored using the same ID!");
        }
        dataDao.add(colour);
    }

    /**
     * Changes the subtype of an arc. Styles all related shapes in the scene
     * accordingly.
     *
     * @param arc
     * @param type
     * @throws DataGraphServiceException
     */
    public void changeArcType(DataArc arc, DataArc.Type type) throws DataGraphServiceException {
        validateArcType(arc, type);
        arc.setArcType(type);
        styleArc(arc);
    }

    /**
     * Changes the subtype of a place. Styles all related shapes in the scene
     * accordingly.
     *
     * @param place
     * @param type
     * @throws DataGraphServiceException
     */
    public void changePlaceType(DataPlace place, DataPlace.Type type) throws DataGraphServiceException {
        DataPlace.Type typeOld = place.getPlaceType();
        place.setPlaceType(type);
        try {
            for (IArc arc : place.getArcsIn()) {
                validateArcType(arc, arc.getArcType());
            }
            for (IArc arc : place.getArcsOut()) {
                validateArcType(arc, arc.getArcType());
            }
            stylePlace(place);
        } catch (DataGraphServiceException ex) {
            place.setPlaceType(typeOld);
            messengerService.addToLog("Cannot change place type to '" + type + "'! [" + ex.getMessage() + "]");
            throw new DataGraphServiceException("Cannot change place type to '" + type + "'!");
        }
    }

    /**
     * Changes the subtype of a transition. Styles all related shapes in the
     * scene accordingly.
     *
     * @param transition
     * @param type
     * @throws DataGraphServiceException
     */
    public void changeTransitionType(DataTransition transition, DataTransition.Type type) throws DataGraphServiceException {
        DataTransition.Type typeOld = transition.getTransitionType();
        transition.setTransitionType(type);
        try {
            for (IArc arc : transition.getArcsIn()) {
                validateArcType(arc, defaultArcType);
            }
            for (IArc arc : transition.getArcsOut()) {
                validateArcType(arc, defaultArcType);
            }
            styleTransition(transition);
        } catch (DataGraphServiceException ex) {
            transition.setTransitionType(typeOld);
            messengerService.addToLog("Cannot change place type to '" + type + "'! [" + ex.getMessage() + "]");
            throw new DataGraphServiceException("Cannot change place type to '" + type + "'!");
        }
    }

    /**
     * Connects the given graph nodes. Validates the connection, then creates
     * and adds a new graph arc to the scene.
     *
     * @param source
     * @param target
     * @return
     * @throws DataGraphServiceException
     */
    public IGraphArc connect(IGraphNode source, IGraphNode target) throws DataGraphServiceException {
        IGraphArc arc;
        validateConnection(source, target);
        arc = createConnection(source, target, null);
        validateArcType(arc.getDataElement(), defaultArcType);
        return add(arc);
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
                shape = new GraphPlace(
                            new DataPlace(
                                ID_PREFIX_PLACE + dataDao.getNextPlaceId(),
                                defaultPlaceType));
                break;
            case TRANSITION:
                shape = new GraphTransition(
                            new DataTransition(
                                ID_PREFIX_TRANSITION + dataDao.getNextTransitionId(),
                                defaultTransitionType));
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
     * Creates an arc that binds its source to the given node.
     *
     * @param source
     * @return
     */
    public GraphEdge createTemporaryArc(IGraphNode source) {

        GraphEdge edge;
        edge = new GraphEdge(source, null);
        edge.getParentElementHandles().forEach(ele -> {
            ele.setActiveStyleClass(styleArcDefault);
        });
        edge.getChildElementHandles().forEach(ele -> {
            ele.setActiveStyleClass(styleArcDefaultHead);
        });
        edge.setArrowHeadVisible(true);
        edge.setCircleHeadVisible(false);
        graphDao.add(edge);

        return edge;
    }

    /**
     * Groups and hides elements in a cluster element. Removes any existing
     * groups within the selection before creating the group.
     *
     * @param selected
     * @return
     * @throws DataGraphServiceException
     */
    public GraphCluster group(List<IGraphElement> selected) throws DataGraphServiceException {

        if (selected.isEmpty()) {
            throw new DataGraphServiceException("Nothing was selected for clustering!");
        }

        List<IGraphCluster> clusters = new ArrayList();
        List<IGraphNode> nodes = new ArrayList();
        List<IGraphArc> arcs = new ArrayList();

        for (IGraphElement element : selected) {
            switch (element.getDataElement().getElementType()) {
                case CLUSTER:
                    clusters.add((IGraphCluster) element);
                    break;
                case PLACE:
                    nodes.add((IGraphNode) element);
                    break;
                case TRANSITION:
                    nodes.add((IGraphNode) element);
                    break;
                default:
                    messengerService.addToLog("Unsuitable element for clustering! (" + element.toString() + ")");
                    break;
            }
        }
        if (nodes.size() + clusters.size() <= 1) {
            throw new DataGraphServiceException("Not enough elements selected for clustering!");
        }

        // Combine nodes and arcs from existing clusters.
        for (IGraphCluster cluster : clusters) {
            arcs.addAll(cluster.getDataElement().getClusteredArcs());
            nodes.addAll(cluster.getDataElement().getClusteredNodes());
            clusterRemove(cluster);
        }

        return clusterCreate(nodes, arcs);
    }

    /**
     * Removes the given graph arc from the scene.
     *
     * @param arc
     * @return
     * @throws DataGraphServiceException
     */
    public IGraphArc remove(IGraphArc arc) throws DataGraphServiceException {
        validateRemoval(arc);
        removeShape(arc);
        removeData(arc.getDataElement());
        return arc;
    }

    /**
     * Removes the given graph node from the scene. Also removes all arcs
     * connected to the given node.
     *
     * @param node
     * @return
     * @throws DataGraphServiceException
     */
    public IGraphNode remove(IGraphNode node) throws DataGraphServiceException {
        IGraphArc arc;
        for (IGravisConnection connection : node.getConnections()) {
            validateRemoval((IGraphArc) connection);
        }
        validateRemoval(node);
        while (!node.getConnections().isEmpty()) {
            arc = (IGraphArc) node.getConnections().get(0);
            removeShape(arc);
            removeData(arc.getDataElement());
        }
        removeShape(node);
        removeData(node.getDataElement());
        return node;
    }

    /**
     * Removes the given graph elements from the scene. Also removes all
     * connections to the given nodes.
     *
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
     * Restores and shows all the nodes stored within the given cluster(s).
     *
     * @param selected
     * @throws DataGraphServiceException
     */
    public void ungroup(List<IGraphElement> selected) throws DataGraphServiceException {

        if (selected.isEmpty()) {
            throw new DataGraphServiceException("Nothing was selected for unclustering!");
        }

        List<IGraphCluster> clusters = new ArrayList();

        for (IGraphElement element : selected) {
            switch (element.getDataElement().getElementType()) {
                case CLUSTER:
                    clusters.add((IGraphCluster) element);
                    break;
                default:
                    messengerService.addToLog("Unsuitable element selected for unclustering! (" + element.toString() + ")");
            }
        }

        for (IGraphCluster cluster : clusters) {
            clusterRemove(cluster);
        }
    }

    /**
     * Adds arc to scene and data model.
     *
     * @param arc
     * @return
     */
    private IGraphArc add(IGraphArc arc) throws DataGraphServiceException {
        if (arc.getDataElement() != null) {
            if (arc.getDataElement().getElementType() == Element.Type.ARC) {
                if (dataDao.containsAndNotEqual(arc.getDataElement())) {
                    throw new DataGraphServiceException("Conflict! Another arc has already been stored using the same ID!");
                }
                dataDao.add(arc.getDataElement());
            }
            arc.getDataElement().getShapes().add(arc);
        }
        graphDao.add(arc);
        styleElement(arc);
        return arc;
    }

    /**
     * Adds node to scene and data model.
     *
     * @param node
     * @return
     */
    private IGraphNode add(IGraphNode node) throws DataGraphServiceException {
        if (node.getDataElement() != null) {
            if (node.getDataElement().getElementType() != Element.Type.CLUSTER) {
                if (dataDao.containsAndNotEqual(node.getDataElement())) {
                    throw new DataGraphServiceException("Conflict! Another node has already been stored using the same ID!");
                }
                dataDao.add(node.getDataElement());
            }
            node.getDataElement().getShapes().add(node);
        }
        graphDao.add(node);
        styleElement(node);
        return node;
    }

    /**
     * Creates a cluster, grouping all given nodes and arcs.
     *
     * @param nodes
     * @param arcs
     * @return
     * @throws DataGraphServiceException
     */
    private GraphCluster clusterCreate(List<IGraphNode> nodes, List<IGraphArc> arcs) throws DataGraphServiceException {

        List<IGraphArc> arcsToCluster = new ArrayList();
        List<IGraphArc> arcsFromCluster = new ArrayList();

        DataCluster clusterData;
        DataClusterArc clusterDataArc;
        GraphCluster clusterShape;
        IGraphArc tmp;

        // Find connections to nodes outside of the cluster
        for (IGraphNode node : nodes) {

            for (IGravisConnection connection : node.getConnections()) {
                tmp = (IGraphArc) connection;

                if (!arcs.contains(tmp)) {
                    arcs.add(tmp);

                    if (!nodes.contains(tmp.getSource())) { // source is OUTSIDE
                        arcsToCluster.add(tmp);
                    } else if (!nodes.contains(tmp.getTarget())) { // target is OUTSIDE
                        arcsFromCluster.add(tmp);
                    }
                }
            }
        }

        // Create cluster object
        clusterData = new DataCluster(nodes, arcs);
        clusterDataArc = new DataClusterArc(clusterData);
        clusterShape = new GraphCluster(clusterData);
        add(clusterShape);

        Point2D pos = calculator.getCenter(nodes);
        clusterShape.translateXProperty().set(pos.getX());
        clusterShape.translateYProperty().set(pos.getY());

        for (IGraphArc arc : arcsFromCluster) {
            if (!clusterShape.getChildren().contains(arc.getTarget())) {
                arc = createConnection(clusterShape, arc.getTarget(), clusterDataArc);
                arc = add(arc);
            }
        }
        for (IGraphArc arc : arcsToCluster) {
            if (!clusterShape.getParents().contains(arc.getSource())) {
                arc = createConnection(arc.getSource(), clusterShape, clusterDataArc);
                arc = add(arc);
            }
        }

        for (IGraphArc arc : arcs) {
            graphDao.remove(arc);
        }
        for (IGraphNode node : nodes) {
            graphDao.remove(node);
        }

        return clusterShape;
    }

    /**
     * Removes the given cluster and un-groups all elements stored inside.
     *
     * @param cluster
     * @return
     */
    private IGraphCluster clusterRemove(IGraphCluster cluster) throws DataGraphServiceException {

        List<IGraphArc> clusteredArcs = cluster.getDataElement().getClusteredArcs();
        List<IGraphNode> clusteredNodes = cluster.getDataElement().getClusteredNodes();

        Point2D oldNodesPosition = calculator.getCenter(clusteredNodes);
        double translateX = cluster.getShape().getTranslateX() - oldNodesPosition.getX();
        double translateY = cluster.getShape().getTranslateY() - oldNodesPosition.getY();

        for (IGraphNode node : clusteredNodes) {
            if (dataDao.getNodeIds().contains(node.getDataElement().getId())) {
                node.translateXProperty().set(node.translateXProperty().get() + translateX);
                node.translateYProperty().set(node.translateYProperty().get() + translateY);
                graphDao.add(node);
            }
        }

        for (IGraphArc arc : clusteredArcs) {
            if (dataDao.getArcs().contains(arc.getDataElement())) {
                graphDao.add(arc);
            }
        }
        remove(cluster);

        return cluster;
    }

    /**
     * Clones the given node. Results in a node of the same type that references
     * the data object of the given node.
     *
     * @param target
     * @return
     * @throws DataGraphServiceException
     */
    private IGraphNode clone(IGraphNode target) throws DataGraphServiceException {
        IDataNode node = target.getDataElement();
        switch (node.getElementType()) {
            case PLACE:
                return new GraphPlace((DataPlace) node);
            case TRANSITION:
                return new GraphTransition((DataTransition) node);
            default:
                throw new DataGraphServiceException("Cannot clone the given type of element! [" + node.getElementType() + "]");
        }
    }

    /**
     * Creates a copy of the given node. Results in a node of the same type as
     * the given node.
     *
     * @param target
     * @return
     * @throws DataGraphServiceException
     */
    private IGraphNode copy(IGraphNode target) throws DataGraphServiceException {
        IDataNode node = target.getDataElement();
        switch (node.getElementType()) {
            case PLACE:
                return new GraphPlace(
                            new DataPlace(
                                ID_PREFIX_PLACE + dataDao.getNextPlaceId(), 
                                ((DataPlace) node).getPlaceType()));
            case TRANSITION:
                return new GraphTransition(
                            new DataTransition(
                                ID_PREFIX_TRANSITION + dataDao.getNextTransitionId(), 
                                ((DataTransition) node).getTransitionType()));
            default:
                throw new DataGraphServiceException("Cannot copy the given type of element! [" + node.getElementType() + "]");
        }
    }

    /**
     * Creates an arc connecting the given nodes.
     *
     * @param source
     * @param target
     * @param dataArc
     * @return
     * @throws DataGraphServiceException
     */
    private IGraphArc createConnection(IGraphNode source, IGraphNode target, DataArc dataArc) throws DataGraphServiceException {

        if (dataArc == null) {
            dataArc = new DataArc(source.getDataElement(), target.getDataElement(), defaultArcType);
        }

        IGraphArc shapeSourceToTarget;

        /**
         * Creating shape.
         */
        if (target == null) {

            // Arc not bound to any target
            shapeSourceToTarget = new GraphEdge(source, dataArc);

        } else if (!source.getParents().contains(target) && !target.getChildren().contains(source)) {

            // Create connection for source and target
            shapeSourceToTarget = new GraphEdge(source, target, dataArc);

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
            remove(shapeTargetToSource);
            add(getConvertedArcShape(shapeTargetToSource));

            // Create arc
            shapeSourceToTarget = new GraphCurve(source, target, dataArc);
        }

        // Adding shape.
        return shapeSourceToTarget;
    }

    /**
     * Gets the converted shape for a given arc. Converts straight to curved or
     * curved to straight, respectively.
     *
     * @param shape
     * @return
     * @throws DataGraphServiceException
     */
    private IGraphArc getConvertedArcShape(IGraphArc shape) throws DataGraphServiceException {
        if (shape instanceof GraphEdge) {
            return new GraphCurve(shape.getSource(), shape.getTarget(), shape.getDataElement());
        } else {
            return new GraphEdge(shape.getSource(), shape.getTarget(), shape.getDataElement());
        }
    }

    /**
     * Removes an element. Also removes all related parameters.
     *
     * @param node
     * @return
     */
    private IDataElement removeData(IDataElement element) throws DataGraphServiceException {
        if (element != null) {
            if (element.getShapes().isEmpty()) {
                for (Parameter param : element.getParameters().values()) {
                    dataDao.remove(param);
                }
                if (element instanceof DataArc) {
                    dataDao.remove((DataArc) element);
                } else {
                    dataDao.remove((IDataNode) element);
                }
                dataDao.remove(element);
            }
        }
        return element;
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
            arc.getDataElement().getShapes().remove(arc);
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
                    remove(reverseArc);
                    add(getConvertedArcShape(reverseArc));
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
     * @throws DataGraphServiceException
     */
    private IGraphNode removeShape(IGraphNode node) throws DataGraphServiceException {
        graphDao.remove(node);
        if (node.getDataElement() != null) {
            node.getDataElement().getShapes().remove(node);
        }
        return node;
    }

    /**
     * Sets the given style to all elements in the scene that are related to the
     * given data arc.
     *
     * @param element
     * @param styleParent
     */
    private void setElementStyle(IDataElement element, String styleParent, String styleChildren) {
        for (IGraphElement elem : element.getShapes()) {
            elem.getParentElementHandles().forEach(s -> {
                s.setActiveStyleClass(styleParent);
            });
            elem.getChildElementHandles().forEach(s -> {
                s.setActiveStyleClass(styleChildren);
            });
        }
    }

    /**
     * Styles the graph element in the scene according to the type assigned to
     * its data element.
     *
     * @param element
     * @throws DataGraphServiceException
     */
    private void styleElement(IGraphElement element) throws DataGraphServiceException {
        switch (element.getDataElement().getElementType()) {
            case ARC:
                styleArc((DataArc) element.getDataElement());
                break;
            case CLUSTER:
                setElementStyle(element.getDataElement(), stylceCluster, stylceCluster);
                break;
            case CLUSTERARC:
                setElementStyle(element.getDataElement(), styleClusterArc, styleClusterArc);
                break;
            case PLACE:
                stylePlace((DataPlace) element.getDataElement());
                break;
            case TRANSITION:
                styleTransition((DataTransition) element.getDataElement());
                break;
            default:
                throw new DataGraphServiceException("Cannot style element of undefined type!");
        }
    }

    private void styleArc(DataArc arc) throws DataGraphServiceException {
        if (arc.getArcType() != null) {
            switch (arc.getArcType()) {
                case INHIBITORY:
                    arc.getShapes().forEach(s -> {
                        IGraphArc a = (IGraphArc) s;
                        a.setArrowHeadVisible(false);
                        a.setCircleHeadVisible(true);
                    });
                    setElementStyle(arc, styleArcInhi, styleArcInhiHead);
                    break;
                case NORMAL:
                    arc.getShapes().forEach(s -> {
                        IGraphArc a = (IGraphArc) s;
                        a.setArrowHeadVisible(true);
                        a.setCircleHeadVisible(false);
                    });
                    setElementStyle(arc, styleArcDefault, styleArcDefaultHead);
                    break;
                case READ:
                    arc.getShapes().forEach(s -> {
                        IGraphArc a = (IGraphArc) s;
                        a.setArrowHeadVisible(false);
                        a.setCircleHeadVisible(true);
                    });
                    setElementStyle(arc, styleArcDefault, styleArcDefaultHead);
                    break;
                case TEST:
                    arc.getShapes().forEach(s -> {
                        IGraphArc a = (IGraphArc) s;
                        a.setArrowHeadVisible(true);
                        a.setCircleHeadVisible(false);
                    });
                    setElementStyle(arc, styleArcTest, styleArcTestHead);
                    break;
                default:
                    throw new DataGraphServiceException("Cannot create shape for an undefined arc type!");
            }
        }
    }

    private void stylePlace(DataPlace place) throws DataGraphServiceException {
        switch (place.getPlaceType()) {
            case CONTINUOUS:
                place.getShapes().forEach(s -> {
                    IGraphNode n = (IGraphNode) s;
                    n.setInnerCircleVisible(true);
                });
                setElementStyle(place, stylePlace, stylePlace);
                break;
            case DISCRETE:
                place.getShapes().forEach(s -> {
                    IGraphNode n = (IGraphNode) s;
                    n.setInnerCircleVisible(false);
                });
                setElementStyle(place, stylePlace, stylePlace);
                break;
            default:
                throw new DataGraphServiceException("Cannot create shape for an undefined place type!");
        }
    }

    private void styleTransition(DataTransition transition) throws DataGraphServiceException {
        switch (transition.getTransitionType()) {
            case CONTINUOUS:
                transition.getShapes().forEach(s -> {
                    IGraphNode n = (IGraphNode) s;
                    n.setInnerRectangleVisible(true);
                });
                setElementStyle(transition, styleTransitionDefault, styleTransitionDefault);
                break;
            case DISCRETE:
                transition.getShapes().forEach(s -> {
                    IGraphNode n = (IGraphNode) s;
                    n.setInnerRectangleVisible(false);
                });
                setElementStyle(transition, styleTransitionDefault, styleTransitionDefault);
                break;
            case STOCHASTIC:
                transition.getShapes().forEach(s -> {
                    IGraphNode n = (IGraphNode) s;
                    n.setInnerRectangleVisible(false);
                });
                setElementStyle(transition, styleTransitionStoch, styleTransitionStoch);
                break;
            default:
                throw new DataGraphServiceException("Cannot create shape for an undefined transition type!");
        }
    }

    /**
     * Validates the subtype of an arc.
     * @param arc
     * @param typeArc
     * @throws DataGraphServiceException
     */
    private void validateArcType(IArc arc, DataArc.Type typeArc) throws DataGraphServiceException {
        if (Element.Type.PLACE == arc.getTarget().getElementType()) {
            switch (typeArc) {
                case NORMAL:
                    break;
                case INHIBITORY:
                    throw new DataGraphServiceException("A transition cannot inhibit a place!");
                case TEST:
                    throw new DataGraphServiceException("A transition cannot test a place!");
                case READ:
                    throw new DataGraphServiceException("A transition cannot read a place!");
                default:
                    throw new DataGraphServiceException("Validation for arc type '" + typeArc + "' is undefined!");
            }
        }
    }

    /**
     * Validates a connection between two graph nodes.
     *
     * @param source
     * @param target
     * @throws DataGraphServiceException thrown in case the connection is not
     *                                   valid
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
        for (IGraphElement relatedSourceElement : dataSource.getShapes()) {

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
     * Validates the potential removal of a graph arc.
     *
     * @param arc
     * @throws DataGraphServiceException thrown in case the graph arc can not be
     *                                   deleted
     */
    private void validateRemoval(IGraphArc arc) throws DataGraphServiceException {
        IDataArc data = arc.getDataElement();
        if (data != null) {
            if (data.getShapes().size() <= 1) {
                try {
                    parameterService.ValidateRemoval(data.getParameters().values(), data);
                } catch (ParameterServiceException ex) {
                    throw new DataGraphServiceException(ex.getMessage());
                }
            }
        }
    }

    /**
     * Validates the potential removal of a graph node.
     *
     * @param node
     * @throws DataGraphServiceException
     */
    private void validateRemoval(IGraphNode node) throws DataGraphServiceException {
        IDataNode data = node.getDataElement();
        if (data != null) {
            try {
                for (IGravisConnection connection : node.getConnections()) {
                    validateRemoval((IGraphArc) connection);
                }
            } catch (DataGraphServiceException ex) {
                throw new DataGraphServiceException("A related arc cannot be removed! [" + ex.getMessage() + "]");
            }
            if (data.getShapes().size() <= 1) {
                try {
                    parameterService.ValidateRemoval(data.getParameters().values(), data);
                } catch (ParameterServiceException ex) {
                    throw new DataGraphServiceException("Node cannot be removed! [" + ex.getMessage() + "]");
                }
            }
        }
    }

    public DataDao getDataDao() {
        return dataDao;
    }

    public GraphDao getGraphDao() {
        return graphDao;
    }

//    public void setArcTypeDefault(DataArc.Type type) {
//        defaultArcType = type; // specifying a different arc type for default might cause problems, i.e.
//    }

    public void setPlaceTypeDefault(DataPlace.Type type) {
        defaultPlaceType = type;
    }

    public void setTransitionTypeDefault(DataTransition.Type type) {
        defaultTransitionType = type;
    }
}
