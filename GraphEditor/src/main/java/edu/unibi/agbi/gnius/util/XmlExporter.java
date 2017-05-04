/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.util;

import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.model.entity.simulation.SimulationLineChartData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.chart.XYChart.Data;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 *
 * @author PR
 */
@Component
public class XmlExporter
{
    @Value("${simulation.datetime.format}") private String simulationDateTimeFormat;
    
    public void exportXml(File file, List<SimulationLineChartData> simulationData) {
        
        Document dom;
        NamedNodeMap attributes;
        Element simulations, simulation = null, elements = null, element = null, elementdata = null;
        String dateTime, model, author, id, name;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            simulations = dom.createElement("simulations");
            
            for (SimulationLineChartData data : simulationData) {
                
                dateTime = data.getSimulation().getTime().format(DateTimeFormatter.ofPattern(simulationDateTimeFormat));
                model = data.getSimulation().getName();
                author = data.getSimulation().getAuthor();
                
                /**
                 * check if simulation element exists
                 */
                for (int i = 0; i < simulations.getChildNodes().getLength(); i++) {
                    attributes = simulations.getChildNodes().item(i).getAttributes();
                    if (attributes.getNamedItem("datetime").getNodeValue().matches(dateTime)) {
                        if (attributes.getNamedItem("model").getNodeValue().matches(dateTime)) {
                            if (attributes.getNamedItem("author").getNodeValue().matches(dateTime)) {
                                simulation = (Element) simulations.getChildNodes().item(i);
                                elements = (Element) simulation.getElementsByTagName("elements").item(0);
                                break;
                            }
                        }
                    }
                }
                
                if (simulation == null) {
                    simulation = dom.createElement("simulation");
                    simulation.setAttribute("datetime", dateTime);
                    simulation.setAttribute("model", model);
                    simulation.setAttribute("author", author);
                    simulations.appendChild(simulation);
                }
                if (elements == null) {
                    elements = dom.createElement("elements");
                    simulation.appendChild(elements);
                }
                
                /**
                 * check if element element exists
                 */
                id = data.getElement().getId();
                name = data.getElement().getName();
                
                for (int i = 0; i < elements.getChildNodes().getLength(); i++) {
                    attributes = elements.getChildNodes().item(i).getAttributes();
                    if (attributes.getNamedItem("id").getNodeValue().matches(id)) {
                        element = (Element) elements.getChildNodes().item(i);
                        elementdata = (Element) element.getElementsByTagName("elementdata").item(0);
                        break;
                    }
                }
                
                if (element == null) {
                    element = dom.createElement("element");
                    element.setAttribute("id", id);
                    element.setAttribute("name", name);
                    elements.appendChild(element);
                }
                if (elementdata == null) {
                    elementdata = dom.createElement("elementdata");
                    element.appendChild(elementdata);
                }
                
                simulations.appendChild(simulation);
            }

            dom.appendChild(simulations);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(file)));
            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }
}
