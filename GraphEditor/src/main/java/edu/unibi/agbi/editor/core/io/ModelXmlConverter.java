/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.io;

import edu.unibi.agbi.editor.core.data.dao.ModelDao;
import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.core.data.entity.data.IDataArc;
import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataClusterArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataPlace;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataTransition;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphCluster;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.HierarchyService;
import edu.unibi.agbi.editor.business.service.ParameterService;
import edu.unibi.agbi.gravisfx.entity.root.node.IGravisCluster;
import edu.unibi.agbi.gravisfx.entity.root.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
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
import java.util.List;
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
public class ModelXmlConverter
{
    @Autowired private FunctionBuilder functionBuilder;
    @Autowired private ModelService dataService;
    @Autowired private HierarchyService hierarchyService;
    @Autowired private ParameterService parameterService;

    private final String formatDateTime = "yy-MM-dd HH:mm:ss";
    private final String dtdModelData = "model.dtd";

    private final String attrAuthor = "author";
    private final String attrColourId = "colourId";
    private final String attrConstant = "constant";
    private final String attrCurrentClusterId = "currentClusterId";
    private final String attrCurrentNodeId = "currentNodeId";
    private final String attrCurrentPlaceId = "currentPlaceId";
    private final String attrCurrentTransitionId = "currentTransitionId";
    private final String attrCreationDateTime = "creationDateTime";
    private final String attrDescription = "description";
    private final String attrDisabled = "disabled";
    private final String attrElementId = "elementId";
    private final String attrId = "id";
    private final String attrLabel = "label";
    private final String attrMax = "max";
    private final String attrMin = "min";
    private final String attrName = "name";
    private final String attrUnit = "unit";
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
    private final String tagColour = "Colour";
    private final String tagColours = "Colours";
    private final String tagConnection = "Connection";
    private final String tagFunction = "Function";
    private final String tagGraph = "Graph";
    private final String tagLabel = "Label";
    private final String tagModel = "Model";
    private final String tagNode = "Node";
    private final String tagNodes = "Nodes";
    private final String tagNodeShapes = "Nodes";
    private final String tagParameter = "Parameter";
    private final String tagParameters = "Parameters";
    private final String tagParametersLocal = "LocalParameters";
    private final String tagPlace = "Place";
    private final String tagPlaces = "Places";
    private final String tagShapes = "Shapes";
    private final String tagToken = "Token";
    private final String tagTokens = "Tokens";
    private final String tagTransiton = "Transition";
    private final String tagTransitons = "Transitions";
    private final String tagWeight = "Weight";
    private final String tagWeights = "Weights";
    
    // <editor-fold defaultstate="collapsed" desc="File import and related methods">

