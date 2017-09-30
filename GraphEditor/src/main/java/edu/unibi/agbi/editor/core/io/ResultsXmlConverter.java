/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.io;

import edu.unibi.agbi.editor.core.data.dao.ModelDao;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataPlace;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataTransition;
import edu.unibi.agbi.editor.core.data.entity.result.ResultSet;
import edu.unibi.agbi.editor.core.data.entity.result.Simulation;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.entity.abstr.Element.Type;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import edu.unibi.agbi.petrinet.util.References;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

/**
 *
 * @author PR
 */
@Component
public class ResultsXmlConverter
{
    @Autowired private ModelService dataService;

    private final String formatDateTime = "yy-MM-dd HH:mm:ss";
    private final String dtdResultsData = "results.dtd";

    private final String attrAuthor = "author";
    private final String attrDateTime = "dateTime";
    private final String attrId = "id";
    private final String attrName = "name";
    private final String attrType = "type";
    private final String attrSubtype = "subtype";
    private final String attrShow = "showing";

    private final String tagData = "Data";
    private final String tagElement = "Element";
    private final String tagModel = "Model";
    private final String tagModels = "Models";
    private final String tagSimulation = "Simulation";
    private final String tagSimulations = "Simulations";
    private final String tagResults = "Results";
    private final String tagReferences = "References";
    private final String tagVariable = "Variable";
    
    // <editor-fold defaultstate="collapsed" desc="File import and related methods">

    public List<Simulation> importXml(File file) throws Exception {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
//        doc.getDocumentElement().normalize();

        NodeList nlm, nls;
        Element model, sim;
        Simulation simulation;
        References references;
        ModelDao dao;
        String[] variables;

        List<ModelDao> daos = new ArrayList();
        List<Simulation> simulations = new ArrayList();

        /**
         * Models.
         */
        nlm = doc.getElementsByTagName(tagModel);
        for (int i = 0; i < nlm.getLength(); i++) {
            if (nlm.item(i).getNodeType() == Node.ELEMENT_NODE) {
                model = (Element) nlm.item(i);

                dao = getDao(daos, model);

                /**
                 * Simulations.
                 */
                nls = model.getElementsByTagName(tagSimulation);
                for (int j = 0; j < nls.getLength(); j++) {
                    if (nls.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        sim = (Element) nls.item(j);

                        /**
                         * References.
                         */
                        references = getReferences(dao, sim.getElementsByTagName(tagElement));
                        references.addFilterReference("time", null); // time must be present in variables

                        variables = new String[references.getFilterToElementReferences().keySet().size()];
                        variables = references.getFilterToElementReferences().keySet().toArray(variables);

                        simulation = new Simulation(dao, variables, references);
                        simulation.setDateTime(LocalDateTime.parse(sim.getAttribute(attrDateTime), DateTimeFormatter.ofPattern(formatDateTime)));
                        simulations.add(simulation);

                        /**
                         * Results.
                         */
                        addResults(simulation, sim.getElementsByTagName(tagData));
                    }
                }
            }
        }
        return simulations;
    }

    private void addResults(Simulation simulation, final NodeList nld) {

        Element data;
        List<Object> values;

        for (int k = 0; k < nld.getLength(); k++) {
            if (nld.item(k).getNodeType() == Node.ELEMENT_NODE) {
                data = (Element) nld.item(k);
                values = simulation.getData(data.getAttribute(attrId));
                for (String value : data.getTextContent().split(",")) {
                    values.add(Double.parseDouble(value));
                }
            }
        }
    }

    private IElement getElement(ModelDao dao, final Element elem) throws Exception {

        IElement element;
        Type type;
        String id, name, subtype;

        id = elem.getAttribute(attrId);
        name = elem.getAttribute(attrName);
        type = Type.valueOf(elem.getAttribute(attrType));
        subtype = elem.getAttribute(attrSubtype);

        element = dao.getModel().getElement(id);
        if (element == null) {
            switch (type) {
                case ARC:
                    element = new DataArc(id, Arc.Type.NORMAL);
                    element.setName(name);
//                    dao.getModel().add((INode) element);
                    break;
                case PLACE:
                    element = new DataPlace(id, Place.Type.valueOf(subtype));
                    element.setName(name);
                    dao.getModel().add(element);
                    break;
                case TRANSITION:
                    element = new DataTransition(id, Transition.Type.valueOf(subtype));
                    element.setName(name);
                    dao.getModel().add(element);
                    break;
            }
        }

        return element;
    }

