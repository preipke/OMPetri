/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.business.service;

import edu.unibi.agbi.editor.business.exception.DataException;
import edu.unibi.agbi.editor.business.exception.ParameterException;
import edu.unibi.agbi.editor.core.data.dao.ModelDao;
import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.core.data.entity.data.IDataArc;
import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataPlace;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataTransition;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphCluster;
import edu.unibi.agbi.editor.core.util.Calculator;
import edu.unibi.agbi.gravisfx.entity.root.connection.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.root.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Model;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class ModelService
{
    @Autowired private Calculator calculator;
    @Autowired private FactoryService factoryService;
    @Autowired private MessengerService messengerService;
    @Autowired private ParameterService parameterService;

    private final ObservableList<ModelDao> dataDaos = FXCollections.observableArrayList();
    private ModelDao modelDaoActive;

    private final BooleanProperty isGridEnabled = new SimpleBooleanProperty(true);

    /**
     * Adds a colour.
     *
     * @param colour
     * @throws DataException
     */
    public synchronized void add(Colour colour) throws DataException {
        modelDaoActive.setHasChanges(true);
        if (modelDaoActive.getModel().getColours().contains(colour)) {
            throw new DataException("Conflict! Another colour has already been stored using the same ID!");
        }
        modelDaoActive.getModel().add(colour);
    }

    /**
     * Adds arc to scene and data model.
     *
     * @param dao
     * @param arc
     * @return
     * @throws DataException
     */
    public synchronized IGraphArc add(ModelDao dao, IGraphArc arc) throws DataException {
        if (arc.getData() != null) {
            if (arc.getData().getType() != DataType.CLUSTERARC) {
                try {
                    dao.getModel().add(arc.getData());
                } catch (Exception ex) {
                    throw new DataException("Arc conflict!", ex);
                }
            }
        }
        dao.getGraph().add(arc);
        factoryService.StyleElement(arc);
        return arc;
    }

    /**
     * Adds node to scene and data model.
     *
     * @param dao
     * @param node
     * @return
     * @throws DataException
     */
    public synchronized IGraphNode add(ModelDao dao, IGraphNode node) throws DataException {
        if (node.getData() != null) {
            if (node.getData().getType() != DataType.CLUSTER) {
                try {
                    dao.getModel().add(node.getData());
                } catch (Exception ex) {
                    throw new DataException("Node conflict!", ex);
                }
            }
        }
        dao.getGraph().add(node);
        factoryService.StyleElement(node);
        return node;
    }
    
    /**
     * Validate an ID to be available.
     * 
     * @param id
     * @throws DataException 
     */
    private void validateIdAvailable(String id) throws DataException {
        if (modelDaoActive.getModel().containsElement(id)) {
            throw new DataException("The specified ID is already used by another element!");
        }
        if (modelDaoActive.getModel().containsParameter(id)) {
            throw new DataException("The specified ID is already used by a parameter!");
        }
    }
    
    private void changeElementsId(IDataElement data, String id) {
        
        
    }
    
    /**
     * Attempts to change the ID/name of a data element.
     * 
     * @param data
     * @param id 
     * @throws DataException 
     */
    public synchronized void changeId(IDataElement data, String id) throws DataException {
        
        final String oldId = data.getId();
        
        if (id.contentEquals(oldId)) {
            return;
        }
        
        try {

            // Validate ID 
            validateIdAvailable(id);
            modelDaoActive.getModel().changeId(data, id);
            
            
            if (data instanceof IDataNode) {
                IDataNode node = (IDataNode) data;
                
                // Validate IDs for related arcs
                try {
                    for (IArc arc : node.getArcsIn()) {
                        id = factoryService.getArcId(arc.getSource(), arc.getTarget());
                        validateIdAvailable(id);
                    }
                    for (IArc arc : node.getArcsOut()) {
                        id = factoryService.getArcId(arc.getSource(), arc.getTarget());
                        validateIdAvailable(id);
                    }
                } catch (DataException ex) {
                    throw new DataException("The resulting ID of a related arc is already used inside the model!");
                }
                
                // Change IDs for related arcs
                for (IArc arc: node.getArcsIn()) {
                    if (arc.getId().matches(".+_" + oldId)) {
                        id = factoryService.getArcId(arc.getSource(), arc.getTarget());
                        modelDaoActive.getModel().changeId(arc, id);
                        
                        
                    }
                }
                for (IArc arc: node.getArcsOut()) {
                    if (arc.getId().matches(oldId + "_.+")) {
                        id = factoryService.getArcId(arc.getSource(), arc.getTarget());
                        modelDaoActive.getModel().changeId(arc, id);
                        
                        
                    }
                }
                
            }
            
            
        } catch (Exception ex) {
            try {
                modelDaoActive.getModel().changeId(data, oldId);
            } catch (Exception exFatal) {
                throw new DataException("A conflict was detected when changing an ID, but revoking the action failed! Possible data integrity breach!", exFatal);
            }
            throw new DataException(ex.getMessage());
        }
        
    }

    public synchronized void changeSubtype(IDataElement element, Object subtype) throws DataException {

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
                    factoryService.setPlaceTypeDefault(placeType);
                    changePlaceType(place, placeType);
                }
                break;

            case TRANSITION:
                DataTransition transition = (DataTransition) element;
                DataTransition.Type transitionType = (DataTransition.Type) subtype;
                if (transition.getTransitionType() != transitionType) {
                    factoryService.setTransitionTypeDefault(transitionType);
                    changeTransitionType(transition, transitionType);
                }
                break;
        }
    }

    /**
     * Connects the given graph nodes.Validates the connection, then creates and
     * adds a new graph arc to the scene.
     *
     * @param dao
     * @param source
     * @param target
     * @return
     * @throws DataException
     */
    public synchronized IGraphArc connect(ModelDao dao, IGraphNode source, IGraphNode target) throws DataException {
        IGraphArc arc = factoryService.CreateConnection(source, target, null);
        validateConnection(source, target);
        validateArc(arc.getData());
        add(dao, arc);
        dao.setHasChanges(true);
        return arc;
    }
    
    public synchronized IGraphNode clone(ModelDao modelDao, IDataNode data, double posX, double posY) throws DataException {
        IGraphNode node;
        node = factoryService.CreateClone(modelDao, data, posX, posY);
        node = add(modelDao, node);
        modelDao.setHasChanges(true);
        return node;
    }

    public synchronized IGraphNode create(ModelDao modelDao, DataType type, double posX, double posY) throws DataException {
        IGraphNode node;
        node = factoryService.CreateNode(modelDao, type, posX, posY);
        node = add(modelDao, node);
        modelDao.setHasChanges(true);
        return node;
    }

    /**
     * Creates an arc that binds its source to the given node. Used when
     * creating new arcs in the editor.
     *
     * @param source
     * @return
     */
    public synchronized IGraphArc createTmpArc(IGraphNode source) {
        IGraphArc arc;
        arc = factoryService.CreateConnectionTmp(source);
        modelDaoActive.getGraph().add(arc);
        return arc;
    }

    /**
     * Removes a data access object.
     *
     * @param dataDao
     */
    public synchronized void remove(ModelDao dataDao) {
        dataDaos.remove(dataDao);
    }

    /**
     * Removes the given graph arc from the scene.
     *
     * @param arc
     * @return
     * @throws DataException
     */
    public synchronized IGraphArc remove(IGraphArc arc) throws DataException {
        validateRemoval(arc);
        removeShape(arc);
        try {
            removeData(arc.getData());
        } catch (Exception ex) {
            throw new DataException(ex.getMessage());
        }
        modelDaoActive.setHasChanges(true);
        return arc;
    }

    /**
     * Removes the given graph node from the scene. Also removes all arcs
     * connected to the given node.
     *
     * @param node
     * @return
     * @throws DataException
     */
    public synchronized IGraphNode remove(IGraphNode node) throws DataException {
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
            modelDaoActive.setHasChanges(true);
        } catch (Exception ex) {
            throw new DataException(ex.getMessage());
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
            } catch (DataException ex) {
                messengerService.printMessage("Cannot remove element(s)!");
                messengerService.addException("Cannot remove element '" + element.getData().getId() + "'!", ex);
            }
        }
        modelDaoActive.setHasChanges(true);
    }

    public IGraphNode copy(IGraphNode target) {
        return factoryService.copy(modelDaoActive, target);
    }

    /**
     * Pastes given node(s).Either copies or clones nodes, inserting them at the
     * latest mouse pointer location.
     *
     * @param dao
     * @param nodes
     * @param cut
     * @return
     * @throws DataException
     */
    public synchronized List<IGraphNode> paste(ModelDao dao, List<IGraphNode> nodes, boolean cut) throws DataException {

        List<IGraphNode> shapes = new ArrayList();
        IGraphNode shape;

        Point2D posCenter = calculator.getCenterN(nodes);
        Point2D posMouse = calculator.getCorrectedMousePositionLatest(modelDaoActive.getGraph());

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

        modelDaoActive.setHasChanges(true);
        return shapes;
    }

    /**
     * Changes the subtype of an arc. Styles all related shapes in the scene
     * accordingly.
     *
     * @param arc
     * @param type
     * @throws DataException
     */
    private void changeArcType(DataArc arc, DataArc.Type type) throws DataException {
        DataArc.Type typeOld = arc.getArcType();
        arc.setArcType(type);
        try {
            validateArc(arc);
        } catch (DataException ex) {
            arc.setArcType(typeOld);
            throw ex;
        }
        modelDaoActive.setHasChanges(true);
        factoryService.styleArc(arc);
    }

    /**
     * Changes the subtype of a place. Styles all related shapes in the scene
     * accordingly.
     *
     * @param place
     * @param type
     * @throws DataException
     */
    private void changePlaceType(DataPlace place, DataPlace.Type type) throws DataException {
        DataPlace.Type typeOld = place.getPlaceType();
        place.setPlaceType(type);
        try {
            for (IArc arc : place.getArcsIn()) {
                validateArc((IDataArc) arc);
            }
            for (IArc arc : place.getArcsOut()) {
                validateArc((IDataArc) arc);
            }
        } catch (DataException ex) {
            place.setPlaceType(typeOld);
            throw ex;
        }
        modelDaoActive.setHasChanges(true);
        factoryService.stylePlace(place);
    }

    /**
     * Changes the subtype of a transition. Styles all related shapes in the
     * scene accordingly.
     *
     * @param transition
     * @param type
     * @throws DataException
     */
    private void changeTransitionType(DataTransition transition, DataTransition.Type type) throws DataException {
        DataTransition.Type typeOld = transition.getTransitionType();
        transition.setTransitionType(type);
        try {
            for (IArc arc : transition.getArcsIn()) {
                validateArc((IDataArc) arc);
            }
            for (IArc arc : transition.getArcsOut()) {
                validateArc((IDataArc) arc);
            }
        } catch (DataException ex) {
            transition.setTransitionType(typeOld);
            throw ex;
        }
        modelDaoActive.setHasChanges(true);
        factoryService.styleTransition(transition);
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
                modelDaoActive.getModel().remove(element);
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
     * @throws DataException
     */
    private IGraphArc removeShape(IGraphArc arc) throws DataException {

        modelDaoActive.getGraph().remove(arc);
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
     * @throws DataException
     */
    private IGraphNode removeShape(IGraphNode node) throws DataException {
        modelDaoActive.getGraph().remove(node);
        if (node.getData() != null) {
            node.getData().getShapes().remove(node);
        }
        return node;
    }

    /**
     * Validates the subtype of an arc.
     *
     * @param arc
     * @param typeArc
     * @throws DataException
     */
    private void validateArc(IDataArc arc) throws DataException {

        DataPlace place;
        DataTransition transition;

        if (DataType.PLACE == arc.getTarget().getType()) {

            switch (arc.getArcType()) {
                case NORMAL:
                    break;
                case INHIBITORY:
                    throw new DataException("A transition cannot inhibit a place!");
                case TEST:
                    throw new DataException("A transition cannot test a place!");
                case READ:
                    throw new DataException("A transition cannot read a place!");
                default:
                    throw new DataException("Validation for arc type '" + arc.getArcType() + "' is undefined!");
            }

            transition = (DataTransition) arc.getSource();
            place = (DataPlace) arc.getTarget();

            switch (transition.getTransitionType()) { // source

                case CONTINUOUS: {
                    switch (place.getPlaceType()) { // target
                        case CONTINUOUS:
                            break;
                        case DISCRETE:
                            throw new DataException("A continuous transition cannot be connected to a discrete place!");
                        default:
                            throw new DataException("Arc validation for place type '" + place.getPlaceType() + "' has not been defined!");
                    }
                }
                break;

                case DISCRETE:
                    break;

                case STOCHASTIC:
                    break;

                default:
                    throw new DataException("Arc validation for transition type '" + transition.getTransitionType() + "' has not been defined!");
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
                            throw new DataException("A discrete place cannot be connected to a continuous transition!");
                        case DISCRETE:
                            break;
                        case STOCHASTIC:
                            break;
                        default:
                            throw new DataException("Arc validation for transition type '" + transition.getTransitionType() + "' has not been defined!");
                    }
                }
                break;

                default:
                    throw new DataException("Arc validation for place type '" + place.getPlaceType() + "' has not been defined!");
            }
        }
    }

    /**
     * Validates a connection between two graph nodes.
     *
     * @param source
     * @param target
     * @throws DataException thrown in case the connection is not valid
     */
    private void validateConnection(IGraphNode source, IGraphNode target) throws DataException {

        IDataNode dataSource = source.getData();
        IDataNode dataTarget = target.getData();

        IGraphNode relatedSourceShape;
        IGraphNode relatedSourceShapeChild;

        /**
         * Ensuring the connection to be valid.
         */
        if (source.getClass().equals(target.getClass())) {
            throw new DataException("Nodes of the same type cannot be connected.");
        }
        if (source instanceof GraphCluster || target instanceof GraphCluster) {
            throw new DataException("Cannot create connection to a cluster from the outside.");
        }
        if (source.getChildren().contains(target) || target.getParents().contains(source)) {
            throw new DataException("The nodes are already connected.");
        }
        for (IGraphElement relatedSourceElement : dataSource.getShapes()) {

            relatedSourceShape = (IGraphNode) relatedSourceElement;
            for (IGravisNode shape : relatedSourceShape.getChildren()) {
                relatedSourceShapeChild = (IGraphNode) shape;
                if (dataTarget == relatedSourceShapeChild.getData()) {
                    throw new DataException("The nodes are already connected by a related element.");
                }
            }
        }
    }

    /**
     * Validates the potential removal of a graph arc.
     *
     * @param arc
     * @throws DataException thrown in case the graph arc can not be deleted
     */
    private void validateRemoval(IGraphArc arc) throws DataException {
        IDataArc data = arc.getData();
        if (data != null) {
            if (data.getType() == DataType.CLUSTER) {
                throw new DataException("Cannot delete an arc that connects to a cluster!");
            }
            if (data.getShapes().size() <= 1) {
                try {
                    parameterService.ValidateRemoval(data);
                } catch (ParameterException ex) {
                    throw new DataException(ex.getMessage());
                }
            }
        }
    }

    /**
     * Validates the potential removal of a graph node.
     *
     * @param node
     * @throws DataException
     */
    private void validateRemoval(IGraphNode node) throws DataException {
        IDataNode data = node.getData();
        if (data.getType() == DataType.CLUSTER) {
            throw new DataException("Cannot delete a cluster! Restore it first or delete nodes within.");
        }
        for (IGravisConnection connection : node.getConnections()) {
            validateRemoval((IGraphArc) connection);
        }
        if (data.getShapes().size() <= 1) {
            try {
                parameterService.ValidateRemoval(data);
            } catch (ParameterException ex) {
                throw new DataException(ex.getMessage());
            }
        }
    }

    public synchronized Graph getGraph() {
        return modelDaoActive.getGraph();
    }

    public synchronized Model getModel() {
        return modelDaoActive.getModel();
    }

    public synchronized List<ModelDao> getDataDaosWithChanges() {
        List<ModelDao> daosWithChanges = new ArrayList();
        for (ModelDao dao : dataDaos) {
            if (dao.hasChanges()) {
                daosWithChanges.add(dao);
            }
        }
        return daosWithChanges;
    }

    public ObservableList<ModelDao> getDaos() {
        return dataDaos;
    }

    public synchronized ModelDao getDao() {
        return modelDaoActive;
    }

    public synchronized void setDao(ModelDao dataDao) {
        if (!dataDaos.contains(dataDao)) {
            dataDaos.add(dataDao);
        }
        this.modelDaoActive = dataDao;
    }

    public synchronized void setArcWeight(DataArc arc, Weight weight) {
        arc.addWeight(weight);
        modelDaoActive.setHasChanges(true);
    }

    public synchronized void setPlaceToken(DataPlace place, Token token) {
        place.addToken(token);
        modelDaoActive.setHasChanges(true);
    }

    public synchronized void setElementFunction(IDataElement element, Function function, Colour colour) throws DataException {
        try {
            parameterService.setFunction(modelDaoActive.getModel(), element, function, colour);
            modelDaoActive.setHasChanges(true);
        } catch (Exception ex) {
            throw new DataException(ex.getMessage());
        }
    }

    public boolean isGridEnabled() {
        return isGridEnabled.get();
    }

    public BooleanProperty isGridEnabledProperty() {
        return isGridEnabled;
    }
}