    public ModelDao importXml(File file) throws Exception {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
//        doc.getDocumentElement().normalize();

        NodeList nl;
        Element root, elem, tmp;
        ModelDao dao;

        /**
         * Model.
         */
        nl = doc.getElementsByTagName(tagModel);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                root = (Element) nl.item(0);
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
        nl = root.getElementsByTagName(tagColours);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nl.item(0);
                nl = elem.getElementsByTagName(tagColour);

                for (int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        dao.getModel().add(getColour((Element) nl.item(i)));
                    }
                }
            }
        }

        /**
         * Places.
         */
        nl = root.getElementsByTagName(tagPlaces);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nl.item(0);
                nl = elem.getElementsByTagName(tagPlace);

                // Each place
                for (int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        addPlace(dao, (Element) nl.item(i));
                    }
                }
            }
        }

        /**
         * Transitions.
         */
        nl = root.getElementsByTagName(tagTransitons);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nl.item(0);
                nl = elem.getElementsByTagName(tagTransiton);

                // Each transition
                for (int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        addTransition(dao, (Element) nl.item(i));
                    }
                }
            }
        }

        /**
         * Arcs.
         */
        nl = root.getElementsByTagName(tagArcs);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nl.item(0);
                nl = elem.getElementsByTagName(tagArc);

                // Each arc
                for (int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        addArc(dao, (Element) nl.item(i));
                    }
                }
            }
        }

        /**
         * Parameters.
         */
        nl = root.getElementsByTagName(tagParameters);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nl.item(0);
                nl = elem.getElementsByTagName(tagParameter);

                for (int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        tmp = (Element) nl.item(i);
                        parameterService.add(dao.getModel(), getParameter(tmp, dao.getModel().getElement(tmp.getAttribute(attrElementId))));
                    }
                }
            }
        }
        for (Transition transition : dao.getModel().getTransitions()) {
            String functionString = transition.getFunction().toString();
            parameterService.ValidateFunction(dao.getModel(), transition, functionString);
            parameterService.setFunction(dao.getModel(), transition, functionString, null);
        }
        for (Arc arc : dao.getModel().getArcs()) {
            for (Weight weight : arc.getWeights()) {
                String functionString = weight.getFunction().toString();
                parameterService.ValidateFunction(dao.getModel(), arc, functionString);
                parameterService.setFunction(dao.getModel(), arc, functionString, weight.getColour());
            }
        }
        
        /**
         * Graph.
         */
        nl = root.getElementsByTagName(tagGraph);
        if (nl.getLength() >= 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                addGraph(dao, null, (Element) nl.item(0));
            }
        }

        return dao;
    }

    private ModelDao getDataDao(Element elem) {
        ModelDao dao = new ModelDao();
        dao.setAuthor(elem.getAttribute(attrAuthor));
        dao.setCreationDateTime(LocalDateTime.parse(elem.getAttribute(attrCreationDateTime), DateTimeFormatter.ofPattern(formatDateTime)));
        dao.setModelDescription(elem.getAttribute(attrDescription));
        dao.setModelId(elem.getAttribute(attrId));
        dao.setModelName(elem.getAttribute(attrName));
        dao.setNextClusterId(Integer.parseInt(elem.getAttribute(attrCurrentClusterId)));
        dao.setNextNodeId(Integer.parseInt(elem.getAttribute(attrCurrentNodeId)));
        dao.setNextPlaceId(Integer.parseInt(elem.getAttribute(attrCurrentPlaceId)));
        dao.setNextTransitionId(Integer.parseInt(elem.getAttribute(attrCurrentTransitionId)));
        return dao;
    }
    
    /**
     * 
     * @param dao
     * @param clusterId the related cluster ID, null for the root graph
     * @param graphElement
     * @return
     * @throws Exception 
     */
    private IGraphCluster addGraph(ModelDao dao, final Element clusterElement, final Element graphElement) throws Exception {
        
        Element tmp, clusterChildElement, nodeElements = null, clusterElements = null;
        NodeList nl, nlCluster;
        List<IGraphElement> nodes = new ArrayList();
        IGraphNode node;
        
        nl = graphElement.getChildNodes();
        
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                
                tmp = (Element) nl.item(i);
                if (tmp.getNodeName().contentEquals(tagNodes)) {
                    nodeElements = tmp;
                } else if (tmp.getNodeName().contentEquals(tagClusters)) {
                    clusterElements = tmp;
                }
            }
        }
        
        /**
         * Recursively create subgraphs.
         */
        if (clusterElements != null) {
        
            nl = clusterElements.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    
                    clusterChildElement = (Element) nl.item(i);
                    if (clusterChildElement.getNodeName().contentEquals(tagCluster)) {
                        
                        nlCluster = clusterChildElement.getChildNodes();
                        for (int j = 0; j < nlCluster.getLength(); j++) {
                            if (nlCluster.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                
                                tmp = (Element) nlCluster.item(j);
                                if (tmp.getNodeName().contentEquals(tagGraph)) {
                                    nodes.add(addGraph(dao, clusterChildElement, tmp));
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Cluster the related nodes.
         */
        if (nodeElements != null) {
            
            nl = nodeElements.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    
                    tmp = (Element) nl.item(i);
                    if (tmp.getNodeName().contentEquals(tagNode)) {
                        node = (IGraphNode) dao.getGraph().getNode(tmp.getAttribute(attrId));
                        node.translateXProperty().set(Double.parseDouble(tmp.getAttribute(attrPosX)));
                        node.translateYProperty().set(Double.parseDouble(tmp.getAttribute(attrPosY)));
                        nodes.add(node);
                    }
                }
            }
        }
        
        if (clusterElement == null) {
            return null; // no cluster is created for the root graph
        } else {
            IGraphCluster cl = hierarchyService.cluster(dao, nodes, clusterElement.getAttribute(attrId));
            cl.getData().setDescription(clusterElement.getAttribute(attrDescription));
            cl.getData().setLabelText(clusterElement.getAttribute(attrLabel));
//            cl.getData().setName(clusterElement.getAttribute(attrName));
            return cl;
        }
    }

    private void addArc(ModelDao dao, final Element elem) throws Exception {

        NodeList nl;
        Element tmp;
        DataArc data;
        GraphArc arc;
        IGraphNode source, target;
        
        /**
         * Data Object.
         */
        data = new DataArc(
                elem.getAttribute(attrId),
                (IDataNode) dao.getModel().getElement(elem.getAttribute(attrSource)), 
                (IDataNode) dao.getModel().getElement(elem.getAttribute(attrTarget)),
                DataArc.Type.valueOf(elem.getAttribute(attrType))
        );

        // Weights
        nl = elem.getElementsByTagName(tagWeights);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {

                tmp = (Element) nl.item(0);
                nl = tmp.getElementsByTagName(tagWeight);

                for (int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        data.addWeight(getWeight((Element) nl.item(i), dao));
                    }
                }
            }
        }

        /**
         * Arc Shape.
         */
        nl = elem.getElementsByTagName(tagShapes);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {

                tmp = (Element) nl.item(0);
                nl = tmp.getElementsByTagName(tagConnection);

                for (int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {

                        tmp = (Element) nl.item(i);
                        
                        source = (IGraphNode) dao.getGraph().getNode(tmp.getAttribute(attrSource));
                        target = (IGraphNode) dao.getGraph().getNode(tmp.getAttribute(attrTarget));
                        
                        arc = new GraphArc(tmp.getAttribute(attrId), source, target, data);

                        dataService.add(dao, arc);
                    }
                }
            }
        }
        
        // Properties
        if (elem.getAttribute(attrDisabled) != null) {
            data.setDisabled(Boolean.valueOf(elem.getAttribute(attrDisabled)));
        }
        if (elem.getAttribute(attrLabel) != null) {
            data.setLabelText(elem.getAttribute(attrLabel));
        }
        data.setDescription(elem.getAttribute(attrDescription));
