/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.io;

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
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCurve;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphEdge;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import edu.unibi.agbi.petrinet.util.FunctionBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private final String formatDateTime = "yy-MM-dd HH:mm:ss";
    private final String dtdModelData = "model.dtd";

    private final String attrAuthor = "author";
    private final String attrColourId = "colourId";
    private final String attrCurved = "curved";
    private final String attrCreationDateTime = "creationDateTime";
    private final String attrDescription = "description";
    private final String attrElementId = "elementId";
    private final String attrId = "id";
    private final String attrLabel = "label";
    private final String attrMax = "max";
    private final String attrMin = "min";
    private final String attrName = "name";
    private final String attrNote = "note";
    private final String attrParameterId = "parameterId";
    private final String attrPosX = "posX";
    private final String attrPosY = "posY";
    private final String attrSource = "source";
    private final String attrStart = "start";
    private final String attrTarget = "target";
    private final String attrType = "type";
    private final String attrUnit = "unit";
    private final String attrValue = "value";

    private final String tagArcs = "Arcs";
    private final String tagArc = "Arc";
    private final String tagColours = "Colours";
    private final String tagColour = "Colour";
    private final String tagConnection = "Connection";
    private final String tagFunction = "Function";
    private final String tagLabel = "Label";
    private final String tagModel = "Model";
    private final String tagNode = "Node";
    private final String tagParameters = "Parameters";
    private final String tagParameter = "Parameter";
    private final String tagPlaces = "Places";
    private final String tagPlace = "Place";
    private final String tagRelatedParameters = "RelatedParameters";
    private final String tagRelatedParameter = "RelatedParameter";
    private final String tagTokens = "Tokens";
    private final String tagToken = "Token";
    private final String tagTransitons = "Transitions";
    private final String tagTransiton = "Transition";
    private final String tagVisualisation = "Visualisation";
    private final String tagWeights = "Weights";
    private final String tagWeight = "Weight";

    public void exportXml(File file, DataDao dataDao) throws ParserConfigurationException, TransformerException, FileNotFoundException {

        Element model, colourElements, parameterElements, arcElements, placeElements, transitionElements;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.newDocument();

        arcElements = dom.createElement(tagArcs);
        dataDao.getModel().getArcs().forEach(a -> {
            DataArc data = (DataArc) a;

            Element arcElement = dom.createElement(tagArc);
            arcElement.setAttribute(attrType, data.getArcType().toString());
            arcElement.setAttribute(attrSource, data.getSource().getId());
            arcElement.setAttribute(attrTarget, data.getTarget().getId());
            if (data.getName() != null && !data.getName().isEmpty()) {
                arcElement.setAttribute(attrName, data.getName());
            }
            if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                arcElement.setAttribute(attrDescription, data.getDescription());
            }

            arcElement.appendChild(getRelatedParameterIdsElement(dom, data));
            arcElement.appendChild(getWeightsElement(dom, data));
            arcElement.appendChild(getShapesForArcElement(dom, data));

            arcElements.appendChild(arcElement);
        });

        placeElements = dom.createElement(tagPlaces);
        dataDao.getModel().getPlaces().forEach(p -> {
            DataPlace data = (DataPlace) p;

            Element placeElement = dom.createElement(tagPlace);
            placeElement.setAttribute(attrId, data.getId());
            placeElement.setAttribute(attrType, data.getPlaceType().toString());
            if (data.getName() != null && !data.getName().isEmpty()) {
                placeElement.setAttribute(attrName, data.getName());
            }
            if (data.getLabelText() != null && !data.getLabelText().isEmpty()) {
                placeElement.setAttribute(attrLabel, data.getLabelText());
            }
            if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                placeElement.setAttribute(attrDescription, data.getDescription());
            }
            
            placeElement.appendChild(getRelatedParameterIdsElement(dom, data));
            placeElement.appendChild(getTokensElement(dom, data));
            placeElement.appendChild(getShapesForNodeElement(dom, data));

            placeElements.appendChild(placeElement);
        });

        transitionElements = dom.createElement(tagTransitons);
        dataDao.getModel().getTransitions().forEach(t -> {
            DataTransition data = (DataTransition) t;

            Element transitionElement = dom.createElement(tagTransiton);
            transitionElement.setAttribute(attrId, data.getId());
            transitionElement.setAttribute(attrType, data.getTransitionType().toString());
            if (data.getName() != null && !data.getName().isEmpty()) {
                transitionElement.setAttribute(attrName, data.getName());
            }
            if (data.getLabelText() != null && !data.getLabelText().isEmpty()) {
                transitionElement.setAttribute(attrLabel, data.getLabelText());
            }
            if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                transitionElement.setAttribute(attrDescription, data.getDescription());
            }

            Element functionElement = dom.createElement(tagFunction);
            if (!data.getFunction().getUnit().isEmpty()) {
                functionElement.setAttribute(attrUnit, data.getFunction().getUnit());
            }
            functionElement.setTextContent(data.getFunction().toString());
            transitionElement.appendChild(functionElement);
            
            transitionElement.appendChild(getRelatedParameterIdsElement(dom, data));
            transitionElement.appendChild(getShapesForNodeElement(dom, data));

            transitionElements.appendChild(transitionElement);
        });

        colourElements = dom.createElement(tagColours);
        dataDao.getModel().getColours().forEach(colour -> {
            Element colourElement = dom.createElement(tagColour);
            colourElement.setAttribute(attrId, colour.getId());
            if (colour.getDescription() != null && !colour.getDescription().isEmpty()) {
                colourElement.setAttribute(attrDescription, colour.getDescription());
            }

            colourElements.appendChild(colourElement);
        });

        parameterElements = dom.createElement(tagParameters);
        dataDao.getModel().getParameters().forEach(param -> {
            Element parameterElement = dom.createElement(tagParameter);
            parameterElement.setAttribute(attrId, param.getId());
            if (param.getNote() != null && !param.getNote().isEmpty()) {
                parameterElement.setAttribute(attrNote, param.getNote());
            }
            parameterElement.setAttribute(attrType, param.getType().toString());
            parameterElement.setTextContent(param.getValue());
            parameterElement.setAttribute(attrElementId, param.getRelatedElementId());

            parameterElements.appendChild(parameterElement);
        });

        model = dom.createElement(tagModel); // create the root element

        model.setAttribute(attrAuthor, dataDao.getAuthor());
        model.setAttribute(attrCreationDateTime, dataDao.getCreationDateTime().format(DateTimeFormatter.ofPattern(formatDateTime)));
        model.setAttribute(attrDescription, dataDao.getModelDescription());
        model.setAttribute(attrId, dataDao.getDaoId());
        model.setAttribute(attrName, dataDao.getModelName());

        model.appendChild(arcElements);
        model.appendChild(placeElements);
        model.appendChild(transitionElements);
        model.appendChild(colourElements);
        model.appendChild(parameterElements);

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

    public DataDao importXml(File file) throws Exception {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
//        doc.getDocumentElement().normalize();

        NodeList nodes;
        Element root, elem;
        DataDao dataDao;

        /**
         * Model.
         */
        nodes = doc.getElementsByTagName(tagModel);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
                root = (Element) nodes.item(0);
                dataDao = getDataDao(root);
            } else {
                throw new Exception("File import failed. Malformed 'Model' element.");
            }
        } else {
            throw new Exception("File import failed. More than one or no 'Model' element.");
        }

        /**
         * Colours.
         */
        nodes = root.getElementsByTagName(tagColours);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nodes.item(0);
                nodes = elem.getElementsByTagName(tagColour);

                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        dataDao.getModel().add(getColour((Element) nodes.item(i)));
                    }
                }
            }
        }

        /**
         * Parameters.
         */
        nodes = root.getElementsByTagName(tagParameters);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nodes.item(0);
                nodes = elem.getElementsByTagName(tagParameter);

                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        dataDao.getModel().add(getParameter((Element) nodes.item(i)));
                    }
                }
            }
        }

        /**
         * Places.
         */
        nodes = root.getElementsByTagName(tagPlaces);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nodes.item(0);
                nodes = elem.getElementsByTagName(tagPlace);

                // Each place
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        DataPlace place = getDataPlace((Element) nodes.item(i));
                        dataDao.getModel().add(place);
                        for (IGraphElement shape : place.getShapes()) {
                            dataDao.getGraphRoot().add((IGraphNode) shape);
                        }
                    }
                }
            }
        }

        /**
         * Transitions.
         */
        nodes = root.getElementsByTagName(tagTransitons);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nodes.item(0);
                nodes = elem.getElementsByTagName(tagTransiton);

                // Each transition
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        DataTransition transition = getDataTransition((Element) nodes.item(i));
                        transition.getFunction().getParameterIds().forEach(id -> {
                            dataDao.getModel().getParameter(id).getUsingElements().add(transition);
                        });
                        dataDao.getModel().add(transition);
                        for (IGraphElement shape : transition.getShapes()) {
                            dataDao.getGraphRoot().add((IGraphNode) shape);
                        }
                    }
                }
            }
        }

        /**
         * Arcs.
         */
        nodes = root.getElementsByTagName(tagArcs);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nodes.item(0);
                nodes = elem.getElementsByTagName(tagArc);

                // Each arc
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

                        elem = (Element) nodes.item(i);

                        for (int j = 0; j < elem.getAttributes().getLength(); j++) {
                            System.out.println(elem.getAttributes().item(j).getNodeName() + " = " + elem.getAttributes().item(j).getTextContent());
                        }

                        DataArc arc = getDataArc(
                                (Element) nodes.item(i),
                                (IDataNode) dataDao.getModel().getElement(elem.getAttribute(attrSource)),
                                (IDataNode) dataDao.getModel().getElement(elem.getAttribute(attrTarget)),
                                dataDao
                        );
                        dataDao.getModel().add(arc);
                        for (IGraphElement shape : arc.getShapes()) {
                            dataDao.getGraphRoot().add((IGraphArc) shape);
                        }
                    }
                }
            }
        }

        return dataDao;
    }

    private DataArc getDataArc(final Element elem, IDataNode source, IDataNode target, DataDao dataDao) {

        NodeList nodes;
        Element tmp;

        DataArc arc = new DataArc(
                source,
                target,
                DataArc.Type.valueOf(elem.getAttribute(attrType))
        );
        arc.setDescription(elem.getAttribute(attrDescription));
        arc.setName(elem.getAttribute(attrName));
        
        // Related parameter
        setRelatedParameterIds(elem, arc);

        // Weights
        nodes = elem.getElementsByTagName(tagWeights);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                tmp = (Element) nodes.item(0);
                nodes = tmp.getElementsByTagName(tagWeight);

                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        arc.addWeight(getWeight((Element) nodes.item(i), dataDao));
                    }
                }
            }
        }

        // Shapes
        nodes = elem.getElementsByTagName(tagVisualisation);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                tmp = (Element) nodes.item(0);
                nodes = tmp.getElementsByTagName(tagConnection);

                IGraphArc shape;
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

                        tmp = (Element) nodes.item(i);

                        boolean curved = Boolean.parseBoolean(tmp.getAttribute(attrCurved));

                        if (curved) {
                            shape = new GraphCurve(
                                    tmp.getAttribute(attrSource) + tmp.getAttribute(attrTarget),
                                    (IGraphNode) dataDao.getGraphRoot().getNode(tmp.getAttribute(attrSource)),
                                    (IGraphNode) dataDao.getGraphRoot().getNode(tmp.getAttribute(attrTarget)),
                                    arc);
                        } else {
                            shape = new GraphEdge(
                                    tmp.getAttribute(attrSource) + tmp.getAttribute(attrTarget),
                                    (IGraphNode) dataDao.getGraphRoot().getNode(tmp.getAttribute(attrSource)),
                                    (IGraphNode) dataDao.getGraphRoot().getNode(tmp.getAttribute(attrTarget)),
                                    arc);
                        }
