/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.io;

import edu.unibi.agbi.editor.business.exception.DataException;
import edu.unibi.agbi.editor.business.exception.ParameterException;
import edu.unibi.agbi.editor.business.service.FactoryService;
import edu.unibi.agbi.editor.core.data.dao.ModelDao;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataPlace;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataTransition;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphCluster;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.editor.business.service.HierarchyService;
import edu.unibi.agbi.editor.business.service.ParameterService;
import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.core.util.Calculator;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import edu.unibi.agbi.petrinet.model.ConflictResolutionStrategy;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import edu.unibi.agbi.petrinet.util.FunctionFactory;
import edu.unibi.agbi.petrinet.util.ParameterFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Point2D;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author PR
 */
@Component
public class ModelSbmlConverter {

    @Autowired
    private FactoryService factoryService;
    @Autowired
    private HierarchyService hierarchyService;
    @Autowired
    private ParameterService parameterService;
    @Autowired
    private FunctionFactory functionBuilder;
    @Autowired
    private ParameterFactory parameterFactory;
    @Autowired
    private Calculator calculator;

    private final String tagConflictStrategy = "ConflictStrategy";
    private final String tagConnection = "reaction";
    private final String tagConnectionNode = "speciesReference";
    private final String tagConnectionSources = "listOfReactants";
    private final String tagConnectionTargets = "listOfProducts";
    private final String tagConstant = "constCheck";
    private final String tagCoordinateX = "x_Coordinate";
    private final String tagCoordinateY = "y_Coordinate";
    private final String tagCluster = "coarseNode";
    private final String tagClusterChild = "child";
    private final String tagDisabled = "knockedOut";
    private final String tagFunction = "maximumSpeed";
    private final String tagLabel = "label";
    private final String tagModel = "model";
    private final String tagName = "Name";
    private final String tagNodes = "listOfSpecies";
    private final String tagNode = "species";
    private final String tagParameter = "Parameter";
    private final String tagProbability = "Probability";
    private final String tagPriority = "Priority";
    private final String tagRefAvailable = "hasRef";
    private final String tagRefId = "RefID";
    private final String tagTokenMax = "tokenMax";
    private final String tagTokenMin = "tokenMin";
    private final String tagTokenStart = "tokenStart";
    private final String tagType = "BiologicalElement";
    private final String tagUnit = "Unit";
    private final String tagValue = "Value";
    private final String tagWeight = "Function";

    private final String attrConnectionNodeId = "species";
    private final String attrConstant = "constCheck";
    private final String attrCoordinateX = "x_Coordinate";
    private final String attrCoordinateY = "y_Coordinate";
    private final String attrDisabled = "knockedOut";
    private final String attrFunction = "maximumSpeed";
    private final String attrId = "id";
    private final String attrLabel = "label";
    private final String attrName = "name";
    private final String attrNameParameter = "Name";
    private final String attrRefAvailable = "hasRef";
    private final String attrRefId = "RefID";
    private final String attrTokenMax = "tokenMax";
    private final String attrTokenMin = "tokenMin";
    private final String attrTokenStart = "tokenStart";
    private final String attrType = "BiologicalElement";
    private final String attrUnit = "Unit";
    private final String attrValue = "Value";
    private final String attrWeight = "Function";

    public ModelDao importSbml(File file) throws Exception {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
//        doc.getDocumentElement().normalize();

        NodeList nl;
        List<Element> elems;
        Element model, elem;
        ModelDao dao;
        Map<String, String> idReferenceMap;
        String tmp;

        /**
         * Model.
         */
        nl = doc.getElementsByTagName(tagModel);
        if (nl.getLength() >= 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                model = (Element) nl.item(0);
                dao = factoryService.CreateDao();
                dao.setAuthor("");
                dao.setModelName(model.getAttribute(attrId));
            } else {
                throw new Exception("File import failed. Malformed 'Model' element.");
            }
        } else {
            throw new Exception("File import failed. More than one or no 'Model' element.");
        }

        /**
         * Nodes and Shapes.
         */
        elems = new ArrayList();
        idReferenceMap = new HashMap();

        nl = doc.getElementsByTagName(tagNodes);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) nl.item(0);
                nl = elem.getElementsByTagName(tagNode);
                for (int i = 0; i < nl.getLength(); i++) { // non-refering nodes
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        elem = addNode((Element) nl.item(i), dao, false, null);
                        if (elem != null) {
                            elems.add(elem);
                        }
                    }
                }
                for (Element e : elems) { // referencing nodes
                    addNode(e, dao, true, idReferenceMap);
                }
            }
        }

        /**
         * Arcs.
         */
        nl = doc.getElementsByTagName(tagConnection);
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) nl.item(i);
                addConnection(elem, dao);
            }
        }
