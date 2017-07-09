/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.io;

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
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.ParameterService;
import edu.unibi.agbi.gravisfx.entity.IGravisCluster;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Model;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import edu.unibi.agbi.petrinet.util.FunctionBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
public class XmlModelConverter
{
    @Autowired private FunctionBuilder functionBuilder;
    @Autowired private DataService dataService;
    @Autowired private ParameterService parameterService;

    private final String formatDateTime = "yy-MM-dd HH:mm:ss";
    private final String dtdModelData = "model.dtd";

    private final String attrAuthor = "author";
    private final String attrColourId = "colourId";
    private final String attrCurrentClusterId = "currentClusterId";
    private final String attrCurrentNodeId = "currentNodeId";
    private final String attrCurrentPlaceId = "currentPlaceId";
    private final String attrCurrentTransitionId = "currentTransitionId";
    private final String attrCreationDateTime = "creationDateTime";
    private final String attrDataId = "dataId";
    private final String attrDescription = "description";
    private final String attrDisabled = "disabled";
    private final String attrElementId = "elementId";
    private final String attrId = "id";
    private final String attrLabel = "label";
    private final String attrMax = "max";
    private final String attrMin = "min";
    private final String attrName = "name";
    private final String attrUnit = "unit";
    private final String attrParameterId = "parameterId";
    private final String attrPosX = "posX";
    private final String attrPosY = "posY";
    private final String attrSource = "source";
    private final String attrStart = "start";
    private final String attrSticky = "sticky";
    private final String attrTarget = "target";
    private final String attrType = "type";
    private final String attrValue = "value";

    private final String tagArc = "Arc";
    private final String tagArcs = "Arcs";
    private final String tagCluster = "Cluster";
    private final String tagClusters = "Clusters";
    private final String tagClusterArc = "ClusterArc";
    private final String tagClusterArcs = "ClusterArcs";
    private final String tagColour = "Colour";
    private final String tagColours = "Colours";
    private final String tagConnection = "Connection";
    private final String tagConnections = "Connections";
    private final String tagFunction = "Function";
    private final String tagGraph = "Graph";
    private final String tagLabel = "Label";
    private final String tagModel = "Model";
    private final String tagNode = "Node";
    private final String tagNodes = "Nodes";
    private final String tagParameter = "Parameter";
    private final String tagParameters = "Parameters";
    private final String tagParametersLocal = "LocalParameters";
    private final String tagPlace = "Place";
    private final String tagPlaces = "Places";
    private final String tagRelatedParameter = "RelatedParameter";
    private final String tagRelatedParameters = "RelatedParameters";
    private final String tagToken = "Token";
    private final String tagTokens = "Tokens";
    private final String tagTransiton = "Transition";
    private final String tagTransitons = "Transitions";
    private final String tagWeights = "Weights";
    private final String tagWeight = "Weight";
    
    // <editor-fold defaultstate="collapsed" desc="File import and related methods">

    public DataDao importXml(File file) throws Exception {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
//        doc.getDocumentElement().normalize();

        NodeList n;
        Element root, elem, tmp;
        DataDao dao;

        /**
         * Model.
         */
        n = doc.getElementsByTagName(tagModel);
        if (n.getLength() == 1) {
            if (n.item(0).getNodeType() == Node.ELEMENT_NODE) {
                root = (Element) n.item(0);
                dao = getDataDao(root);
            } else {
                throw new Exception("File import failed. Malformed 'Model' element.");
            }
        } else {
            throw new Exception("File import failed. More than one or no 'Model' element.");
        }

        /**
         * Colours.
         */
        n = root.getElementsByTagName(tagColours);
        if (n.getLength() == 1) {
            if (n.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) n.item(0);
                n = elem.getElementsByTagName(tagColour);

                for (int i = 0; i < n.getLength(); i++) {
                    if (n.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        dao.getModel().add(getColour((Element) n.item(i)));
                    }
                }
            }
        }

