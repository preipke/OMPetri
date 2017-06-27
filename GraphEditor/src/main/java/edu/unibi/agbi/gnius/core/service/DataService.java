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
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
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
     * @param arc
     * @return
     * @throws DataServiceException
     */
    public synchronized IGraphArc add(IGraphArc arc) throws DataServiceException {
        
        ValidateArcShape(arc);

        if (arc.getDataElement() != null) {
            if (arc.getDataElement().getElementType() == Element.Type.ARC) {
                if (dataDao.getModel().containsAndNotEqual(arc.getDataElement())) {
                    throw new DataServiceException("Conflict! Another arc has already been stored using the same ID!");
                }
                dataDao.getModel().add(arc.getDataElement());
            }
        }
        dataDao.getGraph().add(arc);
        styleElement(arc);
        return arc;
    }

    /**
     * Adds node to scene and data model.
     *
     * @param node
     * @return
     * @throws DataServiceException
     */
    public synchronized IGraphNode add(IGraphNode node) throws DataServiceException {
        if (node.getDataElement() != null) {
            if (node.getDataElement().getElementType() != Element.Type.CLUSTER) {
                if (dataDao.getModel().containsAndNotEqual(node.getDataElement())) {
                    throw new DataServiceException("Conflict! Another node has already been stored using the same ID!");
                }
                dataDao.getModel().add(node.getDataElement());
            }
        }
        dataDao.getGraph().add(node);
        styleElement(node);
        return node;
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
    public synchronized void changePlaceType(DataPlace place, DataPlace.Type type) throws DataServiceException {
        DataPlace.Type typeOld = place.getPlaceType();
        place.setPlaceType(type);
        try {
            for (IArc arc : place.getArcsIn()) {
                validateArc(arc);
            }
            for (IArc arc : place.getArcsOut()) {
                validateArc(arc);
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
    public synchronized void changeTransitionType(DataTransition transition, DataTransition.Type type) throws DataServiceException {
        DataTransition.Type typeOld = transition.getTransitionType();
        transition.setTransitionType(type);
        try {
            for (IArc arc : transition.getArcsIn()) {
                validateArc(arc);
            }
            for (IArc arc : transition.getArcsOut()) {
                validateArc(arc);
            }
        } catch (DataServiceException ex) {
            transition.setTransitionType(typeOld);
            throw ex;
        }
        dataDao.setHasChanges(true);
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
        validateArc(arc.getDataElement());
        add(arc);
        dataDao.setHasChanges(true);
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
                place = new DataPlace(getPlaceId(), defaultPlaceType);
                place.addToken(new Token(DEFAULT_COLOUR));
                shape = new GraphPlace(getGraphNodeId(), place);
                break;

            case TRANSITION:
                DataTransition transition;
                transition = new DataTransition(getTransitionId(), defaultTransitionType);
                shape = new GraphTransition(getGraphNodeId(), transition);
                break;

            default:
                throw new DataServiceException("Cannot create element of undefined type!");
        }
        Point2D pos = calculator.getCorrectedMousePosition(dataDao.getGraph(), posX, posY);
        if (isGridEnabled()) {
            pos = calculator.getPositionInGrid(pos, getGraph().getScale());
        }
        shape.translateXProperty().set(pos.getX() - shape.getCenterOffsetX());
        shape.translateYProperty().set(pos.getY() - shape.getCenterOffsetY());
        shape = add(shape);
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
    public IGraphArc createConnection(IGraphNode source, IGraphNode target, DataArc dataArc) {
        
        String id;

        /**
         * Create data.
         */
        id = getArcId(source.getDataElement(), target.getDataElement());
        if (dataArc == null) {
            dataArc = new DataArc(id, source.getDataElement(), target.getDataElement(), defaultArcType);
            dataArc.addWeight(new Weight(DEFAULT_COLOUR));
        }

        /**
         * Creating shape.
         */
        id = getConnectionId(source, target);
        if (source.getParents().contains(target) && target.getChildren().contains(source)) {
            return new GraphCurve(id, source, target, dataArc);
        } else {
            return new GraphEdge(id, source, target, dataArc);
        }
    }

    /**
     * Creates an arc that binds its source to the given node.
     *
     * @param source
     * @return
     */
    public synchronized IGraphArc createConnectionTmp(IGraphNode source) {
        GraphEdge edge;
        edge = new GraphEdge(source.getId() + "null", source);
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
    public DataDao createDao() {
        DataDao dao = new DataDao();
        dao.setAuthor(System.getProperty("user.name"));
        dao.setCreationDateTime(LocalDateTime.now());
        dao.setModelDescription("New model.");
        dao.setDaoId(String.valueOf(System.nanoTime()));
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
        removeData(arc.getDataElement());
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
        
        IGraphArc arc;
        while (!node.getConnections().isEmpty()) {
            arc = (IGraphArc) node.getConnections().get(0);
            removeShape(arc);
            removeData(arc.getDataElement());
        }
        removeShape(node);
        removeData(node.getDataElement());
        dataDao.setHasChanges(true);

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
                messengerService.addException("Cannot remove element '" + element.getDataElement().getId() + "'!", ex);
            }
        }
        dataDao.setHasChanges(true);
    }

    /**
     * Pastes given node(s). Either copies or clones nodes, inserting them at
     * the latest mouse pointer location.
     *
     * @param nodes
     * @param cut
     * @return
     * @throws DataServiceException
     */
    public synchronized List<IGraphNode> paste(List<IGraphNode> nodes, boolean cut) throws DataServiceException {

        List<IGraphNode> shapes = new ArrayList();
        IGraphNode shape;

        Point2D center = calculator.getCenterN(nodes);
        Point2D position = calculator.getCorrectedMousePositionLatest(dataDao.getGraph());
        
        if (isGridEnabled()) {
            center = calculator.getPositionInGrid(center, getGraph().getScale());
            position = calculator.getPositionInGrid(position, getGraph().getScale());
        }

        for (int i = 0; i < nodes.size(); i++) {

            if (cut) {
//                shape = clone(nodes.get(i));
                shape = nodes.get(i);
            } else {
                shape = copy(nodes.get(i));
                if (shape == null) {
                    continue;
                }
                add(shape);
            }

            shape.translateXProperty().set(nodes.get(i).translateXProperty().get() - center.getX() + position.getX() - shape.getCenterOffsetX());
            shape.translateYProperty().set(nodes.get(i).translateYProperty().get() - center.getY() + position.getY() - shape.getCenterOffsetY());

            shapes.add(shape);
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
        switch (element.getDataElement().getElementType()) {
            case ARC:
                styleArc((DataArc) element.getDataElement());
                break;
            case CLUSTER:
                setElementStyle(element.getDataElement(), stylceCluster, stylceCluster);
                break;
            case CLUSTERARC:
                setElementStyle(element.getDataElement(), styleClusterArc, styleClusterArc);
                ((IGraphArc) element).setCircleHeadVisible(false);
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
                return new GraphPlace(getGraphNodeId(), (DataPlace) node);
            case TRANSITION:
                return new GraphTransition(getGraphNodeId(), (DataTransition) node);
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
     */
    private IGraphNode copy(IGraphNode target) {
        IDataNode node = target.getDataElement();
        switch (node.getElementType()) {
            case PLACE:
                DataPlace place;
                place = new DataPlace(getPlaceId(), ((DataPlace) node).getPlaceType());
                place.addToken(new Token(DEFAULT_COLOUR));
                return new GraphPlace(getGraphNodeId(), place);
            case TRANSITION:
                DataTransition transition;
                transition = new DataTransition(getTransitionId(), ((DataTransition) node).getTransitionType());
                return new GraphTransition(getGraphNodeId(), transition);
            default:
                return null;
        }
    }

    /**
     * Converts the reverse arc for an arc. Converts the arcs for one-way or
     * two-way connected nodes to straight or curved arcs, respectively.
     *
     * @param arc
     * @throws DataServiceException
     */
    public synchronized void ValidateArcShape(IGraphArc arc) throws DataServiceException {

        IGraphNode source = arc.getSource();
        IGraphNode target = arc.getTarget();

        // Find and convert double linked arc
        if (target.getChildren().contains(source)) {

            IGraphArc arcReverse = null;

            for (int i = 0; i < target.getConnections().size(); i++) {
                if (target.getConnections().get(i).getTarget().equals(source)) {
                    arcReverse = (IGraphArc) target.getConnections().get(i);
                    break;
                }
            }

            if (arcReverse == null) {
                throw new DataServiceException("Data integrity breached! Reversely connecting arc was not found!");
            }

            // Temporarily remove parameters to allow conversion without failing validation
            Set<String> paramsTmp = new TreeSet();
            for (String key : arcReverse.getDataElement().getRelatedParameterIds()) {
                paramsTmp.add(key);
            }
            arcReverse.getDataElement().getRelatedParameterIds().clear();

            // Convert
            remove(arcReverse);
            String id = getConnectionId(target, source);
            if (arcReverse instanceof GraphEdge) {
                arcReverse = new GraphCurve(id, target, source, arcReverse.getDataElement());
            } else {
                arcReverse = new GraphEdge(id, target, source, arcReverse.getDataElement());
            }
            add(arcReverse);

            // Restore parameters again
            arcReverse.getDataElement().getRelatedParameterIds().addAll(paramsTmp);
            
            
        }
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
        if (arc.getDataElement() != null) {
            arc.getDataElement().getShapes().remove(arc);
        }

        // Check null for temporary arcs
        if (arc.getTarget() == null) {
            return arc;
        }
        
        ValidateArcShape(arc);
        
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
    private void validateArc(IArc arc) throws DataServiceException {

        DataPlace place;
        DataTransition transition;

        if (Element.Type.PLACE == arc.getTarget().getElementType()) {

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

        IDataNode dataSource = source.getDataElement();
        IDataNode dataTarget = target.getDataElement();

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
            for (int i = 0; i < relatedSourceShape.getChildren().size(); i++) {

                relatedSourceShapeChild = (IGraphNode) relatedSourceShape.getChildren().get(i);
                if (dataTarget == relatedSourceShapeChild.getDataElement()) {
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
        IDataArc data = arc.getDataElement();
        if (data != null) {
            if (data.getElementType() == Element.Type.CLUSTERARC) {
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
        IDataNode data = node.getDataElement();
        if (data.getElementType() == Element.Type.CLUSTER) {
            throw new DataServiceException("Cannot delete a cluster! Restore it first or delete nodes within.");
        }
        for (IGravisConnection connection : node.getConnections()) {
            validateRemoval((IGraphArc) connection);
        }
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
    
    public synchronized String getArcId(IDataNode source, IDataNode target) {
        return source.getId() + target.getId();
    }
    
    public synchronized String getConnectionId(IGraphNode source, IGraphNode target) {
        return source.getId() + target.getId();
    }

    public synchronized String getClusterId() {
        return PREFIX_ID_CLUSTER + dataDao.getNextClusterId();
    }

    private String getGraphNodeId() {
        return PREFIX_ID_GRAPHNODE + dataDao.getNextNodeId();
    }

    private String getPlaceId() {
        return PREFIX_ID_PLACE + dataDao.getNextPlaceId();
    }

    private String getTransitionId() {
        return PREFIX_ID_TRANSITION + dataDao.getNextTransitionId();
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
            parameterService.setTransitionFunction(transition, functionString);
            dataDao.setHasChanges(true);
        } catch (Exception ex) {
            throw new DataServiceException("Cannot build function from input '" + functionString + "'! [" + ex.getMessage() + "]");
        }
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
