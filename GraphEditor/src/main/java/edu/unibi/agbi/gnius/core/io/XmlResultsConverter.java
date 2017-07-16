/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.io;

import edu.unibi.agbi.gnius.core.model.entity.result.ResultSet;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

/**
 *
 * @author PR
 */
@Component
public class XmlResultsConverter
{
    private final String formatDateTime = "yy-MM-dd HH:mm:ss";
    private final String dtdResultsData = "results.dtd";
    
    private final String attrAuthor = "author";
    private final String attrDateTime = "dateTime";
    private final String attrId = "id";
    private final String attrName = "name";
    private final String attrTime = "time";
    
    private final String tagData = "Data";
    private final String tagElement = "Element";
    private final String tagElements = "Elements";
    private final String tagModel = "Model";
    private final String tagModels = "Models";
    private final String tagSimulations = "Simulations";
    private final String tagSimulation = "Simulation";
    private final String tagResults = "Variable";
    private final String tagValue = "Value";
    
    public void importXml(File file) {
        // ...
        String dateTime = "...";
        LocalDateTime simulationDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(formatDateTime));
        // ...
    }

    public void exportXml(File file, List<ResultSet> resultSets) throws Exception {

        Document dom;
        NamedNodeMap attributes;
        Element models, model, simulations, simulation, elements, element, data;

        String modelAuthor, modelName, modelId, simulationDateTime, elementId, elementName, variableName;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // instance of a DocumentBuilderFactory
        DocumentBuilder db = dbf.newDocumentBuilder(); // use factory to get an instance of document builder
        dom = db.newDocument(); // create instance of DOM
        
        models = dom.createElement(tagModels); // root element
        
        for (ResultSet resultSet : resultSets) {
            
            model = null;
            simulations = null;
            simulation = null;
            elements = null; 
            element = null;
            data = null;

            modelAuthor = resultSet.getSimulation().getDao().getAuthor();
            modelId = resultSet.getSimulation().getDao().getModelId();
            modelName = resultSet.getSimulation().getDao().getModelName();
            simulationDateTime = resultSet.getSimulation().getDateTime().format(DateTimeFormatter.ofPattern(formatDateTime));
            elementId = resultSet.getElement().getId();
            elementName = resultSet.getElement().getName();
            variableName = resultSet.getVariable();
            
            /**
             * check if model element exists
             */
            for (int i = 0; i < models.getChildNodes().getLength(); i++) {
                attributes = models.getChildNodes().item(i).getAttributes();
                if (attributes.getNamedItem(attrName).getNodeValue().matches(modelName)) {
                    if (attributes.getNamedItem(attrAuthor).getNodeValue().matches(modelAuthor)) {
                        model = (Element) models.getChildNodes().item(i);
                        simulations = (Element) models.getElementsByTagName(tagSimulations).item(0);
                        break;
                    }
                }
            }
            if (model == null) {
                model = dom.createElement(tagModel);
                model.setAttribute(attrAuthor, modelAuthor);
                model.setAttribute(attrId, modelId);
                model.setAttribute(attrName, modelName);
            }
            if (simulations == null) {
                simulations = dom.createElement(tagSimulations);
                model.appendChild(simulations);
            }

            /**
             * check if simulation element exists
             */
            for (int i = 0; i < simulations.getChildNodes().getLength(); i++) {
                attributes = simulations.getChildNodes().item(i).getAttributes();
                if (attributes.getNamedItem(attrDateTime).getNodeValue().matches(simulationDateTime)) {
                    simulation = (Element) simulations.getChildNodes().item(i);
                    elements = (Element) simulation.getElementsByTagName(tagElements).item(0);
                    break;
                }
            }
            if (simulation == null) {
                simulation = dom.createElement(tagSimulation);
                simulation.setAttribute(attrDateTime, simulationDateTime);
                simulations.appendChild(simulation);
            }
            if (elements == null) {
                elements = dom.createElement(tagElements);
                simulation.appendChild(elements);
            }

            /**
             * check if element element exists
             */

            for (int i = 0; i < elements.getChildNodes().getLength(); i++) {
                attributes = elements.getChildNodes().item(i).getAttributes();
                if (attributes.getNamedItem(attrId).getNodeValue().matches(elementId)) {
                    element = (Element) elements.getChildNodes().item(i);
                    data = (Element) element.getElementsByTagName(tagData).item(0);
                    break;
                }
            }

            if (element == null) {
                element = dom.createElement(tagElement);
                element.setAttribute(attrId, elementId);
                element.setAttribute(attrName, elementName);
                elements.appendChild(element);
            }
            if (data == null) {
                data = dom.createElement(tagData);
                element.appendChild(data);
            }

            /**
             * parse data
             */
            final Element variable = dom.createElement(tagResults);
            variable.setAttribute(attrId, variableName);
            data.appendChild(variable);
            
            List<Object> timePoints = resultSet.getSimulation().getTimeData();
            List<Object> dataPoints = resultSet.getData();
            Element datapoint;
            
            for (int i = 0; i < timePoints.size(); i++) {
                datapoint = dom.createElement(tagValue);
                datapoint.setAttribute(attrTime, timePoints.get(i).toString());
                datapoint.setTextContent(dataPoints.get(i).toString());
                variable.appendChild(datapoint);
            }
        }

        dom.appendChild(models);

        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//        tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dtdResultsData);
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // send DOM to file
        tr.transform(new DOMSource(dom),
                new StreamResult(new FileOutputStream(file)));
    }
}
