/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.business.service;

import edu.unibi.agbi.editor.business.exception.DataException;
import edu.unibi.agbi.editor.core.data.dao.ModelDao;
import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataPlace;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataTransition;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.editor.core.util.Calculator;
import edu.unibi.agbi.petrinet.entity.INode;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import javafx.geometry.Point2D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Handles name/ID generation for elements and parameters in a model.
 * 
 * @author PR
 */
@Component
public class FactoryService
{
    @Autowired private Calculator calculator;
    
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
    
    private static final Colour DEFAULT_COLOUR = new Colour("WHITE", "Default colour");

    private static final String PREFIX_ID_GRAPHNODE = "N";
    private static final String PREFIX_ID_PLACE = "P";
    private static final String PREFIX_ID_TRANSITION = "T";

    private final DataArc.Type defaultArcType = DataArc.Type.NORMAL;
    private DataPlace.Type defaultPlaceType = DataPlace.Type.CONTINUOUS;
    private DataTransition.Type defaultTransitionType = DataTransition.Type.CONTINUOUS;

    /**
     * Creates a copy of the given node. Results in a node of the same type as
     * the given node.
     *
     * @param target
     * @return
     */
    IGraphNode copy(ModelDao modelDao, IGraphNode target) {
        IDataNode node = target.getData();
        switch (node.getType()) {
            case PLACE:
                DataPlace place;
                place = new DataPlace(getPlaceId(modelDao), ((DataPlace) node).getPlaceType());
                place.addToken(new Token(DEFAULT_COLOUR));
                return new GraphPlace(getGraphNodeId(modelDao), place);
            case TRANSITION:
                DataTransition transition;
                transition = new DataTransition(getTransitionId(modelDao), ((DataTransition) node).getTransitionType());
                return new GraphTransition(getGraphNodeId(modelDao), transition);
            default:
                return null;
        }
    }

    /**
     * Creates an arc connecting the given nodes.
     *
     * @param source
     * @param target
     * @param dataArc
     * @return
     */
    IGraphArc CreateConnection(IGraphNode source, IGraphNode target, DataArc dataArc) {

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
    IGraphArc CreateConnectionTmp(IGraphNode source) {
        GraphArc edge;
        edge = new GraphArc(source.getId() + "_null", source);
        edge.getRootHandles().forEach(ele -> {
            ele.setActiveStyleClass(styleArcDefault);
        });
        edge.getChildHandles().forEach(ele -> {
            ele.setActiveStyleClass(styleArcDefaultHead);
        });
        edge.setArrowHeadVisible(true);
        edge.setCircleHeadVisible(false);
        return edge;
    }

    /**
     * Creates a node of the specified type at the given event position.
     *
     * @param modelDao
     * @param type
     * @param posX
     * @param posY
     * @return
     * @throws DataException
     */
    IGraphNode CreateNode(ModelDao modelDao, DataType type, double posX, double posY) throws DataException {
        
        IGraphNode shape;
        switch (type) {
            case PLACE:
                DataPlace place;
                place = new DataPlace(getPlaceId(modelDao), defaultPlaceType);
                place.addToken(new Token(DEFAULT_COLOUR));
                shape = new GraphPlace(getGraphNodeId(modelDao), place);
                break;

            case TRANSITION:
                DataTransition transition;
                transition = new DataTransition(getTransitionId(modelDao), defaultTransitionType);
                shape = new GraphTransition(getGraphNodeId(modelDao), transition);
                break;

            default:
                throw new DataException("Cannot create element of undefined type!");
        }
        
        Point2D pos;
        pos = calculator.getCorrectedPosition(modelDao.getGraph(), posX, posY);
        pos = calculator.getPositionInGrid(pos, modelDao.getGraph());
            
        shape.translateXProperty().set(pos.getX() - shape.getCenterOffsetX());
        shape.translateYProperty().set(pos.getY() - shape.getCenterOffsetY());
        
        return shape;
    }

    /**
     * Creates a cloned node.Results in a node of the same type that references
     * the given data.
     *
     * @param modelDao
     * @param data
     * @param posX
     * @param posY
     * @return
     * @throws DataException
     */
    IGraphNode CreateClone(ModelDao modelDao, IDataNode data, double posX, double posY) throws DataException {
        
        /**
         * Adjust data's enabled state.
         */
        if (data.isDisabled()) {
            data.setDisabled(false);
            data.getShapes().forEach(shape -> shape.setElementDisabled(true));
        }
        
        IGraphNode clone;
        switch (data.getType()) {
            case PLACE:
                clone = new GraphPlace(getGraphNodeId(modelDao), (DataPlace) data);
                clone.getLabels().get(1).setText(((DataPlace) data).getTokenLabelText());
                break;
            case TRANSITION:
                clone = new GraphTransition(getGraphNodeId(modelDao), (DataTransition) data);
                break;
            default:
                throw new DataException("Cannot clone the given type of element! [" + data.getType() + "]");
        }
        
        Point2D pos;
        pos = calculator.getCorrectedPosition(modelDao.getGraph(), posX, posY);
        pos = calculator.getPositionInGrid(pos, modelDao.getGraph());
        
        clone.translateXProperty().set(pos.getX() - clone.getCenterOffsetX());
        clone.translateYProperty().set(pos.getY() - clone.getCenterOffsetY());
        clone.getLabels().get(0).setText(data.getLabelText());
        
        return clone;
    }

    /**
     * Creates a new data access object.
     *
     * @return
     */
    public ModelDao CreateDao() {
        ModelDao dao = new ModelDao();
        dao.setAuthor(System.getProperty("user.name"));
        dao.setCreationDateTime(LocalDateTime.now());
        dao.setModelId(String.valueOf(System.nanoTime()));
        dao.setModelName("Untitled");
        dao.getModel().add(DEFAULT_COLOUR);
        dao.setHasChanges(false);
        return dao;
    }

    /**
     * Styles the graph element in the scene according to the type assigned to
     * its data element.
     *
     * @param element
     * @throws DataException
     */
    public void StyleElement(IGraphElement element) throws DataException {
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
                throw new DataException("Cannot style element of undefined type!");
        }
    }