        /**
         * Places.
         */
        n = root.getElementsByTagName(tagPlaces);
        if (n.getLength() == 1) {
            if (n.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) n.item(0);
                n = elem.getElementsByTagName(tagPlace);

                // Each place
                for (int i = 0; i < n.getLength(); i++) {
                    if (n.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        DataPlace place = getPlace((Element) n.item(i));
                        dao.getModel().add(place);
                        for (IGraphElement shape : place.getShapes()) {
                            dao.getGraphRoot().add((IGraphNode) shape);
                        }
                    }
                }
            }
        }

        /**
         * Transitions.
         */
        n = root.getElementsByTagName(tagTransitons);
        if (n.getLength() == 1) {
            if (n.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) n.item(0);
                n = elem.getElementsByTagName(tagTransiton);

                // Each transition
                for (int i = 0; i < n.getLength(); i++) {
                    if (n.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        DataTransition transition = getTransition((Element) n.item(i));
                        dao.getModel().add(transition);
                        for (IGraphElement shape : transition.getShapes()) {
                            dao.getGraphRoot().add((IGraphNode) shape);
                        }
                    }
                }
            }
        }

        /**
         * Arcs.
         */
        n = root.getElementsByTagName(tagArcs);
        if (n.getLength() == 1) {
            if (n.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) n.item(0);
                n = elem.getElementsByTagName(tagArc);

                // Each arc
                for (int i = 0; i < n.getLength(); i++) {
                    if (n.item(i).getNodeType() == Node.ELEMENT_NODE) {

                        elem = (Element) n.item(i);

                        DataArc arc = getArc(
                                elem,
                                (IDataNode) dao.getModel().getElement(elem.getAttribute(attrSource)),
                                (IDataNode) dao.getModel().getElement(elem.getAttribute(attrTarget)),
                                dao
                        );
                        dao.getModel().add(arc);
                        for (IGraphElement shape : arc.getShapes()) {
                            dao.getGraphRoot().add((IGraphArc) shape);
                        }
                    }
                }
            }
        }

        /**
         * Parameters.
         */
        n = root.getElementsByTagName(tagParameters);
        if (n.getLength() == 1) {
            if (n.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) n.item(0);
                n = elem.getElementsByTagName(tagParameter);

                for (int i = 0; i < n.getLength(); i++) {
                    if (n.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        tmp = (Element) n.item(i);
                        dao.getModel().add(getParameter(tmp, dao.getModel().getElement(tmp.getAttribute(attrElementId))));
                    }
                }
            }
        }
        for (Transition transition : dao.getModel().getTransitions()) {
            try {
                parameterService.ValidateFunction(dao.getModel(), transition, transition.getFunction().toString());
            } catch (ParameterServiceException ex) {
                throw new IOException(ex);
            }
            for (String paramId : transition.getFunction().getParameterIds()) {
                if (transition.getParameter(paramId) != null) {
                    transition.getParameter(paramId).getUsingElements().add(transition);
                } else if (dao.getModel().getParameter(paramId) != null) {
                    dao.getModel().getParameter(paramId).getUsingElements().add(transition);
                } else {
                    throw new IOException("Unavailable parameter '" + paramId + "' requested by node '" + transition.toString()+ "'.");
                }
            }
        }
        
        /**
         * Graph root.
         */
        n = root.getElementsByTagName(tagGraph);
        if (n.getLength() >= 1) {
            if (n.item(0).getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) n.item(0);
                Map nodeMaps = new HashMap();
                nodeMaps = fillGraph(elem, dao.getModel(), dao.getGraphRoot(), nodeMaps);
                fillGraphConnections(elem, dao.getModel(), dao.getGraphRoot(), nodeMaps);
            }
        }

        return dao;
    }

    private DataDao getDataDao(Element elem) {
        DataDao dao = new DataDao();
        dao.setAuthor(elem.getAttribute(attrAuthor));
        dao.setCreationDateTime(LocalDateTime.parse(elem.getAttribute(attrCreationDateTime), DateTimeFormatter.ofPattern(formatDateTime)));
        dao.setModelDescription(elem.getAttribute(attrDescription));
        dao.setDaoId(elem.getAttribute(attrId));
        dao.setModelName(elem.getAttribute(attrName));
        dao.setNextClusterId(Integer.parseInt(elem.getAttribute(attrCurrentNodeId)));
        dao.setNextNodeId(Integer.parseInt(elem.getAttribute(attrCurrentClusterId)));
        dao.setNextPlaceId(Integer.parseInt(elem.getAttribute(attrCurrentPlaceId)));
        dao.setNextTransitionId(Integer.parseInt(elem.getAttribute(attrCurrentTransitionId)));
        return dao;
    }
    
    private Map fillGraph(Element elem, Model model, Graph graph, Map<String, IGraphNode> nodes) throws IOException {
        
        List<IGraphArc> arcs;
        NodeList nl;
        Element tmp, nodesElement = null, connectionsElement = null, clustersElement = null;
        
        nl = elem.getChildNodes();
        
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(i);
                
                if (tmp.getNodeName().contentEquals(tagNodes)) {
                    nodesElement = tmp;
                } else if (tmp.getNodeName().contentEquals(tagConnections)) {
                    connectionsElement = tmp;
                } else if (tmp.getNodeName().contentEquals(tagClusters)) {
                    clustersElement = tmp;
                }
            }
        }

        // Nodes
        if (nodesElement != null) {
            addNodes(nodesElement.getElementsByTagName(tagNode), model, graph, nodes);
        }

        // Connections
        if (connectionsElement != null) {
            arcs = getConnections(connectionsElement.getElementsByTagName(tagConnection), model, nodes);
            arcs.forEach(arc -> graph.add(arc));
        }

        // Clusters
        if (clustersElement != null) {
            addClusters(clustersElement.getChildNodes(), model, graph, nodes);
        }
        
        return nodes;
    }
    
    private void addClusters(final NodeList nl, Model model, Graph graph, Map<String,IGraphNode> nodes) throws IOException {
        
        IGraphCluster cluster;
        Graph childGraph;
        NodeList inl;
        Element tmp, graphElement = null;
        
        // Iterate all clusters
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                
                final Element elem = (Element) nl.item(i);
                if (elem.getNodeName().contentEquals(tagCluster)) {
                    inl = elem.getChildNodes();
                    cluster = (IGraphCluster) graph.getNode(elem.getAttribute(attrId));
                    
                    for (int j = 0; j < inl.getLength(); j++) {
                        if (inl.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            tmp = (Element) inl.item(j);
                            if (tmp.getNodeName().contentEquals(tagGraph)) {
                                graphElement = tmp;
                                break;
                            }
                        }
                    }

                    // Get child graph
                    if (graphElement != null) {
                        childGraph = cluster.getGraph();
                        childGraph.nameProperty().set(cluster.getId());
                        childGraph.setParentGraph(graph);
                        fillGraph(graphElement, model, childGraph, nodes);
                    }
                }
            }
        }
    }
    
    private Graph fillGraphConnections(Element elem, Model model, Graph graph, Map<String, IGraphNode> nodes) throws IOException {
        
        NodeList nl;
        Element tmp;
        
        nl = elem.getChildNodes();
        
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                tmp = (Element) nl.item(i);
                if (tmp.getNodeName().contentEquals(tagClusters)) {
                    addClusterArcs(tmp.getChildNodes(), model, graph, nodes);
                    break;
                }
            }
        }
        
        return graph;
    }
    
    private void addClusterArcs(final NodeList nl, Model model, Graph graph, Map<String,IGraphNode> nodes) throws IOException {
        
        IGraphCluster cluster;
        Graph childGraph;
        NodeList inl;
        Element tmp, graphElement = null, clusterArcElement = null;
        
        // Iterate all clusters
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                
                final Element elem = (Element) nl.item(i);
                if (elem.getNodeName().contentEquals(tagCluster)) {
                    inl = elem.getChildNodes();
                    cluster = (IGraphCluster) graph.getNode(elem.getAttribute(attrId));
                    
                    for (int j = 0; j < inl.getLength(); j++) {
                        if (inl.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            tmp = (Element) inl.item(j);
                            if (tmp.getNodeName().contentEquals(tagGraph)) {
                                graphElement = tmp;
                            } else if (tmp.getNodeName().contentEquals(tagClusterArcs)) {
                                clusterArcElement = tmp;
                            }
                        }
                    }

                    // Get child graph
                    if (graphElement != null) {
                        childGraph = cluster.getGraph();
                        fillGraphConnections(graphElement, model, childGraph, nodes);
                    }

                    // Create cluster arcs
                    if (clusterArcElement != null) {
                        addClusterConnections(clusterArcElement.getElementsByTagName(tagClusterArc), model, graph, nodes);
                    }
                }
            }
        }
    }
    
    private void addNodes(NodeList nl, Model model, Graph graph, Map<String,IGraphNode> nodes) throws IOException {
        
        IElement data;
        Element elem;
        
        try {
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    elem = (Element) nl.item(i);

                    data = model.getElement(elem.getAttribute(attrDataId));
                    if (data == null) {
                        data = new DataCluster(elem.getAttribute(attrDataId));
                    }
                    IGraphNode shape;
                    switch (data.getElementType()) {
                        case CLUSTER:
                            shape = new GraphCluster(elem.getAttribute(attrId), (DataCluster) data);
                            break;
                        case PLACE:
                            shape = new GraphPlace(elem.getAttribute(attrId), (DataPlace) data);
                            break;
                        case TRANSITION:
                            shape = new GraphTransition(elem.getAttribute(attrId), (DataTransition) data);
                            break;
                        default:
                            throw new IOException("Malformed node detected! Cannot create node shape for data type " + data.getElementType() + "!");
                    }
                    shape.getLabel().setText(elem.getAttribute(attrLabel));
                    graph.add(shape);
                    dataService.styleElement(shape);
                    shape.getElementHandles().forEach(handle -> handle.setDisabled(shape.getDataElement().isDisabled()));
                    setLabelAndPosition(elem, shape);
                    nodes.put(shape.getId(), shape);
                }
            }
        } catch (DataServiceException ex) {
            throw new IOException(ex);
        }
    }
    
    private void addClusterConnections(final NodeList nl, Model model, Graph graph, Map<String, IGraphNode> nodes) throws IOException {
        
        IGraphArc arc;
        DataClusterArc data;
        Element elem;
        
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) nl.item(i);
                
                arc = (IGraphArc) graph.getConnection(elem.getAttribute(attrId));
                data = (DataClusterArc) arc.getDataElement();
                
                arc.getDataElement().getShapes().add(arc);

                for (IGraphArc a : getConnections(elem.getElementsByTagName(tagConnection), model, nodes)) {
                    data.getStoredArcs().put(a.getId(), a);
                    a.getDataElement().getShapes().clear();
                    a.getDataElement().getShapes().add(arc);
                }
                graph.add(arc);
            }
        }
    }
    
    private List<IGraphArc> getConnections(NodeList nl, Model model, Map<String, IGraphNode> nodes) throws IOException {
        
        IGraphNode source, target;
        DataArc data;
        Element elem;
        List<IGraphArc> arcs = new ArrayList();
        
        try {
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    elem = (Element) nl.item(i);

                    source = nodes.get(elem.getAttribute(attrSource));
                    target = nodes.get(elem.getAttribute(attrTarget));
                    
                    data = (DataArc) model.getElement(elem.getAttribute(attrDataId));
                    if (data == null) {
                        data = new DataClusterArc(elem.getAttribute(attrDataId), source.getDataElement(), target.getDataElement());
                    }

                    IGraphArc arc = new GraphArc(elem.getAttribute(attrId), source, target, data);
                    arc.getDataElement().getShapes().add(arc);
                    dataService.styleElement(arc);
                    arc.getElementHandles().forEach(handle -> handle.setDisabled(arc.getDataElement().isDisabled()));
                    arcs.add(arc);
                }
            }
        } catch (DataServiceException ex) {
            throw new IOException(ex);
        }
        return arcs;
    }

    private DataArc getArc(final Element elem, IDataNode source, IDataNode target, DataDao dataDao) {

        NodeList nl;
        Element tmp;

        DataArc arc = new DataArc(
                elem.getAttribute(attrId),
                source, target,
                DataArc.Type.valueOf(elem.getAttribute(attrType))
        );
//        setRelatedParameterIds(elem, arc);

        // Weights
        nl = elem.getElementsByTagName(tagWeights);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {

                tmp = (Element) nl.item(0);
                nl = tmp.getElementsByTagName(tagWeight);

                for (int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        arc.addWeight(getWeight((Element) nl.item(i), dataDao));
                    }
                }
            }
        }
        if (elem.getAttribute(attrDisabled) != null) {
            arc.setDisabled(Boolean.valueOf(elem.getAttribute(attrDisabled)));
        }
        arc.setDescription(elem.getAttribute(attrDescription));
        arc.setName(elem.getAttribute(attrName));

        return arc;
    }

    private DataPlace getPlace(final Element elem) {

        NodeList nodes;
        Element tmp;

        DataPlace place = new DataPlace(
                elem.getAttribute(attrId),
                DataPlace.Type.valueOf(elem.getAttribute(attrType))
        );
//        setRelatedParameterIds(elem, place);

        // Token
        nodes = elem.getElementsByTagName(tagTokens);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                tmp = (Element) nodes.item(0);
                nodes = tmp.getElementsByTagName(tagToken);

                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        place.addToken(getToken((Element) nodes.item(i)));
                    }
                }
            }
        }
        if (elem.getAttribute(attrDisabled) != null) {
            place.setDisabled(Boolean.valueOf(elem.getAttribute(attrDisabled)));
        }
        if (elem.getAttribute(attrSticky) != null) {
            place.setSticky(Boolean.valueOf(elem.getAttribute(attrSticky)));
        }
        place.setDescription(elem.getAttribute(attrDescription));
        place.setName(elem.getAttribute(attrName));

        return place;
    }

    private DataTransition getTransition(final Element elem) throws Exception {
        
        NodeList nl;
        Element tmp;
        DataTransition transition;

        transition = new DataTransition(
                elem.getAttribute(attrId),
                DataTransition.Type.valueOf(elem.getAttribute(attrType))
        );
//        setRelatedParameterIds(elem, transition);
        
        nl = elem.getElementsByTagName(tagParametersLocal);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {

                tmp = (Element) nl.item(0);
                nl = tmp.getElementsByTagName(tagParameter);

                for (int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        transition.addParameter(getParameter((Element) nl.item(i), transition));
                    }
                }
            }
        }

        if (elem.getAttribute(attrDisabled) != null) {
            transition.setDisabled(Boolean.valueOf(elem.getAttribute(attrDisabled)));
        }
        if (elem.getAttribute(attrSticky) != null) {
            transition.setSticky(Boolean.valueOf(elem.getAttribute(attrSticky)));
        }
        transition.setFunction(getFunction(elem));
        transition.setDescription(elem.getAttribute(attrDescription));
        transition.setName(elem.getAttribute(attrName));

        return transition;
    }

    private Colour getColour(Element elem) {
        Colour colour = new Colour(
                elem.getAttribute(attrId),
                elem.getAttribute(attrDescription)
        );
        return colour;
    }

    private Function getFunction(Element elem) throws Exception {
        NodeList nodes = elem.getElementsByTagName(tagFunction);
        if (nodes.getLength() > 0) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) nodes.item(0);
                return functionBuilder.build(elem.getTextContent(), false);
            }
        }
        return functionBuilder.build("1", false);
    }

    private IGraphNode setLabelAndPosition(Element elem, IGraphNode node) {
        node.setId(elem.getAttribute(attrId));
        node.translateXProperty().set(Double.parseDouble(elem.getAttribute(attrPosX)));
        node.translateYProperty().set(Double.parseDouble(elem.getAttribute(attrPosY)));
        if (elem.getElementsByTagName(tagLabel).getLength() == 1) {
            if (elem.getElementsByTagName(tagLabel).item(0).getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) elem.getElementsByTagName(tagLabel).item(0);
                node.getDataElement().setLabelText(elem.getAttribute(attrLabel));
                node.getLabel().setTranslateX(Double.parseDouble(elem.getAttribute(attrPosX)));
                node.getLabel().setTranslateY(Double.parseDouble(elem.getAttribute(attrPosY)));
            }
        }
        return node;
    }

    private Parameter getParameter(Element elem, IElement element) {
        Parameter param = new Parameter(
                elem.getAttribute(attrId),
                elem.getTextContent(),
                elem.getAttribute(attrUnit),
                Parameter.Type.valueOf(elem.getAttribute(attrType).toUpperCase()),
                element
        );
        return param;
    }

    private Token getToken(Element elem) {
        Token token = new Token(new Colour(elem.getAttribute(attrColourId), ""));
        token.setValueStart(Double.parseDouble(elem.getAttribute(attrStart)));
        token.setValueMin(Double.parseDouble(elem.getAttribute(attrMin)));
        token.setValueMax(Double.parseDouble(elem.getAttribute(attrMax)));
        return token;
    }

    private Weight getWeight(Element elem, DataDao dataDao) {
        Weight weight = new Weight(new Colour(elem.getAttribute(attrColourId), ""));
        weight.setValue(elem.getAttribute(attrValue));
        return weight;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="File export and related methods">

    public void exportXml(File file, DataDao dataDao) throws ParserConfigurationException, TransformerException, FileNotFoundException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.newDocument();
        
        Element model;
        model = dom.createElement(tagModel); // create the root element
        model.setAttribute(attrAuthor, dataDao.getAuthor());
        model.setAttribute(attrCreationDateTime, dataDao.getCreationDateTime().format(DateTimeFormatter.ofPattern(formatDateTime)));
        model.setAttribute(attrDescription, dataDao.getModelDescription());
        model.setAttribute(attrId, dataDao.getDaoId());
        model.setAttribute(attrName, dataDao.getModelName());
        model.setAttribute(attrCurrentNodeId, Integer.toString(dataDao.getNextNodeId()));
        model.setAttribute(attrCurrentClusterId, Integer.toString(dataDao.getNextClusterId()));
        model.setAttribute(attrCurrentPlaceId, Integer.toString(dataDao.getNextPlaceId()));
        model.setAttribute(attrCurrentTransitionId, Integer.toString(dataDao.getNextTransitionId()));
        dataDao.setNextClusterId(dataDao.getNextClusterId() - 2); // revoke iteration
        dataDao.setNextNodeId(dataDao.getNextNodeId() - 2);
        dataDao.setNextPlaceId(dataDao.getNextPlaceId() - 2);
        dataDao.setNextTransitionId(dataDao.getNextTransitionId() - 2);
        model.appendChild(getArcsElement(dom, dataDao.getModel().getArcs()));
        model.appendChild(getPlacesElement(dom, dataDao.getModel().getPlaces()));
        model.appendChild(getTransitionsElement(dom, dataDao.getModel().getTransitions()));
        model.appendChild(getColorsElement(dom, dataDao.getModel().getColours()));
        model.appendChild(getGraphElement(dom, dataDao.getGraphRoot()));
            
        Element params = dom.createElement(tagParameters);
        dataDao.getModel().getParameters().forEach(param -> {
            params.appendChild(getParameterElement(dom, param));
        });
        model.appendChild(params);

        dom.appendChild(model);
        dom.normalize();

        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//        tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dtdModelData);
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // send DOM to file
        tr.transform(
                new DOMSource(dom),
                new StreamResult(new FileOutputStream(file))
        );
    }
    
    private Element getArcsElement(Document dom, Collection arcs) {
        Element elements = dom.createElement(tagArcs);
        arcs.forEach(arc -> {
            DataArc data = (DataArc) arc;
            
            Element a = dom.createElement(tagArc);
            a.setAttribute(attrId, data.getId());
            a.setAttribute(attrType, data.getArcType().toString());
            a.setAttribute(attrSource, data.getSource().getId());
            a.setAttribute(attrTarget, data.getTarget().getId());
            if (data.isDisabled()) {
                a.setAttribute(attrDisabled, Boolean.toString(data.isDisabled()));
            }
            if (data.getName() != null && !data.getName().isEmpty()) {
                a.setAttribute(attrName, data.getName());
            }
            if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                a.setAttribute(attrDescription, data.getDescription());
            }
            a.appendChild(getRelatedParameterIdsElement(dom, data));
            a.appendChild(getWeightsElement(dom, data));

            elements.appendChild(a);
        });
        return elements;
    }
    
    public Element getPlacesElement(Document dom, Collection places) {
        Element elements = dom.createElement(tagPlaces);
        places.forEach(place -> {
            DataPlace data = (DataPlace) place;
            
            Element p = dom.createElement(tagPlace);
            p.setAttribute(attrId, data.getId());
            p.setAttribute(attrType, data.getPlaceType().toString());
            if (data.isDisabled()) {
                p.setAttribute(attrDisabled, Boolean.toString(data.isDisabled()));
            }
            if (data.isSticky()) {
                p.setAttribute(attrSticky, Boolean.toString(data.isSticky()));
            }
            if (data.getName() != null && !data.getName().isEmpty()) {
                p.setAttribute(attrName, data.getName());
            }
            if (data.getLabelText() != null && !data.getLabelText().isEmpty()) {
                p.setAttribute(attrLabel, data.getLabelText());
            }
            if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                p.setAttribute(attrDescription, data.getDescription());
            }
            p.appendChild(getRelatedParameterIdsElement(dom, data));
            p.appendChild(getTokensElement(dom, data));

            elements.appendChild(p);
        });
        return elements;
    }
    
    private Element getTransitionsElement(Document dom, Collection transitions) {
        Element elements = dom.createElement(tagTransitons);
        transitions.forEach(transition -> {
            DataTransition data = (DataTransition) transition;
            
            Element t = dom.createElement(tagTransiton);
            t.setAttribute(attrId, data.getId());
            t.setAttribute(attrType, data.getTransitionType().toString());
            if (data.isDisabled()) {
                t.setAttribute(attrDisabled, Boolean.toString(data.isDisabled()));
            }
            if (data.isSticky()) {
                t.setAttribute(attrSticky, Boolean.toString(data.isSticky()));
            }
            if (data.getName() != null && !data.getName().isEmpty()) {
                t.setAttribute(attrName, data.getName());
            }
            if (data.getLabelText() != null && !data.getLabelText().isEmpty()) {
                t.setAttribute(attrLabel, data.getLabelText());
            }
            if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                t.setAttribute(attrDescription, data.getDescription());
            }
            t.appendChild(getRelatedParameterIdsElement(dom, data));
            
            Element p = dom.createElement(tagParametersLocal);
            data.getParameters().forEach(param -> {
                p.appendChild(getParameterElement(dom, param));
            });
            t.appendChild(p);

            Element f = dom.createElement(tagFunction);
            if (!data.getFunction().getUnit().isEmpty()) {
                f.setAttribute(attrUnit, data.getFunction().getUnit());
            }
            f.setTextContent(data.getFunction().toString());
            t.appendChild(f);

            elements.appendChild(t);
        });
        return elements;
    }
    
    private Element getColorsElement(Document dom, Collection<Colour> colors) {
        Element elements = dom.createElement(tagColours);
        colors.forEach(colour -> {
            Element c = dom.createElement(tagColour);
            c.setAttribute(attrId, colour.getId());
            if (colour.getDescription() != null && !colour.getDescription().isEmpty()) {
                c.setAttribute(attrDescription, colour.getDescription());
            }
            elements.appendChild(c);
        });
        return elements;
    }
    
    private Element getParameterElement(Document dom, Parameter param) {
        Element p = dom.createElement(tagParameter);
        p.setAttribute(attrId, param.getId());
        p.setTextContent(param.getValue());
        if (param.getUnit() != null && !param.getUnit().isEmpty()) {
            p.setAttribute(attrUnit, param.getUnit());
        }
        p.setAttribute(attrType, param.getType().toString());
        p.setAttribute(attrElementId, param.getRelatedElement().getId());
        return p;
    }
    
    private Element getGraphElement(Document dom, Graph graph) {
        Element elements = dom.createElement(tagGraph);
        elements.appendChild(getNodesElement(dom, graph.getNodes()));
        elements.appendChild(getConnectionsElement(dom, graph.getConnections()));
        elements.appendChild(getClustersElement(dom, graph.getClusters()));
        return elements;
    }
    
    private Element getClustersElement(Document dom, Collection<IGravisCluster> clusters) {
        Element elements = dom.createElement(tagClusters);
        clusters.forEach(cluster -> {
            
            Element c = dom.createElement(tagCluster);
            c.setAttribute(attrId, cluster.getId());
            c.appendChild(getClusterArcsElement(dom, cluster.getConnections()));
            c.appendChild(getGraphElement(dom, cluster.getGraph()));
            
            elements.appendChild(c);
        });
        return elements;
    }
    
    private Element getClusterArcsElement(Document dom, Collection<IGravisConnection> connections) {
        
        Element elements = dom.createElement(tagClusterArcs);
        connections.forEach(clusterArc -> {
            DataClusterArc data = (DataClusterArc) ((IGraphArc) clusterArc).getDataElement();
            
            Element a = dom.createElement(tagClusterArc);
            a.setAttribute(attrId, clusterArc.getId());
            
            data.getStoredArcs().values().forEach(storedArc -> {
                Element sa = dom.createElement(tagConnection);
                sa.setAttribute(attrId, storedArc.getId());
                sa.setAttribute(attrDataId, ((IGraphArc) storedArc).getDataElement().getId());
                sa.setAttribute(attrSource, String.valueOf(storedArc.getSource().getId()));
                sa.setAttribute(attrTarget, String.valueOf(storedArc.getTarget().getId()));
                
                a.appendChild(sa);
            });
            elements.appendChild(a);
        });
        return elements;
    }
    
    private Element getConnectionsElement(Document dom, Collection<IGravisConnection> connections) {
        Element elements = dom.createElement(tagConnections);
        connections.forEach(connection -> {
            
            Element c = dom.createElement(tagConnection);
            c.setAttribute(attrId, connection.getId());
            c.setAttribute(attrDataId, ((IGraphArc) connection).getDataElement().getId());
            c.setAttribute(attrSource, String.valueOf(connection.getSource().getId()));
            c.setAttribute(attrTarget, String.valueOf(connection.getTarget().getId()));
            
            elements.appendChild(c);
        });
        return elements;
    }
    
    private Element getNodesElement(Document dom, Collection<IGravisNode> nodes) {
        Element elements = dom.createElement(tagNodes);
        nodes.forEach(node -> {
            
            Element n = dom.createElement(tagNode);
            n.setAttribute(attrId, node.getId());
            n.setAttribute(attrDataId, ((IGraphNode) node).getDataElement().getId());
            n.setAttribute(attrPosX, String.valueOf(node.getShape().getTranslateX()));
            n.setAttribute(attrPosY, String.valueOf(node.getShape().getTranslateY()));
            
            Element l = dom.createElement(tagLabel);
            l.setAttribute(attrLabel, node.getLabel().getText());
            l.setAttribute(attrPosX, String.valueOf(node.getLabel().getTranslateX()));
            l.setAttribute(attrPosY, String.valueOf(node.getLabel().getShape().getTranslateY()));
            l.setTextContent(node.getLabel().getText());
            n.appendChild(l);
            
            elements.appendChild(n);
        });
        return elements;
    }
    
    private Element getRelatedParameterIdsElement(Document dom, IDataElement data) {
        Element elem = dom.createElement(tagRelatedParameters);
        data.getRelatedParameterIds().forEach(id -> {
            Element paramElem = dom.createElement(tagRelatedParameter);
            paramElem.setAttribute(attrParameterId, id);
            elem.appendChild(paramElem);
        });
        return elem;
    }
    
    private Element getTokensElement(Document dom, DataPlace data) {
        Element elements = dom.createElement(tagTokens);
        data.getTokens().forEach(token -> {
            Element t = dom.createElement(tagToken);
            t.setAttribute(attrColourId, token.getColour().getId());
            t.setAttribute(attrStart, String.valueOf(token.getValueStart()));
            t.setAttribute(attrMin, String.valueOf(token.getValueMin()));
            t.setAttribute(attrMax, String.valueOf(token.getValueMax()));
            elements.appendChild(t);
        });
        return elements;
    }
    
    private Element getWeightsElement(Document dom, IDataArc data) {
        Element weights = dom.createElement(tagWeights);
        data.getWeights().forEach(weight -> {
            Element w = dom.createElement(tagWeight);
            w.setAttribute(attrColourId, weight.getColour().getId());
            w.setAttribute(attrValue, weight.getValue());
            weights.appendChild(w);
        });
        return weights;
    }
    
    // </editor-fold>
}
