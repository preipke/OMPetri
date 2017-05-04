/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.util;

import edu.unibi.agbi.gnius.core.model.entity.simulation.SimulationLineChartData;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.chart.XYChart.Data;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

/**
 *
 * @author PR
 */
@Component
public class XmlExporter
{
    @Value("${simulation.datetime.format}") private String simulationDateTimeFormat;
    @Value("${xml.results.data.dtd}") private String simulationDataDtd;
    
    private final String resultsRoot = "simulations";
    private final String resultsSimulation = "simulation";
    private final String resultsSimulationAttrDateTime = "datetime";
    private final String resultsSimulationAttrModelName = "model";
    private final String resultsSimulationAttrAuthor = "author";
    private final String resultsSimulationElements = "elements";
    private final String resultsElement = "element";
    private final String resultsElementAttrId = "id";
    private final String resultsElementAttrName = "name";
    private final String resultsElementData = "data";
    private final String resultsVariable = "variable";
    private final String resultsVariableAttrId = "id";
    private final String resultsValue = "value";
    private final String resultsValueAttrX = "x";
    private final String resultsValueAttrY = "y";

    public void exportXml(File file, List<SimulationLineChartData> simulationData) throws Exception {

        Document dom;
        NamedNodeMap attributes;
        Element simulations, simulation;
        Element elements, element;
        Element variables;

        String dateTime, model, author, id, name;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // instance of a DocumentBuilderFactory
        DocumentBuilder db = dbf.newDocumentBuilder(); // use factory to get an instance of document builder
        dom = db.newDocument(); // create instance of DOM
        simulations = dom.createElement(resultsRoot); // create the root element

        for (SimulationLineChartData data : simulationData) {
            
            simulation = null;
            elements = null; 
            element = null;
            variables = null;

            dateTime = data.getSimulation().getTime().format(DateTimeFormatter.ofPattern(simulationDateTimeFormat));
            model = data.getSimulation().getModelName();
            author = data.getSimulation().getAuthor();

            /**
             * check if simulation element exists
             */
            for (int i = 0; i < simulations.getChildNodes().getLength(); i++) {
                attributes = simulations.getChildNodes().item(i).getAttributes();
                if (attributes.getNamedItem(resultsSimulationAttrDateTime).getNodeValue().matches(dateTime)) {
                    if (attributes.getNamedItem(resultsSimulationAttrModelName).getNodeValue().matches(model)) {
                        if (attributes.getNamedItem(resultsSimulationAttrAuthor).getNodeValue().matches(author)) {
                            simulation = (Element) simulations.getChildNodes().item(i);
                            elements = (Element) simulation.getElementsByTagName(resultsSimulationElements).item(0);
                            break;
                        }
                    }
                }
            }

            if (simulation == null) {
                simulation = dom.createElement(resultsSimulation);
                simulation.setAttribute(resultsSimulationAttrDateTime, dateTime);
                simulation.setAttribute(resultsSimulationAttrModelName, model);
                simulation.setAttribute(resultsSimulationAttrAuthor, author);
                simulations.appendChild(simulation);
            }
            if (elements == null) {
                elements = dom.createElement(resultsSimulationElements);
                simulation.appendChild(elements);
            }

            /**
             * check if element element exists
             */
            id = data.getElement().getId();
            name = data.getElement().getName();

            for (int i = 0; i < elements.getChildNodes().getLength(); i++) {
                attributes = elements.getChildNodes().item(i).getAttributes();
                if (attributes.getNamedItem(resultsElementAttrId).getNodeValue().matches(id)) {
                    element = (Element) elements.getChildNodes().item(i);
                    variables = (Element) element.getElementsByTagName(resultsElementData).item(0);
                    break;
                }
            }

            if (element == null) {
                element = dom.createElement(resultsElement);
                element.setAttribute(resultsElementAttrId, id);
                element.setAttribute(resultsElementAttrName, name);
                elements.appendChild(element);
            }
            if (variables == null) {
                variables = dom.createElement(resultsElementData);
                element.appendChild(variables);
            }

            /**
             * parse data
             */
            final Element variable = dom.createElement(resultsVariable);
            variable.setAttribute(resultsVariableAttrId, data.getVariable());
            variables.appendChild(variable);

            data.getSeries().getData().forEach(new Consumer<Data>() {
                @Override
                public void accept(Data d) {
                    Element datapoint = dom.createElement(resultsValue);
                    datapoint.setAttribute(resultsValueAttrX, d.getXValue().toString());
                    datapoint.setAttribute(resultsValueAttrY, d.getYValue().toString());
                    variable.appendChild(datapoint);
                }
            });
        }

        dom.appendChild(simulations);

        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, simulationDataDtd);
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // send DOM to file
        tr.transform(new DOMSource(dom),
                new StreamResult(new FileOutputStream(file)));
    }
}