//                        arc.getShapes().add(shape);
                    }
                }
            }
        }

        return arc;
    }

    private DataPlace getDataPlace(final Element elem) {

        NodeList nodes;
        Element tmp;

        DataPlace place = new DataPlace(
                elem.getAttribute(attrId),
                DataPlace.Type.valueOf(elem.getAttribute(attrType))
        );
        
        // Related parameter
        setRelatedParameterIds(elem, place);

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

        // Shapes
        nodes = elem.getElementsByTagName(tagVisualisation);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                tmp = (Element) nodes.item(0);
                nodes = tmp.getElementsByTagName(tagNode);

                GraphPlace shape;
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        shape = new GraphPlace(null, place);
                        setShapePos((Element) nodes.item(i), shape);
                    }
                }
            }
        }

        place.setDescription(elem.getAttribute(attrDescription));
        place.setLabelText(elem.getAttribute(attrLabel));
        place.setName(elem.getAttribute(attrName));

        return place;
    }

    private DataTransition getDataTransition(final Element elem) throws Exception {

        NodeList nodes;
        Element tmp;

        DataTransition transition = new DataTransition(
                elem.getAttribute(attrId),
                DataTransition.Type.valueOf(elem.getAttribute(attrType))
        );
        
        // Related parameter
        setRelatedParameterIds(elem, transition);

        // Shapes
        nodes = elem.getElementsByTagName(tagVisualisation);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                tmp = (Element) nodes.item(0);
                nodes = tmp.getElementsByTagName(tagNode);

                GraphTransition shape;
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        shape = new GraphTransition(null, transition);
                        setShapePos((Element) nodes.item(i), shape);
                    }
                }
            }
        }

        transition.setDescription(elem.getAttribute(attrDescription));
        transition.setLabelText(elem.getAttribute(attrLabel));
        transition.setName(elem.getAttribute(attrName));
        transition.setFunction(getFunction(elem));

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
                return functionBuilder.build(elem.getTextContent());
            }
        }
        return functionBuilder.build("1");
    }

    private DataDao getDataDao(Element elem) {
        DataDao dao = new DataDao();
        dao.setAuthor(elem.getAttribute(attrAuthor));
        dao.setCreationDateTime(LocalDateTime.parse(elem.getAttribute(attrCreationDateTime), DateTimeFormatter.ofPattern(formatDateTime)));
        dao.setModelDescription(elem.getAttribute(attrDescription));
        dao.setDaoId(elem.getAttribute(attrId));
        dao.setModelName(elem.getAttribute(attrName));
        return dao;
    }

    private IGraphNode setShapePos(Element elem, IGraphNode node) {
        node.setId(elem.getAttribute(attrId));
        node.translateXProperty().set(Double.parseDouble(elem.getAttribute(attrPosX)));
        node.translateYProperty().set(Double.parseDouble(elem.getAttribute(attrPosY)));
        if (elem.getElementsByTagName(tagLabel).getLength() == 1) {
            Node label = elem.getElementsByTagName(tagLabel).item(0);
            if (label.getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) label;
                node.getLabel().setTranslateX(Double.parseDouble(elem.getAttribute(attrPosX)));
                node.getLabel().setTranslateY(Double.parseDouble(elem.getAttribute(attrPosX)));
            }
        }
        return node;
    }
    
    private Element getShapesForArcElement(Document dom, IDataArc data) {
        Element shapes = dom.createElement(tagVisualisation);
        data.getShapes().forEach(c -> {
            IGraphArc connection = (IGraphArc) c;
            Element connectionElement = dom.createElement(tagConnection);
            connectionElement.setAttribute(attrSource, String.valueOf(connection.getSource().getId()));
            connectionElement.setAttribute(attrTarget, String.valueOf(connection.getTarget().getId()));
            if (connection instanceof GraphCurve) {
                connectionElement.setAttribute(attrCurved, "true");
            } else {
                connectionElement.setAttribute(attrCurved, "false");
            }
            shapes.appendChild(connectionElement);
        });
        return shapes;
    }
    
    private Element getShapesForNodeElement(Document dom, IDataNode data) {

        Element shapes = dom.createElement(tagVisualisation);
        data.getShapes().forEach(e -> {

            IGraphNode n = (IGraphNode) e;

            Element nodeElement = dom.createElement(tagNode);
            nodeElement.setAttribute(attrPosX, String.valueOf(n.getShape().getTranslateX()));
            nodeElement.setAttribute(attrPosY, String.valueOf(n.getShape().getTranslateY()));
            nodeElement.setAttribute(attrId, String.valueOf(n.getId()));

            Element labelElement = dom.createElement(tagLabel);
            labelElement.setAttribute(attrPosX, String.valueOf(n.getLabel().getTranslateX()));
            labelElement.setAttribute(attrPosY, String.valueOf(n.getLabel().getShape().getTranslateY()));
            labelElement.setTextContent(n.getLabel().getText());
            nodeElement.appendChild(labelElement);

            shapes.appendChild(nodeElement);
        });
        return shapes;
    }

    private Parameter getParameter(Element elem) {
        Parameter param = new Parameter(
                elem.getAttribute(attrId),
                elem.getAttribute(attrNote),
                elem.getTextContent(),
                Parameter.Type.valueOf(elem.getAttribute(attrType)),
                elem.getAttribute(attrElementId)
        );
        return param;
    }
    
    private void setRelatedParameterIds(Element elem, IDataElement data) {
        
        NodeList nodes = elem.getElementsByTagName(tagRelatedParameters);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nodes.item(0);
                nodes = elem.getElementsByTagName(tagRelatedParameter);

                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        data.getRelatedParameterIds().add(((Element) nodes.item(i)).getAttribute(attrParameterId));
                    }
                }
            }
        }
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

    private Token getToken(Element elem) {
        Token token = new Token(new Colour(elem.getAttribute(attrColourId), ""));
        token.setValueStart(Double.parseDouble(elem.getAttribute(attrStart)));
        token.setValueMin(Double.parseDouble(elem.getAttribute(attrMin)));
        token.setValueMax(Double.parseDouble(elem.getAttribute(attrMax)));
        return token;
    }
    
    private Element getTokensElement(Document dom, DataPlace data) {
        Element tokens = dom.createElement(tagTokens);
        data.getTokens().forEach(token -> {
            Element t = dom.createElement(tagToken);
            t.setAttribute(attrColourId, token.getColour().getId());
            t.setAttribute(attrStart, String.valueOf(token.getValueStart()));
            t.setAttribute(attrMin, String.valueOf(token.getValueMin()));
            t.setAttribute(attrMax, String.valueOf(token.getValueMax()));
            tokens.appendChild(t);
        });
        return tokens;
    }

    private Weight getWeight(Element elem, DataDao dataDao) {
        dataDao.getModel().getColours();
        Weight weight = new Weight(new Colour(elem.getAttribute(attrColourId), ""));
        weight.setValue(elem.getAttribute(attrValue));
        return weight;
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
}
