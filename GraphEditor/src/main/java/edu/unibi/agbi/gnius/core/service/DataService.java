/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.exception.ParameterServiceException;
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
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Model;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class DataService
{
    private static final Colour DEFAULT_COLOUR = new Colour("DEFAULT", "Default colour");
    private static final String PREFIX_ID_GRAPHNODE = "N";
    private static final String PREFIX_ID_PLACE = "P";
    private static final String PREFIX_ID_TRANSITION = "T";

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

    private final DataArc.Type defaultArcType = DataArc.Type.NORMAL;
    private DataPlace.Type defaultPlaceType = DataPlace.Type.CONTINUOUS;
    private DataTransition.Type defaultTransitionType = DataTransition.Type.CONTINUOUS;

    private final ObservableList<DataDao> dataDaos = FXCollections.observableArrayList();
    private DataDao dataDaoActive;

    /**
     * Adds a colour.
     *
     * @param colour
     * @throws DataServiceException
     */
    public synchronized void add(Colour colour) throws DataServiceException {
        dataDaoActive.setHasChanges(true);
        if (dataDaoActive.getModel().getColours().contains(colour)) {
            throw new DataServiceException("Conflict! Another colour has already been stored using the same ID!");
        }
        dataDaoActive.getModel().add(colour);
    }

    /**
     * Changes the subtype of an arc. Styles all related shapes in the scene
     * accordingly.
     *
     * @param arc
     * @param type
     * @throws DataServiceException
     */
    public synchronized void changeArcType(DataArc arc, DataArc.Type type) throws DataServiceException {
        validateArcType(arc, type);
        arc.setArcType(type);
        dataDaoActive.setHasChanges(true);
        styleArc(arc);
    }

    /**
     * Changes the subtype of a place. Styles all related shapes in the scene
     * accordingly.
     *
     * @param place
     * @param type
     * @throws DataServiceException
     */
    public synchronized void changePlaceType(DataPlace place, DataPlace.Type type) throws DataServiceException {
        DataPlace.Type typeOld = place.getPlaceType();
        place.setPlaceType(type);
        try {
            for (IArc arc : place.getArcsIn()) {
                validateArcType(arc, arc.getArcType());
            }
            for (IArc arc : place.getArcsOut()) {
                validateArcType(arc, arc.getArcType());
            }
        } catch (DataServiceException ex) {
            place.setPlaceType(typeOld);
            messengerService.addToLog("Cannot change place type to '" + type + "'! [" + ex.getMessage() + "]");
            throw new DataServiceException("Cannot change place type to '" + type + "'!");
        }
        dataDaoActive.setHasChanges(true);
        stylePlace(place);
    }

    /**
     * Changes the subtype of a transition. Styles all related shapes in the
     * scene accordingly.
     *
     * @param transition
     * @param type
     * @throws DataServiceException
     */
    public synchronized void changeTransitionType(DataTransition transition, DataTransition.Type type) throws DataServiceException {
        DataTransition.Type typeOld = transition.getTransitionType();
        transition.setTransitionType(type);
        try {
            for (IArc arc : transition.getArcsIn()) {
                validateArcType(arc, defaultArcType);
            }
            for (IArc arc : transition.getArcsOut()) {
                validateArcType(arc, defaultArcType);
            }
        } catch (DataServiceException ex) {
            transition.setTransitionType(typeOld);
            messengerService.addToLog("Cannot change place type to '" + type + "'! [" + ex.getMessage() + "]");
            throw new DataServiceException("Cannot change place type to '" + type + "'!");
        }
        dataDaoActive.setHasChanges(true);
        styleTransition(transition);
    }

    /**
     * Connects the given graph nodes. Validates the connection, then creates
     * and adds a new graph arc to the scene.
     *
     * @param source
     * @param target
     * @return
     * @throws DataServiceException
     */
    public synchronized IGraphArc connect(IGraphNode source, IGraphNode target) throws DataServiceException {
        IGraphArc arc;
        validateConnection(source, target);
        arc = createConnection(source, target, null);
        validateArcType(arc.getDataElement(), defaultArcType);
        arc = add(arc);
        dataDaoActive.setHasChanges(true);
        return arc;
    }

    /**
     * Creates a node of the specified type at the given event position.
     *
     * @param type
     * @param posX
     * @param posY
     * @return
     * @throws DataServiceException
     */
    public synchronized IGraphNode create(Element.Type type, double posX, double posY) throws DataServiceException {
        IGraphNode shape;
        switch (type) {
            case PLACE:
                DataPlace place;
                place = new DataPlace(createPlaceId(), defaultPlaceType);
                shape = new GraphPlace(createGraphNodeId(), place);
                break;
            case TRANSITION:
                DataTransition transition;
                transition = new DataTransition(createTransitionId(), defaultTransitionType);
                shape = new GraphTransition(createGraphNodeId(), transition);
                break;

            default:
                throw new DataServiceException("Cannot create element of undefined type!");
        }
        Point2D pos = calculator.getCorrectedMousePosition(dataDaoActive.getGraph(), posX, posY);
        shape.translateXProperty().set(pos.getX() + shape.getOffsetX());
        shape.translateYProperty().set(pos.getY() + shape.getOffsetY());
        shape = add(shape);
        dataDaoActive.setHasChanges(true);
        return shape;
    }

    /**
     * Creates a new data access object.
     * 
     * @return 
     */
    public DataDao createDao() {
        DataDao dao = new DataDao();
        dao.getModel().setAuthor(System.getProperty("user.name"));
        dao.getModel().setCreationDateTime(LocalDateTime.now());
        dao.getModel().setDescription("New model.");
        dao.getModel().setId(String.valueOf(System.nanoTime()));
        dao.getModel().setName("Untitled");
        dao.getModel().add(DEFAULT_COLOUR);
        dao.setHasChanges(false);
        return dao;
    }

    /**
     * Creates an arc that binds its source to the given node.
     *
     * @param source
     * @return
     */
    public synchronized GraphEdge createTemporaryArc(IGraphNode source) {
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
        dataDaoActive.getGraph().add(edge);
        return edge;
    }

    /**
     * Groups and hides elements in a cluster element. Removes any existing
     * groups within the selection before creating the group.
     *
     * @param selected
     * @return
     * @throws DataServiceException
     */
    public synchronized GraphCluster group(List<IGraphElement> selected) throws DataServiceException {

        if (selected.isEmpty()) {
            throw new DataServiceException("Nothing was selected for clustering!");
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
            throw new DataServiceException("Not enough elements selected for clustering!");
        }

        // Combine nodes and arcs from existing clusters.
        for (IGraphCluster cluster : clusters) {
            arcs.addAll(cluster.getDataElement().getClusteredArcs());
            nodes.addAll(cluster.getDataElement().getClusteredNodes());
            clusterRemove(cluster);
        }

        GraphCluster cluster = clusterCreate(nodes, arcs);
        dataDaoActive.setHasChanges(true);
        return cluster;
    }

    /**
     * Removes the given graph arc from the scene.
     *
     * @param arc
     * @return
     * @throws DataServiceException
     */
    public synchronized IGraphArc remove(IGraphArc arc) throws DataServiceException {
        validateRemoval(arc);
        removeShape(arc);
        removeData(arc.getDataElement());
        dataDaoActive.setHasChanges(true);
        return arc;
    }

    /**
     * Removes the given graph node from the scene. Also removes all arcs
     * connected to the given node.
     *
     * @param node
     * @return
     * @throws DataServiceException
     */
    public synchronized IGraphNode remove(IGraphNode node) throws DataServiceException {
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
        dataDaoActive.setHasChanges(true);
        return node;
    }

    /**
     * Removes the given graph elements from the scene. Also removes all
     * connections to the given nodes.
     *
     * @param elements
     * @throws DataServiceException
     */
    public synchronized void remove(List<IGraphElement> elements) throws DataServiceException {
        for (IGraphElement element : elements) {
            if (element instanceof IGraphArc) {
                remove((IGraphArc) element);
            } else {
                remove((IGraphNode) element);
            }
        }
        dataDaoActive.setHasChanges(true);
    }

    /**
     * Pastes given node(s). Either copies or clones nodes, inserting them at
     * the latest mouse pointer location.
     *
     * @param nodes
     * @param cutting
     * @return
     * @throws DataServiceException
     */
    public synchronized List<IGraphNode> paste(List<IGraphNode> nodes, boolean cutting) throws DataServiceException {

        Point2D center = calculator.getCenter(nodes);
        Point2D position = calculator.getCorrectedMousePositionLatest(dataDaoActive.getGraph());

        List<IGraphNode> shapes = new ArrayList();
        IGraphNode shape;

        for (int i = 0; i < nodes.size(); i++) {

            if (cutting) {
//                shape = clone(nodes.get(i));
                shape = nodes.get(i);
            } else {
                shape = copy(nodes.get(i));
                add(shape);
            }

            shape.translateXProperty().set(nodes.get(i).translateXProperty().get() - center.getX() + position.getX());
            shape.translateYProperty().set(nodes.get(i).translateYProperty().get() - center.getY() + position.getY());

            shapes.add(shape);
        }
        dataDaoActive.setHasChanges(true);
        return shapes;
    }

    /**
     * Styles the graph element in the scene according to the type assigned to
     * its data element.
     *
     * @param element
     * @throws DataServiceException
     */
    public void styleElement(IGraphElement element) throws DataServiceException {
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
                throw new DataServiceException("Cannot style element of undefined type!");
        }
    }

    /**
     * Restores and shows all the nodes stored within the given cluster(s).
     *
     * @param selected
     * @throws DataServiceException
     */
    public synchronized void ungroup(List<IGraphElement> selected) throws DataServiceException {

        if (selected.isEmpty()) {
            throw new DataServiceException("Nothing was selected for unclustering!");
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
    private synchronized IGraphArc add(IGraphArc arc) throws DataServiceException {
        if (arc.getDataElement() != null) {
            if (arc.getDataElement().getElementType() == Element.Type.ARC) {
                if (dataDaoActive.getModel().containsAndNotEqual(arc.getDataElement())) {
                    throw new DataServiceException("Conflict! Another arc has already been stored using the same ID!");
                }
                dataDaoActive.getModel().add(arc.getDataElement());
            }
            arc.getDataElement().getShapes().add(arc);
        }
        dataDaoActive.getGraph().add(arc);
        styleElement(arc);
        return arc;
    }

    /**
     * Adds node to scene and data model.
     *
     * @param node
     * @return
     */
    private IGraphNode add(IGraphNode node) throws DataServiceException {
        if (node.getDataElement() != null) {
            if (node.getDataElement().getElementType() != Element.Type.CLUSTER) {
                if (dataDaoActive.getModel().containsAndNotEqual(node.getDataElement())) {
                    throw new DataServiceException("Conflict! Another node has already been stored using the same ID!");
                }
                dataDaoActive.getModel().add(node.getDataElement());
            }
            node.getDataElement().getShapes().add(node);
        }
        dataDaoActive.getGraph().add(node);
        styleElement(node);
        return node;
    }

    /**
     * Creates a cluster, grouping all given nodes and arcs.
     *
     * @param nodes
     * @param arcs
     * @return
     * @throws DataServiceException
     */
    private GraphCluster clusterCreate(List<IGraphNode> nodes, List<IGraphArc> arcs) throws DataServiceException {

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
        clusterShape = new GraphCluster(createGraphNodeId(), clusterData);
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
            dataDaoActive.getGraph().remove(arc);
        }
        for (IGraphNode node : nodes) {
            dataDaoActive.getGraph().remove(node);
        }
        return clusterShape;
    }

    /**
     * Removes the given cluster and un-groups all elements stored inside.
     *
     * @param cluster
     * @return
     */
    private IGraphCluster clusterRemove(IGraphCluster cluster) throws DataServiceException {

        List<IGraphArc> clusteredArcs = cluster.getDataElement().getClusteredArcs();
        List<IGraphNode> clusteredNodes = cluster.getDataElement().getClusteredNodes();

        Point2D oldNodesPosition = calculator.getCenter(clusteredNodes);
        double translateX = cluster.getShape().getTranslateX() - oldNodesPosition.getX();
        double translateY = cluster.getShape().getTranslateY() - oldNodesPosition.getY();

        for (IGraphNode node : clusteredNodes) {
            if (dataDaoActive.getModel().getNodeIds().contains(node.getDataElement().getId())) {
                node.translateXProperty().set(node.translateXProperty().get() + translateX);
                node.translateYProperty().set(node.translateYProperty().get() + translateY);
                dataDaoActive.getGraph().add(node);
            }
        }

        for (IGraphArc arc : clusteredArcs) {
            if (dataDaoActive.getModel().getArcs().contains(arc.getDataElement())) {
                dataDaoActive.getGraph().add(arc);
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
     * @throws DataServiceException
     */
    private IGraphNode clone(IGraphNode target) throws DataServiceException {
        IDataNode node = target.getDataElement();
        switch (node.getElementType()) {
            case PLACE:
                return new GraphPlace(createGraphNodeId(), (DataPlace) node);
            case TRANSITION:
                return new GraphTransition(createGraphNodeId(), (DataTransition) node);
            default:
                throw new DataServiceException("Cannot clone the given type of element! [" + node.getElementType() + "]");
        }
    }

    /**
     * Creates a copy of the given node. Results in a node of the same type as
     * the given node.
     *
     * @param target
     * @return
     * @throws DataServiceException
     */
    private IGraphNode copy(IGraphNode target) throws DataServiceException {
        IDataNode node = target.getDataElement();
        switch (node.getElementType()) {
            case PLACE:
                DataPlace dataPlace;
                dataPlace = new DataPlace(createPlaceId(), ((DataPlace) node).getPlaceType());
                dataPlace.addToken(new Token(DEFAULT_COLOUR));
                return new GraphPlace(createGraphNodeId(), dataPlace);
            case TRANSITION:
                DataTransition dataTransition;
                dataTransition = new DataTransition(createTransitionId(), ((DataTransition) node).getTransitionType());
                return new GraphTransition(createGraphNodeId(), dataTransition);
            default:
                throw new DataServiceException("Cannot copy the given type of element! [" + node.getElementType() + "]");
        }
    }

    /**
     * Creates an arc connecting the given nodes.
     *
     * @param source
     * @param target
     * @param dataArc
     * @return
     * @throws DataServiceException
     */
    private IGraphArc createConnection(IGraphNode source, IGraphNode target, DataArc dataArc) throws DataServiceException {

        if (dataArc == null) {
            dataArc = new DataArc(source.getDataElement(), target.getDataElement(), defaultArcType);
            dataArc.addWeight(new Weight(DEFAULT_COLOUR));
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
            // Convert existing shape
            convertArcShape(shapeTargetToSource);

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
     * @throws DataServiceException
     */
    private IGraphArc convertArcShape(IGraphArc shape) throws DataServiceException {

        // Temporarily remove parameters to allow conversion without failing validation
        Set<String> paramsTmp = new TreeSet();
        for (String key : shape.getDataElement().getRelatedParameterIds()) {
            paramsTmp.add(key);
        }
        shape.getDataElement().getRelatedParameterIds().clear();

        // Convert
        remove(shape);
        if (shape instanceof GraphEdge) {
            shape = new GraphCurve(shape.getSource(), shape.getTarget(), shape.getDataElement());
        } else {
            shape = new GraphEdge(shape.getSource(), shape.getTarget(), shape.getDataElement());
        }
        add(shape);
        styleArc(shape.getDataElement());

        // Restore parameters again
        shape.getDataElement().getRelatedParameterIds().addAll(paramsTmp);

        return shape;
    }

    /**
     * Removes an element. Also removes all related parameters.
     *
     * @param node
     * @return
     */
    private IDataElement removeData(IDataElement element) throws DataServiceException {
        if (element != null) {
            if (element.getShapes().isEmpty()) {
                dataDaoActive.getModel().remove(element);
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
     * @throws DataServiceException
     */
    private IGraphArc removeShape(IGraphArc arc) throws DataServiceException {

        dataDaoActive.getGraph().remove(arc);
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
        if (target.getChildren().contains(arc.getSource())) {
            for (int i = 0; i < target.getConnections().size(); i++) {
                if (target.getConnections().get(i).getTarget().equals(source)) {
                    convertArcShape((IGraphArc) target.getConnections().get(i));
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
     * @throws DataServiceException
     */
    private IGraphNode removeShape(IGraphNode node) throws DataServiceException {
        dataDaoActive.getGraph().remove(node);
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

    private void styleArc(DataArc arc) throws DataServiceException {
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
                    throw new DataServiceException("Cannot create shape for an undefined arc type!");
            }
        }
    }

    private void stylePlace(DataPlace place) throws DataServiceException {
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
                throw new DataServiceException("Cannot create shape for an undefined place type!");
        }
    }

    private void styleTransition(DataTransition transition) throws DataServiceException {
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
                throw new DataServiceException("Cannot create shape for an undefined transition type!");
        }
    }

    /**
     * Validates the subtype of an arc.
     *
     * @param arc
     * @param typeArc
     * @throws DataServiceException
     */
    private void validateArcType(IArc arc, DataArc.Type typeArc) throws DataServiceException {
        if (Element.Type.PLACE == arc.getTarget().getElementType()) {
            switch (typeArc) {
                case NORMAL:
                    break;
                case INHIBITORY:
                    throw new DataServiceException("A transition cannot inhibit a place!");
                case TEST:
                    throw new DataServiceException("A transition cannot test a place!");
                case READ:
                    throw new DataServiceException("A transition cannot read a place!");
                default:
                    throw new DataServiceException("Validation for arc type '" + typeArc + "' is undefined!");
            }
        }
    }

    /**
     * Validates a connection between two graph nodes.
     *
     * @param source
     * @param target
     * @throws DataServiceException thrown in case the connection is not valid
     */
    private void validateConnection(IGraphNode source, IGraphNode target) throws DataServiceException {

        IDataNode dataSource = source.getDataElement();
        IDataNode dataTarget = target.getDataElement();

        IGraphNode relatedSourceShape;
        IGraphNode relatedSourceShapeChild;

        /**
         * Ensuring the connection to be valid.
         */
        if (source.getClass().equals(target.getClass())) {
            throw new DataServiceException("Cannot connect nodes of the same type!");
        }
        if (source instanceof GraphCluster || target instanceof GraphCluster) {
            throw new DataServiceException("Cannot connect to a cluster without specifying the exact nodes!");
        }
        if (source.getChildren().contains(target) || target.getParents().contains(source)) {
            throw new DataServiceException("Nodes are already connected!");
        }
        for (IGraphElement relatedSourceElement : dataSource.getShapes()) {

            relatedSourceShape = (IGraphNode) relatedSourceElement;
            for (int i = 0; i < relatedSourceShape.getChildren().size(); i++) {

                relatedSourceShapeChild = (IGraphNode) relatedSourceShape.getChildren().get(i);
                if (dataTarget == relatedSourceShapeChild.getDataElement()) {
                    throw new DataServiceException("Another element already connects those nodes!");
                }
            }
        }
    }

    /**
     * Validates the potential removal of a graph arc.
     *
     * @param arc
     * @throws DataServiceException thrown in case the graph arc can not be
     *                              deleted
     */
    private void validateRemoval(IGraphArc arc) throws DataServiceException {
        IDataArc data = arc.getDataElement();
        if (data != null) {
            if (data.getShapes().size() <= 1) {
                try {
                    parameterService.ValidateRemoval(data);
                } catch (ParameterServiceException ex) {
                    throw new DataServiceException(ex.getMessage());
                }
            }
        }
    }

    /**
     * Validates the potential removal of a graph node.
     *
     * @param node
     * @throws DataServiceException
     */
    private void validateRemoval(IGraphNode node) throws DataServiceException {
        IDataNode data = node.getDataElement();
        if (data != null) {
            try {
                for (IGravisConnection connection : node.getConnections()) {
                    validateRemoval((IGraphArc) connection);
                }
            } catch (DataServiceException ex) {
                throw new DataServiceException("A related arc cannot be removed! [" + ex.getMessage() + "]");
            }
            if (data.getShapes().size() <= 1) {
                try {
                    parameterService.ValidateRemoval(data);
                } catch (ParameterServiceException ex) {
                    throw new DataServiceException("Node cannot be removed! [" + ex.getMessage() + "]");
                }
            }
        }
    }

    private String createGraphNodeId() {
        String id;
        do {
            id = PREFIX_ID_GRAPHNODE + dataDaoActive.getNextNodeId();
        } while (dataDaoActive.getGraph().contains(id));
        return id;
    }

    private String createPlaceId() {
        String id;
        do {
            id = PREFIX_ID_PLACE + dataDaoActive.getNextPlaceId();
        } while (dataDaoActive.getModel().contains(id));
        return id;
    }

    private String createTransitionId() {
        String id;
        do {
            id = PREFIX_ID_TRANSITION + dataDaoActive.getNextTransitionId();
        } while (dataDaoActive.getModel().contains(id));
        return id;
    }

    public synchronized void setActiveDataDao(DataDao dataDao) {
        if (!dataDaos.contains(dataDao)) {
            dataDaos.add(dataDao);
        }
        dataDaoActive = dataDao;
    }

    public synchronized DataDao getActiveDao() {
        return dataDaoActive;
    }

    public synchronized Graph getActiveGraph() {
        return dataDaoActive.getGraph();
    }

    public synchronized Model getActiveModel() {
        return dataDaoActive.getModel();
    }

    public synchronized List<DataDao> getDataDaosWithChanges() {
        List<DataDao> daosWithChanges = new ArrayList();
        for (DataDao dataDao : dataDaos) {
            if (dataDao.hasChanges()) {
                daosWithChanges.add(dataDao);
            }
        }
        return daosWithChanges;
    }
    
    public ObservableList getDataDaosList() {
        return dataDaos;
    }

    public synchronized void removeDataDao(DataDao dataDao) {
        dataDaos.remove(dataDao);
    }

    public synchronized void setArcWeight(DataArc arc, Weight weight) {
        arc.addWeight(weight);
        dataDaoActive.setHasChanges(true);
    }

    public void setPlaceTypeDefault(DataPlace.Type type) {
        defaultPlaceType = type;
    }

    public synchronized void setPlaceToken(DataPlace place, Token token) {
        place.addToken(token);
        dataDaoActive.setHasChanges(true);
    }

    public void setTransitionTypeDefault(DataTransition.Type type) {
        defaultTransitionType = type;
    }

    public synchronized void setTransitionFunction(DataTransition transition, String functionString) throws DataServiceException {
        try {
            parameterService.setTransitionFunction(transition, functionString);
            dataDaoActive.setHasChanges(true);
        } catch (Exception ex) {
            throw new DataServiceException("Cannot build function from input '" + functionString + "'! [" + ex.getMessage() + "]");
        }
    }
}
