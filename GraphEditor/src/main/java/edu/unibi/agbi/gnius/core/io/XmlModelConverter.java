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
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
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
    
    private final String tagAttrAuthor = "max";
    private final String tagAttrColour = "colour";
    private final String tagAttrDateTime = "dateTime";
    private final String tagAttrDescription = "description";
    private final String tagAttrId = "id";
    private final String tagAttrLabel = "label";
    private final String tagAttrMax = "max";
    private final String tagAttrMin = "min";
    private final String tagAttrName = "name";
    private final String tagAttrNote = "note";
    private final String tagAttrStart = "start";
    private final String tagAttrType = "type";
    private final String tagAttrUnit = "unit";
    private final String tagAttrValue = "value";

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
    
    private final String tagModelVis = "Visualisation";
    private final String tagModelVisConnections = "Connections";
    private final String tagModelVisConnectionsElem = "Connection";
    private final String tagModelVisNodes = "Nodes";
    private final String tagModelVisNodesElem = "Node";
    
    public void importXml(File file) {
        // ...
        String dateTime = "...";
        LocalDateTime simulationDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(formatDateTime));
        // ...
    }
    
    public void exportXml(File file, GraphDao graphDao, DataDao dataDao) throws Exception {
        
        dataDao.getArcs();
        dataDao.getPlaces();
        dataDao.getTransitions();
        
        dataDao.getColours();
        dataDao.getParameters();
        
        graphDao.getConnections();
        graphDao.getNodes();

        Document dom;
        Element model, arcs, places, transitions, colours, parameters, visual, connections, nodes;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // instance of a DocumentBuilderFactory
        DocumentBuilder db = dbf.newDocumentBuilder(); // use factory to get an instance of document builder
        dom = db.newDocument(); // create instance of DOM
        
        parameters = dom.createElement(tagParameters);
        dataDao.getParameters().values().forEach(param -> {
            Element parameterElement = dom.createElement(tagParameter);
            parameterElement.setAttribute(tagAttrId, param.getId());
            if (param.getNote() != null && !param.getNote().isEmpty()) {
                parameterElement.setAttribute(tagAttrNote, param.getNote());
            }
            parameterElement.setAttribute(tagAttrType, param.getType().toString());
            parameterElement.setTextContent(param.getValue());
            parameters.appendChild(parameterElement);
        });
        
        colours = dom.createElement(tagColours);
        dataDao.getColours().forEach(colour -> {
            Element colourElement = dom.createElement(tagColour);
            colourElement.setAttribute(tagAttrId, colour.getId());
            if (colour.getDescription() != null && !colour.getDescription().isEmpty()) {
                colourElement.setAttribute(tagAttrDescription, colour.getDescription());
            }
            colours.appendChild(colourElement);
        });
        
        
        arcs = dom.createElement(tagArcs);
        dataDao.getArcs().forEach(a -> {
            DataArc arc = (DataArc) a;
            
            Element arcElement = dom.createElement(tagArc);
            arcElement.setAttribute(tagAttrId, arc.getId());
            arcElement.setAttribute(tagAttrType,arc.getArcType().toString());
            if (arc.getName() != null && !arc.getName().isEmpty()) {
                arcElement.setAttribute(tagAttrName, arc.getName());
            }
//            if (arc.getLabelText() != null && !arc.getLabelText().isEmpty()) {
//                e.setAttribute(tagAttrLabel, arc.getLabelText());
//            }
            if (arc.getDescription() != null && !arc.getDescription().isEmpty()) {
                arcElement.setAttribute(tagAttrDescription, arc.getDescription());
            }
            
            Element source = dom.createElement(tagSource);
            source.setAttribute(tagAttrId, arc.getSource().getId());
            source.setTextContent(arc.getSource().getName());
            arcElement.appendChild(source);
            
            Element target = dom.createElement(tagTarget);
            target.setAttribute(tagAttrId, arc.getTarget().getId());
            target.setTextContent(arc.getSource().getName());
            arcElement.appendChild(target);
            
            Element weights = dom.createElement(tagWeights);
            arc.getWeightMap().values().forEach(weight -> {
                Element w = dom.createElement(tagWeight);
                w.setAttribute(tagAttrColour, weight.getColour().getId());
                w.setAttribute(tagAttrValue, weight.getValue());
                weights.appendChild(w);
            });
            arcElement.appendChild(weights);
            
            arcs.appendChild(arcElement);
        });
        
        
        places = dom.createElement(tagPlaces);
        dataDao.getPlaces().forEach(p -> {
            DataPlace place = (DataPlace) p;
            
            Element placeElement = dom.createElement(tagPlace);
            placeElement.setAttribute(tagAttrId, place.getId());
            placeElement.setAttribute(tagAttrType,place.getPlaceType().toString());
            if (place.getName() != null && !place.getName().isEmpty()) {
                placeElement.setAttribute(tagAttrName, place.getName());
            }
            if (place.getLabelText() != null && !place.getLabelText().isEmpty()) {
                placeElement.setAttribute(tagAttrLabel, place.getLabelText());
            }
            if (place.getDescription() != null && !place.getDescription().isEmpty()) {
                placeElement.setAttribute(tagAttrDescription, place.getDescription());
            }
            
            Element tokens = dom.createElement(tagTokens);
            place.getTokenMap().values().forEach(token -> {
                Element t = dom.createElement(tagToken);
                t.setAttribute(tagAttrColour, token.getColour().getId());
                t.setAttribute(tagAttrStart, String.valueOf(token.getValueStart()));
                t.setAttribute(tagAttrMin, String.valueOf(token.getValueMin()));
                t.setAttribute(tagAttrMax, String.valueOf(token.getValueMax()));
                tokens.appendChild(t);
            });
            placeElement.appendChild(tokens);
            
            places.appendChild(placeElement);
        });
        
        
        transitions = dom.createElement(tagTransitons);
        dataDao.getTransitions().forEach(t -> {
            DataTransition transition = (DataTransition) t;
            
            Element transitionElement = dom.createElement(tagTransiton);
            transitionElement.setAttribute(tagAttrId, transition.getId());
            transitionElement.setAttribute(tagAttrType,transition.getTransitionType().toString());
            if (transition.getName() != null && !transition.getName().isEmpty()) {
                transitionElement.setAttribute(tagAttrName, transition.getName());
            }
            if (transition.getLabelText() != null && !transition.getLabelText().isEmpty()) {
                transitionElement.setAttribute(tagAttrLabel, transition.getLabelText());
            }
            if (transition.getDescription() != null && !transition.getDescription().isEmpty()) {
                transitionElement.setAttribute(tagAttrDescription, transition.getDescription());
            }
            
            Element f = dom.createElement(tagFunction);
            if (!transition.getFunction().getUnit().isEmpty()) {
                f.setAttribute(tagAttrUnit, transition.getFunction().getUnit());
            }
            f.setTextContent(transition.getFunction().toString());
            transitionElement.appendChild(f);
            
            transitions.appendChild(transitionElement);
        });
        
        model = dom.createElement(tagModel); // create the root element
        model.setAttribute(tagAttrName, dataDao.getName());
        model.setAttribute(tagAttrAuthor, dataDao.getAuthor());
        model.setAttribute(tagAttrDescription, dataDao.getDescription());
        model.setAttribute(tagAttrDateTime, LocalDateTime.now().format(DateTimeFormatter.ofPattern(formatDateTime)));
        model.appendChild(arcs);
        model.appendChild(places);
        model.appendChild(transitions);
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
