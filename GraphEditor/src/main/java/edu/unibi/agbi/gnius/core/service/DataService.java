/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.service.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.service.exception.ParameterServiceException;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.data.DataType;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gravisfx.entity.IGravisCluster;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Model;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    private static final Colour DEFAULT_COLOUR = new Colour("WHITE", "Default colour");
    private static final String PREFIX_ID_CLUSTER = "C";
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
    private DataDao dataDao;

    private final BooleanProperty isGridEnabled = new SimpleBooleanProperty(true);

    /**
     * Adds a colour.
     *
     * @param colour
     * @throws DataServiceException
     */
    public synchronized void add(Colour colour) throws DataServiceException {
        dataDao.setHasChanges(true);
        if (dataDao.getModel().getColours().contains(colour)) {
            throw new DataServiceException("Conflict! Another colour has already been stored using the same ID!");
        }
        dataDao.getModel().add(colour);
    }

    /**
     * Adds arc to scene and data model.
     *
     * @param dao
     * @param arc
     * @return
     * @throws DataServiceException
     */
    public synchronized IGraphArc add(DataDao dao, IGraphArc arc) throws DataServiceException {
        if (arc.getData() != null) {
            if (arc.getData().getType() != DataType.CLUSTERARC) {
                if (dao.getModel().containsAndNotEqual(arc.getData())) {
                    throw new DataServiceException("Conflict! Another arc has already been stored using the same ID!");
                } else {
                    dao.getModel().add(arc.getData());
                }
            }
        }
        dao.getGraph().add(arc);
        styleElement(arc);
        return arc;
    }

    /**
     * Adds node to scene and data model.
     *
     * @param dao
     * @param node
     * @return
     * @throws DataServiceException
     */
    public synchronized IGraphNode add(DataDao dao, IGraphNode node) throws DataServiceException {
        if (node.getData() != null) {
            if (node.getData().getType() != DataType.CLUSTER) {
                if (dao.getModel().containsAndNotEqual(node.getData())) {
                    throw new DataServiceException("Conflict! Another node has already been stored using the same ID!");
                } else {
                    dao.getModel().add(node.getData());
                }
            }
        }
        dao.getGraph().add(node);
        styleElement(node);
        return node;
    }

    public synchronized void ChangeElementSubtype(IDataElement element, Object subtype) throws DataServiceException {

        switch (element.getType()) {

            case ARC:
                DataArc arc = (DataArc) element;
                DataArc.Type arcType = (DataArc.Type) subtype;
                if (arc.getArcType() != arcType) {
                    changeArcType(arc, arcType);
                }
                break;

            case PLACE:
                DataPlace place = (DataPlace) element;
                DataPlace.Type placeType = (DataPlace.Type) subtype;
                if (place.getPlaceType() != placeType) {
                    setPlaceTypeDefault(placeType);
                    changePlaceType(place, placeType);
                }
                break;

            case TRANSITION:
                DataTransition transition = (DataTransition) element;
                DataTransition.Type transitionType = (DataTransition.Type) subtype;
                if (transition.getTransitionType() != transitionType) {
                    setTransitionTypeDefault(transitionType);
                    changeTransitionType(transition, transitionType);
                }
                break;
        }
    }

    /**
     * Connects the given graph nodes.Validates the connection, then creates
 and adds a new graph arc to the scene.
     *
     * @param dao
     * @param source
     * @param target
     * @return
     * @throws DataServiceException
     */
    public synchronized IGraphArc connect(DataDao dao, IGraphNode source, IGraphNode target) throws DataServiceException {
        IGraphArc arc;
        validateConnection(source, target);
        arc = CreateConnection(source, target, null);
        validateArc(arc.getData());
        add(dao, arc);
        dao.setHasChanges(true);
        return arc;
    }

    /**
     * Creates a cloned node.Results in a node of the same type that references
     * the given data.
     *
     * @param dao
     * @param data
     * @param posX
     * @param posY
     * @return
     * @throws DataServiceException
     */
    public IGraphNode CreateClone(DataDao dao, IDataNode data, double posX, double posY) throws DataServiceException {
        IGraphNode clone;
        switch (data.getType()) {
            case PLACE:
                clone = new GraphPlace(getGraphNodeId(dataDao), (DataPlace) data);
                break;
            case TRANSITION:
                clone = new GraphTransition(getGraphNodeId(dataDao), (DataTransition) data);
                break;
            default:
                throw new DataServiceException("Cannot clone the given type of element! [" + data.getType() + "]");
        }
        Point2D pos = calculator.getCorrectedPosition(dataDao.getGraph(), posX, posY);
        clone.translateXProperty().set(pos.getX() - clone.getCenterOffsetX());
        clone.translateYProperty().set(pos.getY() - clone.getCenterOffsetY());
        clone = add(dao, clone);
        dataDao.setHasChanges(true);
        return clone;
    }

    /**
     * Creates a node of the specified type at the given event position.
     *
     * @param dao
     * @param type
     * @param posX
     * @param posY
     * @return
     * @throws DataServiceException
     */
    public synchronized IGraphNode CreateNode(DataDao dao, DataType type, double posX, double posY) throws DataServiceException {
        IGraphNode shape;
        switch (type) {
            case PLACE:
                DataPlace place;
                place = new DataPlace(getPlaceId(dataDao), defaultPlaceType);
                place.addToken(new Token(DEFAULT_COLOUR));
                shape = new GraphPlace(getGraphNodeId(dataDao), place);
                break;

            case TRANSITION:
                DataTransition transition;
                transition = new DataTransition(getTransitionId(dataDao), defaultTransitionType);
                shape = new GraphTransition(getGraphNodeId(dataDao), transition);
                break;

            default:
                throw new DataServiceException("Cannot create element of undefined type!");
        }
        Point2D pos = calculator.getCorrectedPosition(dataDao.getGraph(), posX, posY);
        if (isGridEnabled()) {
            pos = calculator.getPositionInGrid(pos, getGraph());
        }
        shape.translateXProperty().set(pos.getX() - shape.getCenterOffsetX());
        shape.translateYProperty().set(pos.getY() - shape.getCenterOffsetY());
        shape = add(dao, shape);
        dataDao.setHasChanges(true);
        return shape;
    }

    /**
     * Creates an arc connecting the given nodes.
     *
     * @param source
     * @param target
     * @param dataArc
     * @return
     */
    public IGraphArc CreateConnection(IGraphNode source, IGraphNode target, DataArc dataArc) {

        String id;

        /**
         * Create data.
         */
        id = getArcId(source.getData(), target.getData());
        if (dataArc == null) {
            dataArc = new DataArc(id, source.getData(), target.getData(), defaultArcType);
            dataArc.addWeight(new Weight(DEFAULT_COLOUR));
        }

        /**
         * Creating shape.
         */
        id = getConnectionId(source, target);
        return new GraphArc(id, source, target, dataArc);
    }

    /**
     * Creates an arc that binds its source to the given node.
     *
     * @param source
     * @return
     */
    public synchronized IGraphArc CreateConnectionTmp(IGraphNode source) {
        GraphArc edge;
        edge = new GraphArc(source.getId() + "null", source);
        edge.getParentElementHandles().forEach(ele -> {
            ele.setActiveStyleClass(styleArcDefault);
        });
        edge.getChildElementHandles().forEach(ele -> {
            ele.setActiveStyleClass(styleArcDefaultHead);
        });
        edge.setArrowHeadVisible(true);
        edge.setCircleHeadVisible(false);
        dataDao.getGraph().add(edge);
        return edge;
    }

    /**
     * Creates a new data access object.
     *
     * @return
     */
    public DataDao CreateDao() {
        DataDao dao = new DataDao();
        dao.setAuthor(System.getProperty("user.name"));
        dao.setCreationDateTime(LocalDateTime.now());
        dao.setModelId(String.valueOf(System.nanoTime()));
        dao.setModelName("Untitled");
        dao.getModel().add(DEFAULT_COLOUR);
        dao.setHasChanges(false);
        return dao;
    }

    /**
     * Removes a data access object.
     *
     * @param dataDao
     */
    public synchronized void remove(DataDao dataDao) {
        dataDaos.remove(dataDao);
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
        try {
            removeData(arc.getData());
        } catch (Exception ex) {
            throw new DataServiceException(ex.getMessage());
        }
        dataDao.setHasChanges(true);
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
        validateRemoval(node);
        try {
            IGraphArc arc;
            while (!node.getConnections().isEmpty()) {
                arc = (IGraphArc) node.getConnections().iterator().next();
                removeShape(arc);
                removeData(arc.getData());
            }
            removeShape(node);
            removeData(node.getData());
            dataDao.setHasChanges(true);
        } catch (Exception ex) {
            throw new DataServiceException(ex.getMessage());
        }
        return node;
    }

    /**
     * Removes the given elements from the scene.
     *
     * @param elements
     */
    public synchronized void remove(List<IGraphElement> elements) {
        for (IGraphElement element : elements) {
            try {
                if (element instanceof IGraphArc) {
                    remove((IGraphArc) element);
                } else {
                    remove((IGraphNode) element);
                }
            } catch (DataServiceException ex) {
                messengerService.printMessage("Cannot remove element(s)!");
                messengerService.addException("Cannot remove element '" + element.getData().getId() + "'!", ex);
            }
        }
        dataDao.setHasChanges(true);
    }

    /**
     * Pastes given node(s).Either copies or clones nodes, inserting them at the
     * latest mouse pointer location.
     *
     * @param dao
     * @param nodes
     * @param cut
     * @return
     * @throws DataServiceException
     */
    public synchronized List<IGraphNode> paste(DataDao dao, List<IGraphNode> nodes, boolean cut) throws DataServiceException {

        List<IGraphNode> shapes = new ArrayList();
        IGraphNode shape;

        Point2D posCenter = calculator.getCenterN(nodes);
        Point2D posMouse = calculator.getCorrectedMousePositionLatest(dataDao.getGraph());

        for (int i = 0; i < nodes.size(); i++) {

            if (cut) {
                shape = nodes.get(i);
            } else {
                shape = copy(nodes.get(i));
                if (shape == null) {
                    continue;
                }
                add(dao, shape);
            }

            shape.translateXProperty().set(nodes.get(i).translateXProperty().get() - posCenter.getX() + posMouse.getX() - shape.getCenterOffsetX());
            shape.translateYProperty().set(nodes.get(i).translateYProperty().get() - posCenter.getY() + posMouse.getY() - shape.getCenterOffsetY());

            shapes.add(shape);
        }

        if (isGridEnabled()) {

            Point2D pos;
            for (IGraphNode node : shapes) {

                pos = new Point2D(node.translateXProperty().get(), node.translateYProperty().get());
                pos = calculator.getPositionInGrid(pos, getGraph());

                node.translateXProperty().set(pos.getX());
                node.translateYProperty().set(pos.getY());
            }
        }

        dataDao.setHasChanges(true);
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
        switch (element.getData().getType()) {
            case ARC:
                styleArc((DataArc) element.getData());
                break;
            case CLUSTER:
                setElementStyle(element.getData(), stylceCluster, null);
                break;
            case CLUSTERARC:
                setElementStyle(element.getData(), styleClusterArc, styleClusterArc);
                ((IGraphArc) element).setCircleHeadVisible(false);
                break;
            case PLACE:
                stylePlace((DataPlace) element.getData());
                break;
            case TRANSITION:
                styleTransition((DataTransition) element.getData());
                break;
            default:
                throw new DataServiceException("Cannot style element of undefined type!");
        }
    }

    /**
     * Updates cluster shapes visual disabled states.
     */
    public void UpdateClusterShapes() {
        for (IGravisCluster cluster : getGraph().getClusters()) {
            boolean isDisabled = ((GraphCluster) cluster).getData().isDisabled();
            for (GravisShapeHandle handle : cluster.getElementHandles()) {
                handle.setDisabled(isDisabled);
            }
        }
    }

    /**
     * Changes the subtype of an arc. Styles all related shapes in the scene
     * accordingly.
     *
     * @param arc
     * @param type
     * @throws DataServiceException
     */
    private void changeArcType(DataArc arc, DataArc.Type type) throws DataServiceException {
        DataArc.Type typeOld = arc.getArcType();
        arc.setArcType(type);
        try {
            validateArc(arc);
        } catch (DataServiceException ex) {
            arc.setArcType(typeOld);
            throw ex;
        }
        dataDao.setHasChanges(true);
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
    private void changePlaceType(DataPlace place, DataPlace.Type type) throws DataServiceException {
        DataPlace.Type typeOld = place.getPlaceType();
        place.setPlaceType(type);
        try {
            for (IArc arc : place.getArcsIn()) {
                validateArc((IDataArc) arc);
            }
            for (IArc arc : place.getArcsOut()) {
                validateArc((IDataArc) arc);
            }
        } catch (DataServiceException ex) {
            place.setPlaceType(typeOld);
            throw ex;
        }
        dataDao.setHasChanges(true);
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
    private void changeTransitionType(DataTransition transition, DataTransition.Type type) throws DataServiceException {
        DataTransition.Type typeOld = transition.getTransitionType();
        transition.setTransitionType(type);
        try {
            for (IArc arc : transition.getArcsIn()) {
                validateArc((IDataArc) arc);
            }
            for (IArc arc : transition.getArcsOut()) {
                validateArc((IDataArc) arc);
            }
        } catch (DataServiceException ex) {
            transition.setTransitionType(typeOld);
            throw ex;
        }
        dataDao.setHasChanges(true);
        styleTransition(transition);
    }

    /**
     * Creates a copy of the given node. Results in a node of the same type as
     * the given node.
     *
     * @param target
     * @return
     */
    private IGraphNode copy(IGraphNode target) {
        IDataNode node = target.getData();
        switch (node.getType()) {
            case PLACE:
                DataPlace place;
                place = new DataPlace(getPlaceId(dataDao), ((DataPlace) node).getPlaceType());
                place.addToken(new Token(DEFAULT_COLOUR));
                return new GraphPlace(getGraphNodeId(dataDao), place);
            case TRANSITION:
                DataTransition transition;
                transition = new DataTransition(getTransitionId(dataDao), ((DataTransition) node).getTransitionType());
                return new GraphTransition(getGraphNodeId(dataDao), transition);
            default:
                return null;
        }
    }

    /**
     * Removes an element. Also removes all related parameters.
     *
     * @param node
     * @return
     */
    private IDataElement removeData(IDataElement element) throws Exception {
        if (element != null) {
            if (element.getShapes().isEmpty()) {
                dataDao.getModel().remove(element);
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

        dataDao.getGraph().remove(arc);
        if (arc.getData() != null) {
            arc.getData().getShapes().remove(arc);
        }

        // Check null for temporary arcs
        if (arc.getTarget() == null) {
            return arc;
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
        dataDao.getGraph().remove(node);
        if (node.getData() != null) {
            node.getData().getShapes().remove(node);
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
                    throw new DataServiceException("Cannot style shape for an undefined arc type!");
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
                throw new DataServiceException("Cannot style shape for an undefined place type!");
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
                throw new DataServiceException("Cannot style shape for an undefined transition type!");
        }
    }

    /**
     * Validates the subtype of an arc.
     *
     * @param arc
     * @param typeArc
     * @throws DataServiceException
     */
    private void validateArc(IDataArc arc) throws DataServiceException {

        DataPlace place;
        DataTransition transition;

        if (DataType.PLACE == arc.getTarget().getType()) {

            switch (arc.getArcType()) {
                case NORMAL:
                    break;
                case INHIBITORY:
                    throw new DataServiceException("A transition cannot inhibit a place!");
                case TEST:
                    throw new DataServiceException("A transition cannot test a place!");
                case READ:
                    throw new DataServiceException("A transition cannot read a place!");
                default:
                    throw new DataServiceException("Validation for arc type '" + arc.getArcType() + "' is undefined!");
            }

            transition = (DataTransition) arc.getSource();
            place = (DataPlace) arc.getTarget();

            switch (transition.getTransitionType()) { // source

                case CONTINUOUS: {
                    switch (place.getPlaceType()) { // target
                        case CONTINUOUS:
                            break;
                        case DISCRETE:
                            throw new DataServiceException("A continuous transition cannot be connected to a discrete place!");
                        default:
                            throw new DataServiceException("Arc validation for place type '" + place.getPlaceType() + "' has not been defined!");
                    }
                }
                break;

                case DISCRETE:
                    break;

                case STOCHASTIC:
                    break;

                default:
                    throw new DataServiceException("Arc validation for transition type '" + transition.getTransitionType() + "' has not been defined!");
            }

        } else {

            place = (DataPlace) arc.getSource();
            transition = (DataTransition) arc.getTarget();

            switch (place.getPlaceType()) { // source
                case CONTINUOUS:
                    break;

                case DISCRETE: {
                    switch (transition.getTransitionType()) { // target
                        case CONTINUOUS:
                            throw new DataServiceException("A discrete place cannot be connected to a continuous transition!");
                        case DISCRETE:
                            break;
                        case STOCHASTIC:
                            break;
                        default:
                            throw new DataServiceException("Arc validation for transition type '" + transition.getTransitionType() + "' has not been defined!");
                    }
                }
                break;

                default:
                    throw new DataServiceException("Arc validation for place type '" + place.getPlaceType() + "' has not been defined!");
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

        IDataNode dataSource = source.getData();
        IDataNode dataTarget = target.getData();

        IGraphNode relatedSourceShape;
        IGraphNode relatedSourceShapeChild;

        /**
         * Ensuring the connection to be valid.
         */
        if (source.getClass().equals(target.getClass())) {
            throw new DataServiceException("Nodes of the same type cannot be connected.");
        }
        if (source instanceof GraphCluster || target instanceof GraphCluster) {
            throw new DataServiceException("Cannot create connection to a cluster from the outside.");
        }
        if (source.getChildren().contains(target) || target.getParents().contains(source)) {
            throw new DataServiceException("The nodes are already connected.");
        }
        for (IGraphElement relatedSourceElement : dataSource.getShapes()) {

            relatedSourceShape = (IGraphNode) relatedSourceElement;
            for (IGravisNode shape : relatedSourceShape.getChildren()) {
                relatedSourceShapeChild = (IGraphNode) shape;
                if (dataTarget == relatedSourceShapeChild.getData()) {
                    throw new DataServiceException("The nodes are already connected by a related element.");
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
        IDataArc data = arc.getData();
        if (data != null) {
            if (data.getType() == DataType.CLUSTER) {
                throw new DataServiceException("Cannot delete an arc that connects to a cluster!");
            }
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
        IDataNode data = node.getData();
        if (data.getType() == DataType.CLUSTER) {
            throw new DataServiceException("Cannot delete a cluster! Restore it first or delete nodes within.");
        }
        for (IGravisConnection connection : node.getConnections()) {
            validateRemoval((IGraphArc) connection);
        }
        if (data.getShapes().size() <= 1) {
            try {
                parameterService.ValidateRemoval(data);
            } catch (ParameterServiceException ex) {
                throw new DataServiceException(ex.getMessage());
            }
        }
    }

    public synchronized String getArcId(IDataNode source, IDataNode target) {
        return source.getId() + "_" + target.getId();
    }

    public synchronized String getConnectionId(IGraphNode source, IGraphNode target) {
        return source.getId() + "_" + target.getId();
    }

    public synchronized String getClusterId(DataDao dao) {
        return PREFIX_ID_CLUSTER + dao.getNextClusterId();
    }

    public String getGraphNodeId(DataDao dao) {
        return PREFIX_ID_GRAPHNODE + dao.getNextNodeId();
    }

    public String getPlaceId(DataDao dao) {
        return PREFIX_ID_PLACE + dao.getNextPlaceId();
    }

    public String getTransitionId(DataDao dao) {
        return PREFIX_ID_TRANSITION + dao.getNextTransitionId();
    }

    public synchronized Graph getGraphRoot() {
        return dataDao.getGraphRoot();
    }

    public synchronized Graph getGraph() {
        return dataDao.getGraph();
    }

    public synchronized void setGraph(Graph graph) {
        dataDao.getGraphPane().setGraph(graph);
    }

    public synchronized Model getModel() {
        return dataDao.getModel();
    }

    public ObservableList getDaos() {
        return dataDaos;
    }

    public synchronized List<DataDao> getDataDaosWithChanges() {
        List<DataDao> daosWithChanges = new ArrayList();
        for (DataDao dao : dataDaos) {
            if (dao.hasChanges()) {
                daosWithChanges.add(dao);
            }
        }
        return daosWithChanges;
    }

    public synchronized DataDao getDao() {
        return dataDao;
    }

    public synchronized void setDao(DataDao dataDao) {
        if (!dataDaos.contains(dataDao)) {
            dataDaos.add(dataDao);
        }
        this.dataDao = dataDao;
    }

    public synchronized void setArcWeight(DataArc arc, Weight weight) {
        arc.addWeight(weight);
        dataDao.setHasChanges(true);
    }

    public synchronized void setPlaceToken(DataPlace place, Token token) {
        place.addToken(token);
        dataDao.setHasChanges(true);
    }

    public synchronized void setTransitionFunction(DataTransition transition, String functionString) throws DataServiceException {
        try {
            parameterService.setTransitionFunction(dataDao.getModel(), transition, functionString);
            dataDao.setHasChanges(true);
        } catch (Exception ex) {
            throw new DataServiceException(ex.getMessage());
        }
    }

    public Colour getColourDefault() {
        return DEFAULT_COLOUR;
    }

    public void setPlaceTypeDefault(DataPlace.Type type) {
        defaultPlaceType = type;
    }

    public void setTransitionTypeDefault(DataTransition.Type type) {
        defaultTransitionType = type;
    }

    public boolean isGridEnabled() {
        return isGridEnabled.get();
    }

    public BooleanProperty isGridEnabledProperty() {
        return isGridEnabled;
    }
}
