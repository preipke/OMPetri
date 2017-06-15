/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.exception.ParameterServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.util.FunctionBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class ParameterService
{
    @Autowired private DataService dataService;
    @Autowired private FunctionBuilder functionBuilder;

    @Value("${regex.param.ident.flowIn.actual}") private String regexParamPlaceFlowInNow;
    @Value("${regex.param.ident.flowIn.total}") private String regexParamPlaceFlowInTotal;
    @Value("${regex.param.ident.flowOut.actual}") private String regexParamPlaceFlowOutNow;
    @Value("${regex.param.ident.flowOut.total}") private String regexParamPlaceFlowOutTotal;
    @Value("${regex.param.ident.token}") private String regexParamToken;
    @Value("${regex.param.ident.speed}") private String regexTransitionSpeed;

    /**
     * Attempts to add a parameter.
     *
     * @param element
     * @param param
     * @throws ParameterServiceException
     */
    public void add(Parameter param, IElement element) throws ParameterServiceException {
        if (Parameter.Type.GLOBAL == param.getType()) {
            add(param);
        } else {
            if (element != null) {
                if (dataService.getModel().containsAndNotEqual(param)) {
                    throw new ParameterServiceException("Conflict! Another parameter has already been stored using the same ID!");
                }
                dataService.getModel().add(param);
                element.getRelatedParameterIds().add(param.getId());
            } else {
                throw new ParameterServiceException("A reference element is required for storing non global parameters!");
            }
        }
    }

    /**
     * Attempts to add a parameter to the dao.
     *
     * @param param
     * @throws ParameterServiceException
     */
    private void add(Parameter param) throws ParameterServiceException {
        if (Parameter.Type.GLOBAL != param.getType()) {
            throw new ParameterServiceException("Wrong method for adding non global parameters! Reference element required.");
        }
        if (dataService.getModel().containsAndNotEqual(param)) {
            throw new ParameterServiceException("Conflict! Another parameter has already been stored using the same ID!");
        }
        dataService.getModel().add(param);
    }

    /**
     * Sets the function of a transition. Replaces the existing function.
     * Ensures the integrity of parameter references.
     *
     * @param transition
     * @param functionString
     * @throws ParameterServiceException
     */
    public void setTransitionFunction(DataTransition transition, String functionString) throws Exception {
        clearTransitionFunctionParameterReferences(transition);
        try {
            transition.setFunction(functionBuilder.build(functionString));
        } finally {
            setTransitionFunctionParameterReferences(transition);
//            removeUnusedReferencingParameter();
        }
    }

    /**
     * Adds references to a transition for all parameters used in its function.
     *
     * @param transition
     */
    private void setTransitionFunctionParameterReferences(DataTransition transition) {
        transition.getFunction().getParameterIds().forEach(id -> {
            getParameter(id)
                    .getUsingElements()
                    .add(transition);
        });
    }

    /**
     * Removes references to a transition for all parameters used in its
     * function.
     *
     * @param transition
     */
    private void clearTransitionFunctionParameterReferences(DataTransition transition) {
        transition.getFunction().getParameterIds().forEach(id -> {
            getParameter(id)
                    .getUsingElements()
                    .remove(transition);
        });
    }

    /**
     * Removes any unused parameters that are references to an element.
     */
    private void removeUnusedReferencingParameter() {
        List<Parameter> paramsUnused = new ArrayList();
        dataService.getModel().getParameters().forEach(param -> {
            if (param.getType() == Parameter.Type.REFERENCE) {
                if (param.getUsingElements().isEmpty()) {
                    dataService.getModel()
                            .getElement(param.getRelatedElementId()).getRelatedParameterIds()
                            .remove(param.getId());
                    paramsUnused.add(param);
                }
            }
        });
        paramsUnused.forEach(param -> dataService.getModel().remove(param));
    }

    /**
     * Creates referencing parameters for an element.
     *
     * @param element
     * @throws ParameterServiceException
     */
    public void CreateReferencingParameters(IElement element) throws ParameterServiceException {
        switch (element.getElementType()) {
            case PLACE:
                CreateReferencingParameters((DataPlace) element);
                break;

            case TRANSITION:
                CreateReferencingParameters((DataTransition) element);
                break;

            default:
                throw new ParameterServiceException("There are no referencing parameters to create for elements of type '" + element.getElementType().toString() + "'");
        }
    }

    /**
     * Creates referencing parameters for a place.
     *
     * @param place
     * @throws ParameterServiceException
     */
    private void CreateReferencingParameters(DataPlace place) throws ParameterServiceException {

        String ident, value;
        int index;

        index = 1;
        for (IArc arc : place.getArcsIn()) {

            ident = arc.getTarget().getId() + arc.getSource().getId();
            value = "'" + arc.getTarget().getId() + "'.tokenFlow.inflow[" + index + "]";

            CreateReferencingParameter(ident + "_total", value, arc);
            CreateReferencingParameter(ident + "_now", "der(" + value + ")", arc);

            index++;
        }

        index = 1;
        for (IArc arc : place.getArcsOut()) {

            ident = arc.getSource().getId() + arc.getTarget().getId();
            value = "'" + arc.getSource().getId() + "'.tokenFlow.outflow[" + index + "]";

            CreateReferencingParameter(ident + "_total", value, arc);
            CreateReferencingParameter(ident + "_now", "der(" + value + ")", arc);

            index++;
        }

        CreateReferencingParameter(place.getId(), "'" + place.getId() + "'.t", place);
    }

    /**
     * Creates referencing parameters for a transition.
     *
     * @param transition
     * @throws ParameterServiceException
     */
    private void CreateReferencingParameters(DataTransition transition) throws ParameterServiceException {
        CreateReferencingParameter(transition.getId(), "'" + transition.getId() + "'.actualSpeed", transition);
    }

    /**
     * Creates a parameter that references an element.
     *
     * @param id
     * @param value
     * @param element
     * @throws ParameterServiceException
     */
    private Parameter CreateReferencingParameter(String id, String value, IElement element) throws ParameterServiceException {
        Parameter param = new Parameter(id, "", value, Parameter.Type.REFERENCE, element.getId());
        add(param, element);
        return param;
    }

    /**
     * Gets all global parameters. List is sorted by parameter ids (natural
     * string order).
     *
     * @return
     */
    public List<Parameter> getGlobalParameters() {
        List<Parameter> parameters = new ArrayList();
        for (Parameter param : dataService.getModel().getParameters()) {
            if (param.getType() == Parameter.Type.GLOBAL) {
                parameters.add(param);
            }
        }
        parameters.sort(Comparator.comparing(Parameter::getId));
        return parameters;
    }

    /**
     * Gets all parameters usable for an element. List is sorted by parameter
     * ids (natural string order).
     *
     * @param elem
     * @return
     */
    public List<Parameter> getLocalParameters(IDataElement elem) {
        List<Parameter> parameters = new ArrayList();
        Parameter param;
        if (elem != null) {
            for (String id : elem.getRelatedParameterIds()) {
                param = getParameter(id);
                if (param.getType() == Parameter.Type.LOCAL) {
                    parameters.add(param);
                }
            }
        } else {
            dataService.getModel().getParameters().forEach(p -> {
                if (p.getType() == Parameter.Type.LOCAL) {
                    parameters.add(p);
                }
            });
        }
        parameters.sort(Comparator.comparing(Parameter::getId));
        return parameters;
    }

    /**
     * Gets the parameter with the given id.
     *
     * @param id
     * @return
     */
    public Parameter getParameter(String id) {
        return dataService.getModel().getParameter(id);
    }

    /**
     * Gets the list of currently used parameter ids.
     *
     * @return
     */
    public Set<String> getParameterIds() {
        return dataService.getModel().getParameterIds();
    }
    
    /**
     * Attempts to get a referencing parameter for a given candidate identifier.
     * 
     * @param candidate
     * @return
     * @throws ParameterServiceException 
     */
    private Parameter getReferencingParameter(String candidate) throws ParameterServiceException {

        IElement element;

        if (candidate.matches(regexParamPlaceFlowInNow)) {

            int index = candidate.indexOf("P");

            String idSource = candidate.substring(0, index);
            String idTarget = candidate.substring(index, candidate.indexOf("_"));

            element = dataService.getModel().getElement(idSource + "_" + idTarget);
            if (element != null && element.getElementType() == Element.Type.ARC) {

                IArc arc = (IArc) element;
                
                index = 1;
                for (IArc a : arc.getSource().getArcsIn()) {
                    if (a.equals(arc)) {
                        break;
                    }
                    index++;
                }
                return CreateReferencingParameter(candidate, "'" + idTarget + "'.tokenFlow.inflow[" + index + "]", arc);
            }
        } else if (candidate.matches(regexParamPlaceFlowInTotal)) {

            int index = candidate.indexOf("P");

            String idSource = candidate.substring(0, index);
            String idTarget = candidate.substring(index, candidate.indexOf("_"));

            element = dataService.getModel().getElement(idSource + "_" + idTarget);
            if (element != null && element.getElementType() == Element.Type.ARC) {

                IArc arc = (IArc) element;
                
                index = 1;
                for (IArc a : arc.getSource().getArcsIn()) {
                    if (a.equals(arc)) {
                        break;
                    }
                    index++;
                }
                return CreateReferencingParameter(candidate, "'" + idTarget + "'.tokenFlow.inflow[" + index + "]", arc);
            }
        } else if (candidate.matches(regexParamPlaceFlowOutNow)) {

            int index = candidate.indexOf("T");

            String idSource = candidate.substring(0, index);
            String idTarget = candidate.substring(index, candidate.indexOf("_"));

            element = dataService.getModel().getElement(idSource + "_" + idTarget);
            if (element != null && element.getElementType() == Element.Type.ARC) {

                IArc arc = (IArc) element;
                
                index = 1;
                for (IArc a : arc.getSource().getArcsOut()) {
                    if (a.equals(arc)) {
                        break;
                    }
                    index++;
                }
                return CreateReferencingParameter(candidate, "der('" + idSource + "'.tokenFlow.outflow[" + index + "])", arc);
            }
        } else if (candidate.matches(regexParamPlaceFlowOutTotal)) {

            int index = candidate.indexOf("T");

            String idSource = candidate.substring(0, index);
            String idTarget = candidate.substring(index, candidate.indexOf("_"));

            element = dataService.getModel().getElement(idSource + "_" + idTarget);
            if (element != null && element.getElementType() == Element.Type.ARC) {

                IArc arc = (IArc) element;
                
                index = 1;
                for (IArc a : arc.getSource().getArcsOut()) {
                    if (a.equals(arc)) {
                        break;
                    }
                    index++;
                }
                return CreateReferencingParameter(candidate, "'" + idSource + "'.tokenFlow.outflow[" + index + "]", arc);
            }
        } else if (candidate.matches(regexParamToken)) {

            element = dataService.getModel().getElement(candidate);
            if (element != null && element.getElementType() == Element.Type.PLACE) {
                
                return CreateReferencingParameter(candidate, "'" + element.getId() + "'.t", element);
            }
        } else if (candidate.matches(regexTransitionSpeed)) {

            element = dataService.getModel().getElement(candidate);
            if (element != null && element.getElementType() == Element.Type.TRANSITION) {
                
                return CreateReferencingParameter(candidate, "'" + element.getId() + "'.actualSpeed", element);
            }
        }

        return null;
    }

    /**
     * Attempts to remove a parameter.
     *
     * @param param
     * @throws ParameterServiceException
     */
    public void remove(Parameter param) throws ParameterServiceException {
        ValidateRemoval(param);
        dataService.getModel().remove(param);
    }

    /**
     * Validates a function in relation to a given transition. Ensures that all
     * used parameters exist and that their access is not restricted (i.e.
     * access to LOCAL parameters of a different element).
     *
     * @param function
     * @param element
     * @throws ParameterServiceException
     */
    public void ValidateFunction(String function, DataTransition element) throws ParameterServiceException {

        String[] candidates = function.split(functionBuilder.getOperatorRegex());
        Parameter param;

        for (String candidate : candidates) {
            if (!candidate.matches(functionBuilder.getNumberRegex())) { // candidate is NaN

                param = getParameter(candidate);
                if (param == null) {
                    param = getReferencingParameter(candidate);
                    if (param == null) {
                        throw new ParameterServiceException("Parameter '" + candidate + "' does not exist");
                    }
                } else {
                    if (param.getType() == Parameter.Type.LOCAL) {
                        if (!element.getRelatedParameterIds().contains(candidate)) {
                            throw new ParameterServiceException("'" + candidate + "' is a LOCAL parameter for a different element");
                        }
                    }
                }
            }
        }
    }

    /**
     * Validates the removal of a parameter. Removal is valid if the parameter
     * is not referenced by any other elements.
     *
     * @param param
     * @throws ParameterServiceException
     */
    public void ValidateRemoval(Parameter param) throws ParameterServiceException {
        if (!param.getUsingElements().isEmpty()) {
            throw new ParameterServiceException("Cannot delete parameter! It is referenced by another element.");
        }
    }

    /**
     * Validates the removal of a data element. Removal is valid if the element
     * is not related to any parameters that are referenced by other elements.
     *
     * @param element
     * @throws ParameterServiceException
     */
    public void ValidateRemoval(IDataElement element) throws ParameterServiceException {
        Parameter param;
        for (String id : element.getRelatedParameterIds()) {
            param = getParameter(id);
            ValidateRemoval(param, element);
        }
    }

    /**
     * Validates the removal of a parameter in relation to a data element.
     * Removal of the parameter is valid if no element except for the given
     * element is refering to the parameter.
     *
     * @param param
     * @param element
     * @throws ParameterServiceException
     */
    private void ValidateRemoval(Parameter param, IDataElement element) throws ParameterServiceException {
        if (param.getUsingElements().contains(element)) {
            param.getUsingElements().remove(element);
            if (!param.getUsingElements().isEmpty()) {
                param.getUsingElements().add(element);
                throw new ParameterServiceException(element.getId() + "'s parameter '" + param.getId() + "' is referenced by another element.");
            }
            param.getUsingElements().add(element);
        } else {
            if (!param.getUsingElements().isEmpty()) {
                throw new ParameterServiceException(element.getId() + "'s parameter '" + param.getId() + "' is referenced by another element.");
            }
        }
    }
}
