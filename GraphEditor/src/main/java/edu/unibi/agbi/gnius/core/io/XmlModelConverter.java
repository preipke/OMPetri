/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.io;

import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.dao.GraphDao;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author PR
 */
@Component
public class XmlModelConverter
{
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
    private final String tagSource = "Source";
    private final String tagTarget = "Target";
    
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
    
    public void importXml(File file) {
        // ...
        String dateTime = "...";
        LocalDateTime simulationDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(formatDateTime));
//        setExportId(0);
        // ...
    }
    
    private int getExportId(IGraphNode node) {
        if (node.getExportId() == 0) {
            node.setExportId(exportId.get());
            exportId.set(exportId.get() + 1);
        }
        return exportId.get();
    }
    
    public void exportXml(File file, GraphDao graphDao, DataDao dataDao) throws ParserConfigurationException, TransformerException, FileNotFoundException {

        Document dom;
        Element model, arcs, placesElement, transitionsElement, colours, parameters;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // instance of a DocumentBuilderFactory
        DocumentBuilder db = dbf.newDocumentBuilder(); // use factory to get an instance of document builder
        dom = db.newDocument(); // create instance of DOM
        
        synchronized (exportId) {
            exportId.set(0);

            parameters = dom.createElement(tagParameters);
            dataDao.getParameters().values().forEach(param -> {
                Element parameterElement = dom.createElement(tagParameter);
                parameterElement.setAttribute(attrId, param.getId());
                if (param.getNote() != null && !param.getNote().isEmpty()) {
                    parameterElement.setAttribute(attrNote, param.getNote());
                }
                parameterElement.setAttribute(attrType, param.getType().toString());
                parameterElement.setTextContent(param.getValue());
                parameters.appendChild(parameterElement);
            });

            colours = dom.createElement(tagColours);
            dataDao.getColours().forEach(colour -> {
                Element colourElement = dom.createElement(tagColour);
                colourElement.setAttribute(attrId, colour.getId());
                if (colour.getDescription() != null && !colour.getDescription().isEmpty()) {
                    colourElement.setAttribute(attrDescription, colour.getDescription());
                }
                colours.appendChild(colourElement);
            });

            arcs = dom.createElement(tagArcs);
            dataDao.getArcs().forEach(a -> {
                DataArc data = (DataArc) a;

                Element arcElement = dom.createElement(tagArc);
                arcElement.setAttribute(attrId, data.getId());
                arcElement.setAttribute(attrType, data.getArcType().toString());
                if (data.getName() != null && !data.getName().isEmpty()) {
                    arcElement.setAttribute(attrName, data.getName());
                }
//            if (arc.getLabelText() != null && !arc.getLabelText().isEmpty()) {
//                e.setAttribute(tagAttrLabel, arc.getLabelText());
//            }
                if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                    arcElement.setAttribute(attrDescription, data.getDescription());
                }

                Element source = dom.createElement(tagSource);
                source.setAttribute(attrId, data.getSource().getId());
                source.setTextContent(data.getSource().getName());
                arcElement.appendChild(source);

                Element target = dom.createElement(tagTarget);
                target.setAttribute(attrId, data.getTarget().getId());
                target.setTextContent(data.getSource().getName());
                arcElement.appendChild(target);

                Element weights = dom.createElement(tagWeights);
                data.getWeightMap().values().forEach(weight -> {
                    Element w = dom.createElement(tagWeight);
                    w.setAttribute(attrColour, weight.getColour().getId());
                    w.setAttribute(attrValue, weight.getValue());
                    weights.appendChild(w);
                });
                arcElement.appendChild(weights);

                Element visualsElement = dom.createElement(tagVisualisation);
                data.getGraphElements().forEach(c -> {
                    IGraphArc connection = (IGraphArc) c;
                    Element connectionElement = dom.createElement(tagConnection);
                    connectionElement.setAttribute(attrSource, String.valueOf(getExportId(connection.getSource())));
                    connectionElement.setAttribute(attrTarget, String.valueOf(getExportId(connection.getTarget())));
                    visualsElement.appendChild(connectionElement);
                });
                arcElement.appendChild(visualsElement);

                arcs.appendChild(arcElement);
            });

            placesElement = dom.createElement(tagPlaces);
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
                data.getGraphElements().forEach(e -> {

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

                placesElement.appendChild(placeElement);
            });

            transitionsElement = dom.createElement(tagTransitons);
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
                data.getGraphElements().forEach(e -> {

                    IGraphNode node = (IGraphNode) e;

                    Element nodeElement = dom.createElement(tagNode);
                    nodeElement.setAttribute(attrId, String.valueOf(getExportId(node)));
                    nodeElement.setAttribute(attrPosX, String.valueOf(node.getShape().getTranslateX()));
                    nodeElement.setAttribute(attrPosY, String.valueOf(node.getShape().getTranslateY()));

                    Element labelElement = dom.createElement(tagLabel);
                    labelElement.setAttribute(attrPosX, String.valueOf(node.getLabel().getTranslateX()));
                    labelElement.setAttribute(attrPosY, String.valueOf(node.getLabel().getShape().getTranslateY()));
                    labelElement.setTextContent(node.getLabel().getText());
                    nodeElement.appendChild(labelElement);

                    visualsElement.appendChild(nodeElement);
                });
                transitionElement.appendChild(visualsElement);

                transitionsElement.appendChild(transitionElement);
            });

            model = dom.createElement(tagModel); // create the root element
            model.setAttribute(attrName, dataDao.getName());
            model.setAttribute(attrAuthor, dataDao.getAuthor());
            model.setAttribute(attrDescription, dataDao.getDescription());
            model.setAttribute(attrDateTime, LocalDateTime.now().format(DateTimeFormatter.ofPattern(formatDateTime)));
            model.appendChild(arcs);
            model.appendChild(placesElement);
            model.appendChild(transitionsElement);
            model.appendChild(colours);
            model.appendChild(parameters);
            dom.appendChild(model);

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