//        data.setName(elem.getAttribute(attrName));
    }

    private void addPlace(ModelDao dao, final Element elem) throws Exception {

        NodeList nodes;
        Element tmp;

        /**
         * Data Object.
         */
        DataPlace place = new DataPlace(
                elem.getAttribute(attrId),
                DataPlace.Type.valueOf(elem.getAttribute(attrType))
        );

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
        
        // Properties
        if (elem.getAttribute(attrDisabled) != null) {
            place.setDisabled(Boolean.valueOf(elem.getAttribute(attrDisabled)));
        }
        if (elem.getAttribute(attrConstant) != null) {
            place.setConstant(Boolean.valueOf(elem.getAttribute(attrConstant)));
        }
        if (elem.getAttribute(attrSticky) != null) {
            place.setSticky(Boolean.valueOf(elem.getAttribute(attrSticky)));
        }
        place.setDescription(elem.getAttribute(attrDescription));
//        place.setName(elem.getAttribute(attrName));

        /**
         * Node Shapes.
         */
        addNodeShapes(dao, place, elem);
    }

    private void addTransition(ModelDao dao, final Element elem) throws Exception {
        
        NodeList nl;
        Element tmp;

        /**
         * Data Object.
         */
        DataTransition transition;
        transition = new DataTransition(
                elem.getAttribute(attrId),
                DataTransition.Type.valueOf(elem.getAttribute(attrType))
        );
        
        // Local Parameter
        nl = elem.getElementsByTagName(tagParametersLocal);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {

                tmp = (Element) nl.item(0);
                nl = tmp.getElementsByTagName(tagParameter);

                for (int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        parameterService.add(dao.getModel(), getParameter((Element) nl.item(i), transition));
                    }
                }
            }
        }

        // Properties
        if (elem.getAttribute(attrDisabled) != null) {
            transition.setDisabled(Boolean.valueOf(elem.getAttribute(attrDisabled)));
        }
        if (elem.getAttribute(attrSticky) != null) {
            transition.setSticky(Boolean.valueOf(elem.getAttribute(attrSticky)));
        }
        transition.setDescription(elem.getAttribute(attrDescription));
        transition.setFunction(getFunction(dao.getModel(), elem));