    private References getReferences(ModelDao dao, final NodeList nle) throws IOException {

        NodeList nlr;
        Element elem, var;
        References references = new References();
        IElement element;

        for (int k = 0; k < nle.getLength(); k++) {
            if (nle.item(k).getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) nle.item(k);

                try {
                    element = getElement(dao, elem);
                } catch (Exception ex) {
                    throw new IOException(ex.getMessage());
                }

                nlr = elem.getElementsByTagName(tagVariable);
                for (int l = 0; l < nlr.getLength(); l++) {
                    if (nlr.item(l).getNodeType() == Node.ELEMENT_NODE) {
                        var = (Element) nlr.item(l);

                        references.addElementReference(element, var.getAttribute(attrId));
                        if (element.getElementType() == Type.ARC) {
                            continue; // filter variable is meant to reference node only
                        }
                        references.addFilterReference(var.getAttribute(attrId), element);
                    }
                }
            }
        }

        return references;
    }

    private ModelDao getDao(List<ModelDao> daos, final Element elem) {

        String author, id, name;
        ModelDao dao = null;

        author = elem.getAttribute(attrAuthor);
        id = elem.getAttribute(attrId);
        name = elem.getAttribute(attrName);

        for (ModelDao d : dataService.getDaos()) {
            if (d.getModelId().contentEquals(id)) {
                dao = d;
                break;
            }
        }

        if (dao == null) {
            for (ModelDao d : daos) {
                if (d.getModelId().contentEquals(id)) {
                    dao = d;
                    break;
                }
            }
        }

        if (dao == null) {
            dao = new ModelDao();
            dao.setAuthor(author);
            dao.setModelId(id);
            dao.setModelName(name);
        }

        return dao;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="File export and related methods">

    public void ExportSimulationResults(File file, List<Simulation> simulationResults) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
        DocumentBuilder db = dbf.newDocumentBuilder(); 
        Document dom;
        
        NamedNodeMap attributes;
        Element models, simulation, elements, element, results;

        dom = db.newDocument();
        models = dom.createElement(tagModels);
        
        for (Simulation simulationResult : simulationResults) {

            simulation = getSimulationElement(simulationResult, dom, models);
            elements = (Element) simulation.getElementsByTagName(tagReferences).item(0);
            results = (Element) simulation.getElementsByTagName(tagResults).item(0);
            
            for (IElement elem : simulationResult.getElements()) {

                element = null;
                
                /**
                 * Check if element exists.
                 */
                for (int i = 0; i < elements.getChildNodes().getLength(); i++) {
                    attributes = elements.getChildNodes().item(i).getAttributes();
                    if (attributes.getNamedItem(attrId).getNodeValue().matches(elem.getId())) {
                        element = (Element) elements.getChildNodes().item(i);
                        break;
                    }
                }
                if (element == null) {
                    element = getElementElement(dom.createElement(tagElement), elem);
                    elements.appendChild(element);
                }
                
                for (String variable : simulationResult.getElementFilter(elem)) {

                    element.appendChild(getVariableElement(dom, variable, false));
                    if (elem.getElementType() == Type.ARC) {
                        continue; // skip arcs, data is stored in places, only need reference
                    }
                    results.appendChild(getDataElement(dom, variable, simulationResult.getData(variable)));
                }
            }
        }
        dom.appendChild(models);
        
        sendToFile(dom, file);
    }

    public void ExportResultSets(File file, List<ResultSet> resultSets) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder(); 
        Document dom;
        
        NamedNodeMap attributes;
        Element models, simulation, elements, element, results;

        dom = db.newDocument();
        models = dom.createElement(tagModels);

        for (ResultSet resultSet : resultSets) {

            simulation = getSimulationElement(resultSet.getSimulation(), dom, models);
            elements = (Element) simulation.getElementsByTagName(tagReferences).item(0);
            results = (Element) simulation.getElementsByTagName(tagResults).item(0);
            element = null;

            /**
             * Check if element exists.
             */
            for (int i = 0; i < elements.getChildNodes().getLength(); i++) {
                attributes = elements.getChildNodes().item(i).getAttributes();
                if (attributes.getNamedItem(attrId).getNodeValue().matches(resultSet.getElement().getId())) {
                    element = (Element) elements.getChildNodes().item(i);
                    break;
                }
            }
            if (element == null) {
                element = getElementElement(dom.createElement(tagElement), resultSet.getElement());
                elements.appendChild(element);
            }

            /**
             * Append data.
             */
            element.appendChild(getVariableElement(dom, resultSet.getVariable(), resultSet.isShown()));
            results.appendChild(getDataElement(dom, resultSet.getVariable(), resultSet.getData()));
        }
        dom.appendChild(models);
        
        sendToFile(dom, file);
    }
    
    private void sendToFile(Document dom, File file) throws Exception {
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

    private Element getSimulationElement(Simulation simulationResult, Document dom, final Element models) {

        Element model, simulations, simulation, elements, results;
        NamedNodeMap attributes;
        String modelAuthor, modelName, modelId, simulationDateTime;

        modelAuthor = simulationResult.getDao().getAuthor();
        modelId = simulationResult.getDao().getModelId();
        modelName = simulationResult.getDao().getModelName();
        simulationDateTime = simulationResult.getDateTime().format(DateTimeFormatter.ofPattern(formatDateTime));

        model = null;
        simulations = null;
        simulation = null;
        elements = null;
        results = null;

        /**
         * Check if model exists.
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
            models.appendChild(model);
        }
        if (simulations == null) {
            simulations = dom.createElement(tagSimulations);
            model.appendChild(simulations);
        }

        /**
         * Check if simulation exists.
         */
        for (int i = 0; i < simulations.getChildNodes().getLength(); i++) {
            attributes = simulations.getChildNodes().item(i).getAttributes();
            if (attributes.getNamedItem(attrDateTime).getNodeValue().matches(simulationDateTime)) {
                simulation = (Element) simulations.getChildNodes().item(i);
                break;
            }
        }
        if (simulation == null) {
            simulation = dom.createElement(tagSimulation);
            simulation.setAttribute(attrDateTime, simulationDateTime);
            simulations.appendChild(simulation);
        }
        if (elements == null) {
            elements = dom.createElement(tagReferences);
            simulation.appendChild(elements);
        }
        if (results == null) {
            results = dom.createElement(tagResults);
            results.appendChild(getDataElement(dom, "time", simulationResult.getTimeData())); // append time data
            simulation.appendChild(results);
        }

        return simulation;
    }

    private Element getElementElement(final Element elem, IElement element) {

        elem.setAttribute(attrId, element.getId());
        elem.setAttribute(attrName, element.getName());
        elem.setAttribute(attrType, element.getElementType().toString());

        switch (element.getElementType()) {
            case PLACE:
                elem.setAttribute(attrSubtype, ((Place) element).getPlaceType().toString());
                break;
            case TRANSITION:
                elem.setAttribute(attrSubtype, ((Transition) element).getTransitionType().toString());
                break;
        }

        return elem;
    }

    private Element getVariableElement(Document dom, String variableId, boolean show) {

        final Element elem;

        elem = dom.createElement(tagVariable);
        elem.setAttribute(attrId, variableId);
        elem.setAttribute(attrShow, Boolean.toString(show));

        return elem;
    }

    private Element getDataElement(Document dom, String variableId, List<Object> data) {

        final Element elem;
        String dataString;

        elem = dom.createElement(tagData);
        elem.setAttribute(attrId, variableId);

        dataString = "";
        for (Object obj : data) {
            dataString += obj.toString() + ",";
        }
        dataString = dataString.substring(0, dataString.length() - 1);

        elem.setTextContent(dataString);

        return elem;
    }
    
    // </editor-fold>
}
