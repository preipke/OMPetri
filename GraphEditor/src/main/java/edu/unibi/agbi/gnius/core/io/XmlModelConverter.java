/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.io;

import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.dao.GraphDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
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
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleIntegerProperty;
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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${format.datetime}") private String formatDateTime;
    @Value("${xml.results.data.dtd}") private String dtdModelData;

    private final String attrAuthor = "author";
    private final String attrColour = "colour";
    private final String attrDateTime = "dateTime";
    private final String attrDescription = "description";
    private final String attrId = "id";
    private final String attrLabel = "label";
    private final String attrMax = "max";
    private final String attrMin = "min";
    private final String attrName = "name";
    private final String attrNextPlaceId = "nextPlaceId";
    private final String attrNextTransitionId = "nextTransitionId";
    private final String attrNote = "note";
    private final String attrPosX = "posX";
    private final String attrPosY = "posY";
    private final String attrSource = "source";
    private final String attrStart = "start";
    private final String attrTarget = "target";
    private final String attrType = "type";
    private final String attrUnit = "unit";
    private final String attrValue = "value";

    private final String tagModel = "Model";

    private final String tagColours = "Colours";
    private final String tagColour = "Colour";

    private final String tagParameters = "Parameters";
    private final String tagParameter = "Parameter";

    private final String tagArcs = "Arcs";
    private final String tagArc = "Arc";
    private final String tagWeights = "Weights";
    private final String tagWeight = "Weight";
//    private final String tagSource = "Source";
//    private final String tagTarget = "Target";

    private final String tagPlaces = "Places";
    private final String tagPlace = "Place";
    private final String tagTokens = "Tokens";
    private final String tagToken = "Token";

    private final String tagTransitons = "Transitions";
    private final String tagTransiton = "Transition";
    private final String tagFunction = "Function";

    private final String tagConnection = "Connection";
    private final String tagLabel = "Label";
    private final String tagNode = "Node";
    private final String tagVisualisation = "Visualisation";

    private final SimpleIntegerProperty exportId = new SimpleIntegerProperty(1);

    public DataDao importXml(File file) {

        DataDao dataDao;
//        GraphDao graphDao;
//        String dateTime = "...";
//        LocalDateTime simulationDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(formatDateTime));

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            doc.getDocumentElement().normalize();

            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

            NodeList nodes;
            Element root, elem;

            /**
             * Model.
             */
            nodes = doc.getElementsByTagName(tagModel);
            if (nodes.getLength() == 1) {
                if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
                    root = (Element) nodes.item(0);
                    dataDao = getModel(root);
                } else {
                    System.out.println("Invalid 'Model' element!");
                    return null;
                }
            } else {
                System.out.println("More than 1 or no 'Model' tag!");
                return null;
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
                            dataDao.add(getColour((Element) nodes.item(i)));
                        } else {
                            System.out.println("Invalid 'Colour' element!");
                        }
                    }
                } else {
                    System.out.println("Invalid 'Colours' element!");
                }
            } else {
                System.out.println("More than 1 or no 'Colours' tag!");
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
                        if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
                            dataDao.add(getParameter((Element) nodes.item(0)));
                        } else {
                            System.out.println("Invalid 'Parameter' element!");
                        }
                    }
                } else {
                    System.out.println("Invalid 'Parameters' element!");
                }
            } else {
                System.out.println("More than 1 or no 'Parameters' element!");
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
                            dataDao.add(getDataPlace((Element) nodes.item(i)));
                        } else {
                            System.out.println("Invalid 'Place' element!");
                        }
                    }
                } else {
                    System.out.println("Invalid 'Places' element!");
                }
            } else {
                System.out.println("More than 1 or no 'Places' element!");
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
                            dataDao.add(getDataTransition((Element) nodes.item(i)));
                        } else {
                            System.out.println("Invalid 'Transition' element!");
                        }
                    }
                } else {
                    System.out.println("Invalid 'Transitions' element!");
                }
            } else {
                System.out.println("More than 1 or no 'Transitions' node!");
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
                                    (IDataNode) dataDao.getNode(elem.getAttribute(attrSource)),
                                    (IDataNode) dataDao.getNode(elem.getAttribute(attrTarget))
                            );
                            dataDao.add(arc);
                        } else {
                            System.out.println("Invalid 'Arc' element!");
                        }
                    }
                } else {
                    System.out.println("Invalid 'Arcs' element!");
                }
            } else {
                System.out.println("More than 1 or no 'Arcs' tag!");
            }

            return dataDao;

        } catch (Exception e) {
            e.printStackTrace();
        }