//        for (Element e : elems) { // disable connections for referencing nodes
//            dao.getGraph()
//                    .getNode(e.getAttribute(attrId))
//                    .getConnections()
//                    .forEach(conn -> {
//                        IGraphArc arc = (IGraphArc) conn;
//                        arc.setElementDisabled(true);
//                        arc.getData().setDisabled(true);
//                        arc.getElementHandles().forEach(handle -> handle.setDisabled(true));
//                    });
//        }

        /**
         * Parameters.
         */
        nl = doc.getElementsByTagName(tagNodes);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) nl.item(0);
                nl = elem.getElementsByTagName(tagNode);
                for (int i = 0; i < nl.getLength(); i++) { // non-refering nodes
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        elem = (Element) nl.item(i);
                        addParameters(elem, dao);
                    }
                }
            }
        }

        /**
         * Validate transition functions, set parameter relations.
         */
        for (Transition transition : dao.getModel().getTransitions()) {
            for (Function functionElem : transition.getFunction().getElements()) {
                tmp = functionElem.getValue();
                if (idReferenceMap.containsKey(tmp)) {
                    functionElem.setValue(idReferenceMap.get(tmp));
                }
            }
            try {
                String functionString = transition.getFunction().toString();
                Function function = parameterService.validateAndGetFunction(dao.getModel(), transition, functionString);
                parameterService.setElementFunction(dao.getModel(), transition, function, null);
            } catch (ParameterException ex) {
                throw new IOException(ex);
            }
        }

        /**
         * Clustering.
         */
        elems = new ArrayList();
        nl = doc.getElementsByTagName(tagCluster);
        for (int i = 0; i < nl.getLength(); i++) { // non-refering nodes
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                elem = addCluster((Element) nl.item(i), dao);
                if (elem != null) {
                    elems.add(elem);
                }
            }
        }
        createRemainingClusters(elems, dao);

        return dao;
    }

    /**
     * Recursively creates clusters for all remaining elements.
     *
     * @param elements
     * @param dao
     * @throws IOException
     */
    private void createRemainingClusters(final List<Element> elements, ModelDao dao) throws IOException {

        List<Element> elementsRemaining = new ArrayList();

        for (Element element : elements) {

            element = addCluster(element, dao);
            if (element != null) {
                elementsRemaining.add(element);
            }
        }

        if (!elementsRemaining.isEmpty()) {
            createRemainingClusters(elementsRemaining, dao);
        }
    }

    /**
     * Creates a cluster containing a list of nodes. If any of the nodes is a
     * cluster itself and is not present in the graph yet, the element will be
     * returned and has to be added again later.
     *
     * @param elem
     * @param dao
     * @return the Element in case any node was missing, otherwise null
     * @throws IOException
     */
    private Element addCluster(final Element elem, ModelDao dao) throws IOException {

        NodeList nl;
        Element tmp;
        List<IGraphElement> childNodes = new ArrayList();
        IGraphCluster cluster;

        nl = elem.getElementsByTagName(tagClusterChild);
        for (int i = 0; i < nl.getLength(); i++) { // non-refering nodes
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(i);

                if (dao.getGraph().contains(tmp.getAttribute(attrId))) {
                    childNodes.add((IGraphNode) dao.getGraph().getNode(tmp.getAttribute(attrId)));
                } else {
                    return elem; // child node not available, repeat later
                }
            }
        }

        try {
            cluster = hierarchyService.cluster(dao, childNodes, elem.getAttribute(attrId));
            cluster.getData().setLabelText(elem.getAttribute(attrLabel));
            dao.getGraph().add(cluster);
        } catch (DataException ex) {
            throw new IOException("Cannot create cluster. " + ex.getMessage());
        }

        return null;
    }

    private void addParameters(final Element elem, ModelDao dao) throws IOException {

        final NodeList nodes = elem.getElementsByTagName(tagParameter);
        NodeList nl;

        DataTransition transition;
        String id, value, unit;
        Parameter param;

        if (nodes.getLength() > 0) {
            transition = (DataTransition) dao.getModel().getElement(elem.getAttribute(attrName));

            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

                    id = null;
                    value = null;
                    unit = null;

                    nl = nodes.item(i).getChildNodes();
                    for (int j = 0; j < nl.getLength(); j++) {
                        if (nl.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            final Element e = (Element) nl.item(j);
                            if (e.getNodeName().contentEquals(tagName)) {
                                id = e.getAttribute(attrNameParameter);
                            } else if (e.getNodeName().contentEquals(tagValue)) {
                                value = e.getAttribute(attrValue);
                            } else if (e.getNodeName().contentEquals(tagUnit)) {
                                unit = e.getAttribute(attrUnit);
                            }
                        }
                    }
                    param = parameterFactory.createLocalParameter(id, value, unit, transition);

                    /**
                     * If param id is same as an existing node, store param
                     * locally.
                     */
//                    if (transition.getLocalParameter(id) == null) {
//                        transition.getSortedLocalParameters().put(id, param);
//                    } else {
//                        throw new IOException("Parameter '" + param.getId() + "' already exists for element '" + transition.getId() + "'.");
//                    }
//                    if (dao.getModel().contains(id)) {
//                        if (transition.getLocalParameter(id) == null) {
//                            System.out.println("Parameter '" + param.getId() + "' already exists as a reference! Storing locally.");
//                            transition.addLocalParameter(param);
//                        } else {
//                            throw new IOException("Parameter '" + param.getId() + "' already exists as a reference and on local scale.");
//                        }
//                    } else {
//                        if (!dao.getModel().contains(param)) {
//                            dao.getModel().add(param);
//                        } else {
//                            if (!dao.getModel().getLocalParameter(id).getValue().contentEquals(value)) {
                    if (transition.getLocalParameter(id) == null) {
//                                    System.out.println("Parameter '" + param.getId() + "' already exists! Storing locally.");
                        transition.addLocalParameter(param);
                    } else {
                        throw new IOException("Parameter '" + param.getId() + "' already exists on local scale!");
//                                    throw new IOException("Parameter '" + param.getId() + "' already exists on global and local scale!");
                    }
//                            } else {
//                                System.out.println("Parameter '" + param.getId() + "' already exists but equals!");
//                            }
//                        }
//                    }
                }
            }
        }
    }

    /**
     *
     * @param elem
     * @param dao
     * @param addRefering
     * @return node elements that refer another node
     * @throws IOException
     */
    private Element addNode(final Element elem, ModelDao dao, boolean addRefering, Map<String, String> idReferenceMap) throws Exception {

        NodeList nl;
        Element tmp;
        String typeStrings[] = null;
        String dataId, nodeId, label = null;
        double posX = 0, posY = 0;
        boolean disabled, constant = false;

        edu.unibi.agbi.petrinet.entity.abstr.Element.Type type = null;
        IDataNode data = null;
        IGraphNode node;

        dataId = elem.getAttribute(attrName);
        nodeId = elem.getAttribute(attrId);

        nl = elem.getElementsByTagName(tagType);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                typeStrings = tmp.getAttribute(attrType).split(" ");
                type = edu.unibi.agbi.petrinet.entity.abstr.Element.Type.valueOf(typeStrings[1].toUpperCase());
            }
        }

        /**
         * Check for reference. If ref available, use associated data for new
         * shape.
         */
        nl = elem.getElementsByTagName(tagRefAvailable);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                if (Boolean.valueOf(tmp.getAttribute(attrRefAvailable))) { // has ref
                    nl = elem.getElementsByTagName(tagRefId);
                    if (nl.getLength() == 1) {
                        if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) { // get ref id
                            tmp = (Element) nl.item(0);
                            if (addRefering) {
                                if (dao.getGraph().contains("spec_" + tmp.getAttribute(attrRefId))) { // get data
                                    data = ((IGraphNode) dao.getGraph().getNode("spec_" + tmp.getAttribute(attrRefId))).getData();
                                    idReferenceMap.put(dataId, data.getId()); // set reference
                                } else {
                                    throw new IOException("Missing node reference '" + ("spec_" + tmp.getAttribute(attrRefId)) + "' for '" + dataId + "' ('" + nodeId + "')");
                                }
                            } else {
                                return elem;
                            }
                        }
                    }
                }
            }
        }

        if (type == null || typeStrings == null) {
            throw new IOException("Node type not specified for '" + dataId + "' ('" + nodeId + "').");
        }

        // Constant?
        nl = elem.getElementsByTagName(tagConstant);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                constant = Boolean.parseBoolean(tmp.getAttribute(attrConstant));
            }
        }

        /**
         * Create element and node.
         */
        switch (type) {

            case PLACE:
                if (data == null) {
                    data = getPlace(elem, dao, dataId, Place.Type.valueOf(typeStrings[0].toUpperCase()));
                    data.setConstant(constant);
                    try {
                        dao.getModel().add(data);
                    } catch (Exception ex) {
                        throw new IOException(ex.getMessage());
                    }
                }
                node = new GraphPlace(nodeId, (DataPlace) data);
                break;

            case TRANSITION:
                if (data == null) {
                    data = getTransition(elem, dao, dataId, Transition.Type.valueOf(typeStrings[0].toUpperCase()));
                    data.setConstant(constant);
                    try {
                        dao.getModel().add(data);
                    } catch (Exception ex) {
                        throw new IOException(ex.getMessage());
                    }
                }
                node = new GraphTransition(nodeId, (DataTransition) data);
                break;

            default:
                throw new IOException("Unhandled node type: '" + type + "'");
        }

        nl = elem.getElementsByTagName(tagDisabled);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                disabled = Boolean.valueOf(tmp.getAttribute(attrDisabled));
                if (disabled) {
                    data.setDisabled(disabled);
//                if (disabled || addRefering) {
//                    node.setElementDisabled(disabled);
                }