    public void styleArc(DataArc arc) throws DataException {
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
                case TEST:
                    arc.getShapes().forEach(s -> {
                        IGraphArc a = (IGraphArc) s;
                        a.setArrowHeadVisible(true);
                        a.setCircleHeadVisible(false);
                    });
                    setElementStyle(arc, styleArcTest, styleArcTestHead);
                    break;
                default:
                    throw new DataException("Cannot style shape for an undefined arc type!");
            }
        }
    }

    public void stylePlace(DataPlace place) throws DataException {
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
                throw new DataException("Cannot style shape for an undefined place type!");
        }
    }

    public void styleTransition(DataTransition transition) throws DataException {
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
                throw new DataException("Cannot style shape for an undefined transition type!");
        }
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
            elem.getRootHandles().forEach(s -> {
                s.setActiveStyleClass(styleParent);
            });
            elem.getChildHandles().forEach(s -> {
                s.setActiveStyleClass(styleChildren);
            });
        }
    }

    public void setPlaceTypeDefault(DataPlace.Type type) {
        defaultPlaceType = type;
    }

    public void setTransitionTypeDefault(DataTransition.Type type) {
        defaultTransitionType = type;
    }

    public Colour getColourDefault() {
        return DEFAULT_COLOUR;
    }

    public synchronized String getArcId(INode source, INode target) {
        return source.getId() + "_" + target.getId();
    }

    public synchronized String getConnectionId(IGraphNode source, IGraphNode target) {
        return source.getId() + "_" + target.getId();
    }

    public String getGraphNodeId(ModelDao dao) {
        return PREFIX_ID_GRAPHNODE + dao.getNextNodeId();
    }

    public String getPlaceId(ModelDao dao) {
        return PREFIX_ID_PLACE + dao.getNextPlaceId();
    }

    public String getTransitionId(ModelDao dao) {
        return PREFIX_ID_TRANSITION + dao.getNextTransitionId();
    }
    
}
