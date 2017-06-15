/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.io;

import edu.unibi.agbi.gnius.core.model.entity.simulation.SimulationData;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.chart.XYChart.Data;
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
    private final String attrModel = "model";
    private final String attrName = "name";
    private final String attrTime = "time";
    private final String tagData = "Data";
    private final String tagElement = "Element";
    private final String tagElements = "Elements";
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

    public void exportXml(File file, List<SimulationData> simulationData) throws Exception {

        Document dom;
        NamedNodeMap attributes;
        Element simulations, simulation, elements, element, variables;

        String dateTime, model, author, id, name;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // instance of a DocumentBuilderFactory
        DocumentBuilder db = dbf.newDocumentBuilder(); // use factory to get an instance of document builder
        dom = db.newDocument(); // create instance of DOM
        simulations = dom.createElement(tagSimulations); // create the root element

        for (SimulationData data : simulationData) {
            
            simulation = null;
            elements = null; 
            element = null;
            variables = null;

            dateTime = data.getSimulation().getDateTime().format(DateTimeFormatter.ofPattern(formatDateTime));
            model = data.getSimulation().getModelName();
            author = data.getSimulation().getAuthorName();

            /**
             * check if simulation element exists
             */
            for (int i = 0; i < simulations.getChildNodes().getLength(); i++) {
                attributes = simulations.getChildNodes().item(i).getAttributes();
                if (attributes.getNamedItem(attrDateTime).getNodeValue().matches(dateTime)) {
                    if (attributes.getNamedItem(attrModel).getNodeValue().matches(model)) {
                        if (attributes.getNamedItem(attrAuthor).getNodeValue().matches(author)) {
                            simulation = (Element) simulations.getChildNodes().item(i);
                            elements = (Element) simulation.getElementsByTagName(tagElements).item(0);
                            break;
                        }
                    }
                }
            }

            if (simulation == null) {
                simulation = dom.createElement(tagSimulation);
                simulation.setAttribute(attrDateTime, dateTime);
                simulation.setAttribute(attrModel, model);
                simulation.setAttribute(attrAuthor, author);
                simulations.appendChild(simulation);
            }
            if (elements == null) {
                elements = dom.createElement(tagElements);
                simulation.appendChild(elements);
            }

            /**
             * check if element element exists
             */
            id = data.getElementId();
            name = data.getElementName();

            for (int i = 0; i < elements.getChildNodes().getLength(); i++) {
                attributes = elements.getChildNodes().item(i).getAttributes();
                if (attributes.getNamedItem(attrId).getNodeValue().matches(id)) {
                    element = (Element) elements.getChildNodes().item(i);
                    variables = (Element) element.getElementsByTagName(tagData).item(0);
                    break;
                }
            }

            if (element == null) {
                element = dom.createElement(tagElement);
                element.setAttribute(attrId, id);
                element.setAttribute(attrName, name);
                elements.appendChild(element);
            }
            if (variables == null) {
                variables = dom.createElement(tagData);
                element.appendChild(variables);
            }

            /**
             * parse data
             */
            final Element variable = dom.createElement(tagResults);
            variable.setAttribute(attrId, data.getVariable());
            variables.appendChild(variable);

            data.getSeries().getData().forEach(new Consumer<Data>() {
                @Override
                public void accept(Data d) {
                    Element datapoint = dom.createElement(tagValue);
                    datapoint.setAttribute(attrTime, d.getXValue().toString());
                    datapoint.setTextContent(d.getYValue().toString());
                    variable.appendChild(datapoint);
                }
            });
        }

        dom.appendChild(simulations);

        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dtdResultsData);
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // send DOM to file
        tr.transform(new DOMSource(dom),
                new StreamResult(new FileOutputStream(file)));
    }
}