//        transition.setName(elem.getAttribute(attrName));
        
        /**
         * Node Shapes.
         */
        addNodeShapes(dao, transition, elem);
    }
    
    private void addNodeShapes(ModelDao dao, IDataNode data, final Element elem) throws Exception {
        
        NodeList nl;
        Element tmp;
        IGraphNode shape;
        
        nl = elem.getElementsByTagName(tagShapes);
        if (nl.getLength() == 1) {
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {

                tmp = (Element) nl.item(0);
                nl = tmp.getElementsByTagName(tagNode);

                for (int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {

                        tmp = (Element) nl.item(i);
                        
                        switch (data.getType()) {
                            case PLACE:
                                shape = new GraphPlace(tmp.getAttribute(attrId), (DataPlace) data);
                                break;
                            case TRANSITION:
                                shape = new GraphTransition(tmp.getAttribute(attrId), (DataTransition) data);
                                break;
                            default:
                                throw new IOException("Malformed node type '" + data.getType() + "'. Cannot create shape.");
                        }
                        
//                        if (elem.getAttribute(attrDisabled) != null) { // not useful here as arcs are created later and carry their own disabled state
//                            shape.setElementDisabled(Boolean.valueOf(elem.getAttribute(attrDisabled)));
//                        }
                        
                        dataService.add(dao, shape);
                    }
                }
            }
        }
        if (elem.getAttribute(attrLabel) != null) {
            data.setLabelText(elem.getAttribute(attrLabel));
        }
        if (data.getType() == DataType.PLACE) {
            
            DataPlace place = (DataPlace) data;
            double tokenStart = 
                    place.getTokens().iterator().next().getValueStart();
            
            if (tokenStart != 0) {
                place.setTokenLabelText(Double.toString(tokenStart));
            } else {
                place.setTokenLabelText("");
            }
        }
        if (data.isDisabled()) {
            data.setDisabled(true);
        }
    }

    private Colour getColour(Element elem) {
        Colour colour = new Colour(
                elem.getAttribute(attrId),
                elem.getAttribute(attrDescription)
        );
        return colour;
    }

    private Function getFunction(Model model, Element elem) throws Exception {
        NodeList nodes = elem.getElementsByTagName(tagFunction);
        if (nodes.getLength() > 0) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) nodes.item(0);
                return functionBuilder.build(model, elem.getTextContent(), false);
            }
        }
        return functionBuilder.build(model, "1", false);
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

    private Weight getWeight(Element elem, ModelDao dao) throws Exception {
//        Weight weight = new Weight(new Colour(elem.getAttribute(attrColourId), ""));
        Weight weight = new Weight(dao.getModel().getColour(elem.getAttribute(attrColourId)));
        weight.setFunction(getFunction(dao.getModel(), elem));
        return weight;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="File export and related methods">

    public void exportXml(File file, ModelDao dao) throws IOException, ParserConfigurationException, TransformerException, FileNotFoundException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.newDocument();
        
        Element model = dom.createElement(tagModel); // create the root element
        
        model.setAttribute(attrAuthor, dao.getAuthor());
        model.setAttribute(attrCreationDateTime, dao.getCreationDateTime().format(DateTimeFormatter.ofPattern(formatDateTime)));
        model.setAttribute(attrDescription, dao.getModelDescription());
        model.setAttribute(attrId, dao.getModelId());
        model.setAttribute(attrName, dao.getModelName());
        
        model.setAttribute(attrCurrentClusterId, Integer.toString(dao.getNextClusterId()));
        model.setAttribute(attrCurrentNodeId, Integer.toString(dao.getNextNodeId()));
        model.setAttribute(attrCurrentPlaceId, Integer.toString(dao.getNextPlaceId()));
        model.setAttribute(attrCurrentTransitionId, Integer.toString(dao.getNextTransitionId()));
        dao.setNextClusterId(dao.getNextClusterId() - 1); // revoke iteration
        dao.setNextNodeId(dao.getNextNodeId() - 1);
        dao.setNextPlaceId(dao.getNextPlaceId() - 1);
        dao.setNextTransitionId(dao.getNextTransitionId() - 1);
        
        model.appendChild(getArcsElement(dom, dao.getModel().getArcs()));
        model.appendChild(getPlacesElement(dom, dao.getModel().getPlaces()));
        model.appendChild(getTransitionsElement(dom, dao.getModel().getTransitions()));
        model.appendChild(getColorsElement(dom, dao.getModel().getColours()));
        model.appendChild(getGraphElement(dom, dao.getGraphRoot()));
            
        Element params = dom.createElement(tagParameters);
        dao.getModel().getParameters().forEach(param -> {
            if (param.getType() == Parameter.Type.GLOBAL) { // store global only, references are generated on import
                params.appendChild(getParameterElement(dom, param));
            }
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
    
    private Element getArcsElement(Document dom, Collection arcs) throws IOException {
        
        Element elements = dom.createElement(tagArcs);
        
        for (Object obj : arcs) {
            DataArc data = (DataArc) obj;
            
            Element a = dom.createElement(tagArc);
            a.setAttribute(attrId, data.getId());
            a.setAttribute(attrType, data.getArcType().toString());
            a.setAttribute(attrSource, data.getSource().getId());
            a.setAttribute(attrTarget, data.getTarget().getId());
            if (data.isDisabled()) {
                a.setAttribute(attrDisabled, Boolean.toString(data.isDisabled()));
            }
//            if (data.getName() != null && !data.getName().isEmpty()) {
//                a.setAttribute(attrName, data.getName());
//            }
            if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                a.setAttribute(attrDescription, data.getDescription());
            }
            
            // Shapes
            if (!data.getShapes().isEmpty()) {
                a.appendChild(getArcShapesElement(dom, data));
            } else {
                throw new IOException("No shape associated to arc data!");
            }
            
            // Weights
            a.appendChild(getWeightsElement(dom, data));

            elements.appendChild(a);
        }
        return elements;
    }
    
    public Element getPlacesElement(Document dom, Collection places) throws IOException {
        
        Element elements = dom.createElement(tagPlaces);
        
        for (Object obj : places) {
            DataPlace data = (DataPlace) obj;
            
            Element p = dom.createElement(tagPlace);
            p.setAttribute(attrId, data.getId());
            p.setAttribute(attrType, data.getPlaceType().toString());
            if (data.isDisabled()) {
                p.setAttribute(attrDisabled, Boolean.toString(data.isDisabled()));
            }
            if (data.isConstant()) {
                p.setAttribute(attrConstant, Boolean.toString(data.isConstant()));
            }
            if (data.isSticky()) {
                p.setAttribute(attrSticky, Boolean.toString(data.isSticky()));
            }
//            if (data.getName() != null && !data.getName().isEmpty()) {
//                p.setAttribute(attrName, data.getName());
//            }
            if (data.getLabelText() != null && !data.getLabelText().isEmpty()) {
                p.setAttribute(attrLabel, data.getLabelText());
            }
            if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                p.setAttribute(attrDescription, data.getDescription());
            }
            
            // Shapes
            if (!data.getShapes().isEmpty()) {
                p.appendChild(getNodeShapesElement(dom, data));
            } else {
                throw new IOException("No shape associated to arc data!");
            }
            
            // Tokens
            p.appendChild(getTokensElement(dom, data));

            elements.appendChild(p);
        }
        
        return elements;
    }
    
    private Element getTransitionsElement(Document dom, Collection transitions) throws IOException {
        Element elements = dom.createElement(tagTransitons);
        
        for (Object obj : transitions) {
            
            DataTransition data = (DataTransition) obj;
            Element t = dom.createElement(tagTransiton);
            
            t.setAttribute(attrId, data.getId());
            t.setAttribute(attrType, data.getTransitionType().toString());
            if (data.isDisabled()) {
                t.setAttribute(attrDisabled, Boolean.toString(data.isDisabled()));
            }
            if (data.isSticky()) {
                t.setAttribute(attrSticky, Boolean.toString(data.isSticky()));
            }
//            if (data.getName() != null && !data.getName().isEmpty()) {
//                t.setAttribute(attrName, data.getName());
//            }
            if (data.getLabelText() != null && !data.getLabelText().isEmpty()) {
                t.setAttribute(attrLabel, data.getLabelText());
            }
            if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                t.setAttribute(attrDescription, data.getDescription());
            }
            
            // Shapes
            if (!data.getShapes().isEmpty()) {
                t.appendChild(getNodeShapesElement(dom, data));
            } else {
                throw new IOException("No shape associated to arc data!");
            }
            
            // Local Parameters
            Element p = dom.createElement(tagParametersLocal);
            data.getLocalParameters().forEach(param -> {
                p.appendChild(getParameterElement(dom, param));
            });
            t.appendChild(p);

            // Function
            Element f = dom.createElement(tagFunction);
            if (data.getFunction().getUnit() != null && !data.getFunction().getUnit().isEmpty()) {
                f.setAttribute(attrUnit, data.getFunction().getUnit());
            }
            f.setTextContent(data.getFunction().toString());
            t.appendChild(f);

            elements.appendChild(t);
        }
        
        return elements;
    }
    
    public Element getArcShapesElement(Document dom, DataArc arc) throws IOException {

        Element shapes = dom.createElement(tagShapes);
        Element conn;
        
        for (IGraphElement shape : arc.getShapes()) {
            IDataElement data = shape.getData();
            
            if (data.equals(arc)) {
                conn = getArcShapeElement(dom, (GraphArc) shape);
                shapes.appendChild(conn);
                
            } else if (data.getType() == DataType.CLUSTERARC) {
                DataClusterArc dca = (DataClusterArc) data;
                for (IGraphArc innerArc : dca.getStoredArcs().values()) {
                    conn = getArcShapeElement(dom, innerArc);
                    shapes.appendChild(conn);
                }
                
            } else {
                throw new IOException("Invalid shape associated to arc data!");
            }
        }
        
        return shapes;
    }
    
    private Element getArcShapeElement(Document dom, IGraphArc connection) {
        Element c = dom.createElement(tagConnection);
        c.setAttribute(attrId, connection.getId());
        c.setAttribute(attrSource, String.valueOf(connection.getSource().getId()));
        c.setAttribute(attrTarget, String.valueOf(connection.getTarget().getId()));
        return c;
    }
    
    private Element getNodeShapesElement(Document dom, IDataNode node) {
        Element elements = dom.createElement(tagShapes);
        for (IGraphElement shape : node.getShapes()) {
            Element n = dom.createElement(tagNode);
            n.setAttribute(attrId, shape.getId());
            elements.appendChild(n);
        }
        return elements;
    }
    
    private Element getGraphElement(Document dom, Graph graph) {
        Element elements = dom.createElement(tagGraph);
        elements.appendChild(getClustersElement(dom, graph.getClusters()));
        elements.appendChild(getNodesElement(dom, graph.getNodes()));
        return elements;
    }
    
    private Element getClustersElement(Document dom, Collection<IGravisCluster> clusters) {
        
        Element elements = dom.createElement(tagClusters);
        Element c;
        
        for (IGravisCluster cluster : clusters) {
            c = dom.createElement(tagCluster);
            c.setAttribute(attrId, cluster.getId());
            c.setAttribute(attrLabel, ((IGraphCluster)cluster).getData().getLabelText());
//            c.setAttribute(attrName, ((IGraphCluster)cluster).getData().getName());
            c.setAttribute(attrDescription, ((IGraphCluster)cluster).getData().getDescription());
            c.appendChild(getGraphElement(dom, cluster.getGraph()));
            elements.appendChild(c);
        }
        return elements;
    }
    
    private Element getNodesElement(Document dom, Collection<IGravisNode> nodes) {
        
        Element elements = dom.createElement(tagNodeShapes);
        Element n;
        
        for (IGravisNode node : nodes) {
            n = dom.createElement(tagNode);
            n.setAttribute(attrId, node.getId());
            n.setAttribute(attrPosX, String.valueOf(node.translateXProperty().get()));
            n.setAttribute(attrPosY, String.valueOf(node.translateYProperty().get()));
            elements.appendChild(n);
        }
        
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
        if (param.getRelatedElement() != null) {
            p.setAttribute(attrElementId, param.getRelatedElement().getId());
        }
        return p;
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
            
            Element f = dom.createElement(tagFunction);
            if (weight.getFunction().getUnit() != null && !weight.getFunction().getUnit().isEmpty()) {
                f.setAttribute(attrUnit, weight.getFunction().getUnit());
            }
            f.setTextContent(weight.getFunction().toString());
            w.appendChild(f);
            
            weights.appendChild(w);
        });
        return weights;
    }
    
    // </editor-fold>
}