//        setExportId(0);
        return null;
    }

    private DataArc getDataArc(Element elem, IDataNode source, IDataNode target) {

        NodeList nodes;

        DataArc arc = new DataArc(
                source,
                target,
                DataArc.Type.valueOf(elem.getAttribute(attrType))
        );
        arc.setDescription(attrDescription);
        arc.setName(attrName);

        // Weights
        nodes = elem.getElementsByTagName(tagWeights);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nodes.item(0);
                nodes = elem.getElementsByTagName(tagWeight);
                
                Weight weight;
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        weight = getWeight(elem);
                        arc.getWeightMap().put(weight.getColour(), weight);
                    }
                }
            }
        }
        
        // Shapes
        nodes = elem.getElementsByTagName(tagVisualisation);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nodes.item(0);
                nodes = elem.getElementsByTagName(tagConnection);

                GraphEdge shape;
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

//                        shape = new GraphEdge(arc);
//                        shape = (GraphEdge) getArcShape((Element) nodes.item(i), shape);
//
//                        arc.getShapes().add(shape);
                    }
                }
            }
        }

        return arc;
    }

    private DataPlace getDataPlace(Element elem) {

        NodeList nodes;
        Element tmp;

        DataPlace place = new DataPlace(
                elem.getAttribute(attrId),
                DataPlace.Type.valueOf(elem.getAttribute(attrType))
        );
        place.setDescription(attrDescription);
        place.setLabelText(attrId);
        place.setName(attrName);

        // Token
        nodes = elem.getElementsByTagName(tagTokens);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                tmp = (Element) nodes.item(0);
                nodes = tmp.getElementsByTagName(tagToken);
                
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        place.setToken(getToken((Element) nodes.item(i)));
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

                        shape = new GraphPlace(place);
                        shape = (GraphPlace) getNodeShape((Element) nodes.item(i), shape);

                        place.getShapes().add(shape);
                    }
                }
            }
        }

        return place;
    }

    private DataTransition getDataTransition(Element elem) throws IOException {

        DataTransition transition = new DataTransition(
                elem.getAttribute(attrId),
                DataTransition.Type.valueOf(elem.getAttribute(attrType))
        );
        transition.setDescription(attrDescription);
        transition.setLabelText(attrId);
        transition.setName(attrName);
        transition.setFunction(getFunction(elem));

        // Shapes
        NodeList nodes = elem.getElementsByTagName(tagVisualisation);
        if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {

                elem = (Element) nodes.item(0);
                nodes = elem.getElementsByTagName(tagNode);

                GraphTransition shape;
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

                        shape = new GraphTransition(transition);
                        shape = (GraphTransition) getNodeShape((Element) nodes.item(i), shape);

                        transition.getShapes().add(shape);
                    }
                }
            }
        }
        return transition;
    }

    private Colour getColour(Element elem) {
        Colour colour = new Colour(
                elem.getAttribute(attrId),
                elem.getAttribute(attrDescription)
        );
        return colour;
    }

    private Function getFunction(Element elem) throws IOException {
        NodeList nodes = elem.getElementsByTagName(tagFunction);
        if (nodes.getLength() > 0) {
            if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) nodes.item(0);
                return functionBuilder.build(elem.getTextContent());
            }
        }
        return functionBuilder.build("1");
    }

    private DataDao getModel(Element elem) {
        DataDao dataDao = new DataDao(
                Integer.parseInt(elem.getAttribute(attrNextPlaceId)),
                Integer.parseInt(elem.getAttribute(attrNextTransitionId))
        );
        dataDao.setAuthor(elem.getAttribute(attrAuthor));
        dataDao.setDescription(elem.getAttribute(attrDescription));
//        dataDao.setDate(elem.getAttribute(attrDateTime));
        dataDao.setName(elem.getAttribute(attrName));
        return dataDao;
    }

    private IGraphNode getNodeShape(Element elem, IGraphNode node) {
        node.setExportId(Integer.parseInt(elem.getAttribute(attrId)));
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

    private Parameter getParameter(Element elem) {
        Parameter param = new Parameter(
                elem.getAttribute(attrId),
                elem.getAttribute(attrNote),
                elem.getAttribute(attrValue),
                Parameter.Type.valueOf(elem.getAttribute(attrType))
        );
        return param;
    }

    private Token getToken(Element elem) {
        Token token = new Token(new Colour(elem.getAttribute(attrColour), ""));
        token.setValueStart(Double.parseDouble(elem.getAttribute(attrStart)));
        token.setValueMin(Double.parseDouble(elem.getAttribute(attrMin)));
        token.setValueMax(Double.parseDouble(elem.getAttribute(attrMax)));
        return token;
    }

    private Weight getWeight(Element elem) {
        Weight weight = new Weight(new Colour(elem.getAttribute(attrColour), ""));
        weight.setValue(elem.getAttribute(attrValue));
        return weight;
    }

    private synchronized int getExportId(IGraphNode node) {
        if (node.getExportId() == 0) {
            node.setExportId(exportId.get());
            exportId.set(exportId.get() + 1);
        }
        return node.getExportId();
    }

    public void exportXml(File file, GraphDao graphDao, DataDao dataDao) throws ParserConfigurationException, TransformerException, FileNotFoundException {

        Document dom;
        Element model, arcElements, placeElements, transitionElements, colourElements, parameterElements;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // instance of a DocumentBuilderFactory
        DocumentBuilder db = dbf.newDocumentBuilder(); // use factory to get an instance of document builder
        dom = db.newDocument(); // create instance of DOM

        synchronized (exportId) {
            exportId.set(0);

            parameterElements = dom.createElement(tagParameters);
            dataDao.getParameters().values().forEach(param -> {
                Element parameterElement = dom.createElement(tagParameter);
                parameterElement.setAttribute(attrId, param.getId());
                if (param.getNote() != null && !param.getNote().isEmpty()) {
                    parameterElement.setAttribute(attrNote, param.getNote());
                }
                parameterElement.setAttribute(attrType, param.getType().toString());
                parameterElement.setTextContent(param.getValue());
                
                parameterElements.appendChild(parameterElement);
            });

            colourElements = dom.createElement(tagColours);
            dataDao.getColours().forEach(colour -> {
                Element colourElement = dom.createElement(tagColour);
                colourElement.setAttribute(attrId, colour.getId());
                if (colour.getDescription() != null && !colour.getDescription().isEmpty()) {
                    colourElement.setAttribute(attrDescription, colour.getDescription());
                }
                
                colourElements.appendChild(colourElement);
            });

            arcElements = dom.createElement(tagArcs);
            dataDao.getArcs().forEach(a -> {
                DataArc data = (DataArc) a;

                Element arcElement = dom.createElement(tagArc);
                arcElement.setAttribute(attrType, data.getArcType().toString());
                arcElement.setAttribute(attrSource, data.getSource().getId());
                arcElement.setAttribute(attrTarget, data.getTarget().getId());
                if (data.getName() != null && !data.getName().isEmpty()) {
                    arcElement.setAttribute(attrName, data.getName());
                }
//            if (arc.getLabelText() != null && !arc.getLabelText().isEmpty()) {
//                e.setAttribute(tagAttrLabel, arc.getLabelText());
//            }
                if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                    arcElement.setAttribute(attrDescription, data.getDescription());
                }

//                Element source = dom.createElement(tagSource);
//                source.setAttribute(attrId, data.getSource().getId());
//                source.setTextContent(data.getSource().getName());
//                arcElement.appendChild(source);
//
//                Element target = dom.createElement(tagTarget);
//                target.setAttribute(attrId, data.getTarget().getId());
//                target.setTextContent(data.getTarget().getName());
//                arcElement.appendChild(target);

                Element weights = dom.createElement(tagWeights);
                data.getWeightMap().values().forEach(weight -> {
                    Element w = dom.createElement(tagWeight);
                    w.setAttribute(attrColour, weight.getColour().getId());
                    w.setAttribute(attrValue, weight.getValue());
                    weights.appendChild(w);
                });
                arcElement.appendChild(weights);

                Element visualsElement = dom.createElement(tagVisualisation);
                data.getShapes().forEach(c -> {
                    IGraphArc connection = (IGraphArc) c;
                    Element connectionElement = dom.createElement(tagConnection);
                    connectionElement.setAttribute(attrSource, String.valueOf(getExportId(connection.getSource())));
                    connectionElement.setAttribute(attrTarget, String.valueOf(getExportId(connection.getTarget())));
                    visualsElement.appendChild(connectionElement);
                });
                arcElement.appendChild(visualsElement);

                arcElements.appendChild(arcElement);
            });

            placeElements = dom.createElement(tagPlaces);
            dataDao.getPlaces().forEach(p -> {
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

                Element tokens = dom.createElement(tagTokens);
                data.getTokenMap().values().forEach(token -> {
                    Element t = dom.createElement(tagToken);
                    t.setAttribute(attrColour, token.getColour().getId());
                    t.setAttribute(attrStart, String.valueOf(token.getValueStart()));
                    t.setAttribute(attrMin, String.valueOf(token.getValueMin()));
                    t.setAttribute(attrMax, String.valueOf(token.getValueMax()));
                    tokens.appendChild(t);
                });
                placeElement.appendChild(tokens);

                Element visualsElement = dom.createElement(tagVisualisation);
                data.getShapes().forEach(e -> {

                    IGraphNode node = (IGraphNode) e;

                    Element nodeElement = dom.createElement(tagNode);
                    nodeElement.setAttribute(attrPosX, String.valueOf(node.getShape().getTranslateX()));
                    nodeElement.setAttribute(attrPosY, String.valueOf(node.getShape().getTranslateY()));
                    nodeElement.setAttribute(attrId, String.valueOf(getExportId(node)));

                    Element labelElement = dom.createElement(tagLabel);
                    labelElement.setAttribute(attrPosX, String.valueOf(node.getLabel().getTranslateX()));
                    labelElement.setAttribute(attrPosY, String.valueOf(node.getLabel().getShape().getTranslateY()));
                    labelElement.setTextContent(node.getLabel().getText());
                    nodeElement.appendChild(labelElement);

                    visualsElement.appendChild(nodeElement);
                });
                placeElement.appendChild(visualsElement);

                placeElements.appendChild(placeElement);
            });

            transitionElements = dom.createElement(tagTransitons);
            dataDao.getTransitions().forEach(t -> {
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

                Element visualsElement = dom.createElement(tagVisualisation);
                data.getShapes().forEach(e -> {

                    IGraphNode node = (IGraphNode) e;

                    Element nodeElement = dom.createElement(tagNode);
                    nodeElement.setAttribute(attrId, String.valueOf(getExportId(node)));
                    nodeElement.setAttribute(attrPosX, String.valueOf(node.getShape().getTranslateX()));
                    nodeElement.setAttribute(attrPosY, String.valueOf(node.getShape().getTranslateY()));

                    Element labelElement = dom.createElement(tagLabel);
                    labelElement.setAttribute(attrPosX, String.valueOf(node.getLabel().getTranslateX()));
                    labelElement.setAttribute(attrPosY, String.valueOf(node.getLabel().getShape().getTranslateY()));
                    nodeElement.appendChild(labelElement);

                    visualsElement.appendChild(nodeElement);
                });
                transitionElement.appendChild(visualsElement);

                transitionElements.appendChild(transitionElement);
            });

            model = dom.createElement(tagModel); // create the root element
            
            model.setAttribute(attrName, dataDao.getName());
            model.setAttribute(attrAuthor, dataDao.getAuthor());
            model.setAttribute(attrDescription, dataDao.getDescription());
            model.setAttribute(attrDateTime, LocalDateTime.now().format(DateTimeFormatter.ofPattern(formatDateTime)));
            model.setAttribute(attrNextPlaceId, String.valueOf(dataDao.getNextPlaceId()));
            model.setAttribute(attrNextTransitionId, String.valueOf(dataDao.getNextTransitionId()));
            
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
            tr.transform(new DOMSource(dom),
                    new StreamResult(new FileOutputStream(file)));
        }
    }
}