//                data.setDisabled(disabled);
//                node.getElementHandles().forEach(handle -> handle.setDisabled(disabled));
            }
        }

        dao.getGraphRoot().add(node);
        try {
            factoryService.StyleElement(node);
        } catch (DataException ex) {
            throw new IOException(ex);
        }

        if (addRefering) {
            if (!data.getLabelText().contentEquals("")) {
                label = data.getLabelText();
            }
        }
        if (label == null || label.contentEquals("")) {
            nl = elem.getElementsByTagName(tagLabel);
            if (nl.getLength() == 1) {
                if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                    tmp = (Element) nl.item(0);
                    label = tmp.getAttribute(attrLabel);
                }
            }
        }
        data.setLabelText(label);
        if (data.getType() == DataType.PLACE) {

            DataPlace place = (DataPlace) data;
            double tokenStart
                    = place.getTokens().iterator().next().getValueStart();

            if (tokenStart != 0) {
                place.setTokenLabelText(Double.toString(tokenStart));
            } else {
                place.setTokenLabelText("");
            }
        }

        nl = elem.getElementsByTagName(tagCoordinateX);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                posX = Double.valueOf(tmp.getAttribute(attrCoordinateX)) - node.getCenterOffsetX();
            }
        }
        nl = elem.getElementsByTagName(tagCoordinateY);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                posY = Double.valueOf(tmp.getAttribute(attrCoordinateY)) - node.getCenterOffsetY();
            }
        }

        Point2D pos = calculator.getPositionInGrid(new Point2D(posX, posY), dao.getGraphRoot());
        node.translateXProperty().set(pos.getX());
        node.translateYProperty().set(pos.getY());

        return null;
    }

    private DataPlace getPlace(final Element elem, ModelDao dao, String id, Place.Type type) throws Exception {

        NodeList nl;
        Element tmp;

        DataPlace place = new DataPlace(id, type);

        nl = elem.getElementsByTagName(tagConflictStrategy);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);

                switch (tmp.getAttribute(tagConflictStrategy)) {

                    case "0": // none (default = priority in 1.12.0)
                        place.setConflictResolutionType(ConflictResolutionStrategy.PRIORITY);
                        break;

                    case "1": // prio
                        place.setConflictResolutionType(ConflictResolutionStrategy.PRIORITY);
                        break;

                    case "2": // prob
                        place.setConflictResolutionType(ConflictResolutionStrategy.PROBABILITY);
                        break;

//                    case "3": // benefit
//                        place.setConflictResolutionType(Place.ConflictResolutionType.BENEFIT);
//                        break;
                    default:
                        throw new Exception("Unhandled conflict resolution strategy!");

                }
            }
        }

        place.addToken(getToken(elem));

        return place;
    }

    private DataTransition getTransition(final Element elem, ModelDao dao, String id, Transition.Type type) throws Exception {

        NodeList nl;
        Element tmp;
        String functionString;

        DataTransition transition = new DataTransition(id, type);

        nl = elem.getElementsByTagName(tagFunction);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                functionString = tmp.getAttribute(attrFunction);
                transition.setFunction(functionBuilder.build(functionString));
            }
        }

        return transition;
    }

    private void addConnection(final Element elem, ModelDao dao) throws Exception {

        NodeList nl;
        Element tmp;
        String tmpStr;

        DataArc arc;
        DataArc.Type type = null;
        IGraphNode source = null, target = null;
        IGraphArc connection;

        nl = elem.getElementsByTagName(tagConnectionSources);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                nl = tmp.getElementsByTagName(tagConnectionNode);
                if (nl.getLength() == 1) {
                    if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                        tmp = (Element) nl.item(0);
                        source = (IGraphNode) dao.getGraphRoot().getNode(tmp.getAttribute(attrConnectionNodeId));
                    }
                }
            }
        }

        nl = elem.getElementsByTagName(tagConnectionTargets);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                nl = tmp.getElementsByTagName(tagConnectionNode);
                if (nl.getLength() == 1) {
                    if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                        tmp = (Element) nl.item(0);
                        target = (IGraphNode) dao.getGraphRoot().getNode(tmp.getAttribute(attrConnectionNodeId));
                    }
                }
            }
        }

        nl = elem.getElementsByTagName(tagType);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                tmpStr = tmp.getAttribute(attrType);
                if (tmpStr.contentEquals("PN Edge")) {
                    type = DataArc.Type.NORMAL;
                } else {
                    throw new IOException("Unexcepted arc type '" + type + "'.");
                }
            }
        }

        if (source == null) {
            throw new IOException("Arc source cannot be found!");
        } else if (target == null) {
            throw new IOException("Arc target cannot be found!");
        } else if (type == null) {
            throw new IOException("Arc type was not specified!");
        }

        arc = getArc(elem, dao, source, target, type);

        connection = new GraphArc(
                factoryService.getConnectionId(source, target),
                source,
                target,
                arc
        );

        try {
            dao.getModel().add(arc);
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
        dao.getGraphRoot().add(connection);
        try {
            factoryService.StyleElement(connection);
        } catch (DataException ex) {
            throw new IOException(ex);
        }
    }

    private DataArc getArc(final Element elem, ModelDao dao, IGraphNode source, IGraphNode target, DataArc.Type type) throws Exception {

        NodeList nl;
        Element tmp;

        DataArc arc;
        DataPlace place;

        arc = new DataArc(
                factoryService.getArcId(source.getData(), target.getData()),
                source.getData(),
                target.getData(),
                type
        );

        if (source.getData().getType() == DataType.PLACE) {
            place = (DataPlace) source.getData();
        } else {
            place = (DataPlace) target.getData();
        }

        switch (place.getConflictResolutionType()) {

            case PRIORITY:

                nl = elem.getElementsByTagName(tagPriority);
                if (nl.getLength() == 1) {
                    if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                        tmp = (Element) nl.item(0);
                        arc.setConflictResolutionValue(
                                Double.parseDouble(tmp.getAttribute(tagPriority)));
                    }
                }

                break;

            case PROBABILITY:

                nl = elem.getElementsByTagName(tagProbability);
                if (nl.getLength() == 1) {
                    if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                        tmp = (Element) nl.item(0);
                        arc.setConflictResolutionValue(
                                Double.parseDouble(tmp.getAttribute(tagProbability)));
                    }
                }

                break;

//            case BENEFIT:
//                break;
                
            default:
                throw new IOException("Unhandled conflict resolution type!");

        }

        if (source.getData().isDisabled() || target.getData().isDisabled()) {
            arc.setDisabled(true);
        }
//        data.setName(elem.getAttribute(attrName));

        nl = elem.getElementsByTagName(tagWeight);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                arc.addWeight(getWeight(dao, (Element) nl.item(0)));
            }
        }

        return arc;
    }

    private Token getToken(final Element elem) {

        NodeList nl;
        Element tmp;

        Token token = new Token(factoryService.getColourDefault());

        nl = elem.getElementsByTagName(tagTokenStart);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                token.setValueStart(Double.parseDouble(tmp.getAttribute(attrTokenStart)));
            }
        }
        nl = elem.getElementsByTagName(tagTokenMin);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                token.setValueMin(Double.parseDouble(tmp.getAttribute(attrTokenMin)));
            }
        }
        nl = elem.getElementsByTagName(tagTokenMax);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(0);
                token.setValueMax(Double.parseDouble(tmp.getAttribute(attrTokenMax)));
            }
        }

        return token;
    }

    private Weight getWeight(ModelDao dao, Element elem) throws Exception {
        Weight weight = new Weight(factoryService.getColourDefault());
//        Weight weight = new Weight(dao.getModel().getColour(elem.getAttribute(attrColourId)));
        weight.setFunction(functionBuilder.build(elem.getAttribute(attrWeight)));
//        weight.setValue(elem.getAttribute(attrWeight));
        return weight;
    }
}
