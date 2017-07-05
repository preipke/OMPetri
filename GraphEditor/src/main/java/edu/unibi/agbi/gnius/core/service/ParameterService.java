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
import edu.unibi.agbi.petrinet.model.Model;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.util.FunctionBuilder;
import java.util.ArrayList;
import java.util.Collection;
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

    /**
     * Attempts to add a parameter.
     *
     * @param element
     * @param param
     * @throws ParameterServiceException
     */
    public void add(Parameter param, IElement element) throws ParameterServiceException {
        add(dataService.getModel(), param, element);
    }

    /**
     * Attempts to add a parameter.
     *
     * @param model
     * @param element
     * @param param
     * @throws ParameterServiceException
     */
    public void add(Model model, Parameter param, IElement element) throws ParameterServiceException {
        if (Parameter.Type.GLOBAL == param.getType()) {
            add(model, param);
        } else {
            if (element != null) {
                if (model.containsAndNotEqual(param)) {
                    throw new ParameterServiceException("Conflict! Another parameter has already been stored using the same ID!");
                }
                model.add(param);
                element.getRelatedParameterIds().add(param.getId());
            } else {
                throw new ParameterServiceException("A reference element is required for storing non global parameters!");
            }
        }
    }

    /**
     * Attempts to add a parameter to the dao.
     *
     * @param model
     * @param param
     * @throws ParameterServiceException
     */
    private void add(Model model, Parameter param) throws ParameterServiceException {
        if (Parameter.Type.GLOBAL != param.getType()) {
            throw new ParameterServiceException("Wrong method for adding non global parameters! Reference element required.");
        }
        if (model.containsAndNotEqual(param)) {
            throw new ParameterServiceException("Conflict! Another parameter has already been stored using the same ID!");
        }
        model.add(param);
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
            transition.setFunction(functionBuilder.build(functionString, false));
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
            Parameter param = transition.getParameter(id);
            if (param == null) {
                param = getParameter(id);
            }
            param.getUsingElements()
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
            Parameter param = transition.getParameter(id);
            if (param == null) {
                param = getParameter(id);
            }
            param.getUsingElements()
                    .remove(transition);
        });
    }

    /**
     * Removes any unused parameters that are references to an element.
     */
    private void removeUnusedReferencingParameter() {
        List<Parameter> paramsUnused = new ArrayList();
        getParameters().forEach(param -> {
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
     * Gets all global parameters. List is sorted by parameter ids (natural
     * string order).
     *
     * @return
     */
    public List<Parameter> getGlobalParameters() {
        List<Parameter> parameters = new ArrayList();
        for (Parameter param : getParameters()) {
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
            getParameters().forEach(p -> {
                if (p.getType() == Parameter.Type.LOCAL) {
                    parameters.add(p);
                }
            });
        }
        parameters.sort(Comparator.comparing(Parameter::getId));
        return parameters;
    }

    /**
     * Gets the parameter with the given id from the current dao.
     *
     * @param id
     * @return
     */
    public Parameter getParameter(String id) {
        return dataService.getModel().getParameter(id);
    }

    /**
     * Gets the set of IDs representing all parameters from the current dao.
     *
     * @return
     */
    public Set<String> getParameterIds() {
        return dataService.getModel().getParameterIds();
    }
    
    /**
     * Gets a collection of all parameters for the current dao.
     * @return 
     */
    public Collection<Parameter> getParameters() {
        return dataService.getModel().getParameters();
    }

    /**
     * Attempts to get a referencing parameter for a given candidate identifier.
     *
     * @param model
     * @param candidate
     * @return
     * @throws ParameterServiceException
     */
    private Parameter getReferencingParameter(Model model, String candidate) throws ParameterServiceException {

        IElement element;
        int index;

        if (candidate.matches(regexParamPlaceFlowInNow)) {

            index = candidate.indexOf("P");

            String idSource = candidate.substring(0, index);
            String idTarget = candidate.substring(index, candidate.indexOf("_"));

            element = model.getElement(idSource + "_" + idTarget);
            if (element != null && element.getElementType() == Element.Type.ARC) {

                IArc arc = (IArc) element;

                index = 1;
                for (IArc a : arc.getSource().getArcsIn()) {
                    if (a.equals(arc)) {
                        break;
                    }
                    index++;
                }
                return CreateReferencingParameter(model, candidate, "'" + idTarget + "'.tokenFlow.inflow[" + index + "]", arc);
            }

        } else if (candidate.matches(regexParamPlaceFlowInTotal)) {

            index = candidate.indexOf("P");

            String idSource = candidate.substring(0, index);
            String idTarget = candidate.substring(index, candidate.indexOf("_"));

            element = model.getElement(idSource + "_" + idTarget);
            if (element != null && element.getElementType() == Element.Type.ARC) {

                IArc arc = (IArc) element;

                index = 1;
                for (IArc a : arc.getSource().getArcsIn()) {
                    if (a.equals(arc)) {
                        break;
                    }
                    index++;
                }
                return CreateReferencingParameter(model, candidate, "'" + idTarget + "'.tokenFlow.inflow[" + index + "]", arc);
            }

        } else if (candidate.matches(regexParamPlaceFlowOutNow)) {

            index = candidate.indexOf("T");

            String idSource = candidate.substring(0, index);
            String idTarget = candidate.substring(index, candidate.indexOf("_"));

            element = model.getElement(idSource + "_" + idTarget);
            if (element != null && element.getElementType() == Element.Type.ARC) {

                IArc arc = (IArc) element;

                index = 1;
                for (IArc a : arc.getSource().getArcsOut()) {
                    if (a.equals(arc)) {
                        break;
                    }
                    index++;
                }
                return CreateReferencingParameter(model, candidate, "der('" + idSource + "'.tokenFlow.outflow[" + index + "])", arc);
            }

        } else if (candidate.matches(regexParamPlaceFlowOutTotal)) {

            index = candidate.indexOf("T");

            String idSource = candidate.substring(0, index);
            String idTarget = candidate.substring(index, candidate.indexOf("_"));

            element = model.getElement(idSource + "_" + idTarget);
            if (element != null && element.getElementType() == Element.Type.ARC) {

                IArc arc = (IArc) element;

                index = 1;
                for (IArc a : arc.getSource().getArcsOut()) {
                    if (a.equals(arc)) {
                        break;
                    }
                    index++;
                }
                return CreateReferencingParameter(model, candidate, "'" + idSource + "'.tokenFlow.outflow[" + index + "]", arc);
            }

        } else {

            element = model.getElement(candidate);
            if (element != null) {
                switch (element.getElementType()) {
                    case PLACE:
                        return CreateReferencingParameter(model, candidate, "'" + element.getId() + "'.t", element);
                    case TRANSITION:
                        return CreateReferencingParameter(model, candidate, "'" + element.getId() + "'.actualSpeed", element);
                }
            }
        }

        return null;
    }

    /**
     * Creates a parameter that references an element.
     *
     * @param model
     * @param id
     * @param value
     * @param element
     * @throws ParameterServiceException
     */
    private Parameter CreateReferencingParameter(Model model, String id, String value, IElement element) throws ParameterServiceException {
        Parameter param = new Parameter(id, "", value, Parameter.Type.REFERENCE, element.getId());
        add(model, param, element);
        return param;
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
        ValidateFunction(dataService.getModel(), function, element);
    }

    /**
     * Validates a function in relation to a given transition. Ensures that all
     * used parameters exist and that their access is not restricted (i.e.
     * access to LOCAL parameters of a different element).
     *
     * @param model
     * @param function
     * @param transition
     * @throws ParameterServiceException
     */
    public void ValidateFunction(Model model, String function, DataTransition transition) throws ParameterServiceException {

        Parameter param;
        String[] candidates;
        
        function = function.replace(" ", "");
        candidates = function.split(functionBuilder.getOperatorExtRegex());

        for (String candidate : candidates) {
            if (!candidate.matches("")) {
                if (!candidate.matches(functionBuilder.getNumberRegex())) { // candidate is NaN

                    param = transition.getParameter(candidate);
                    if (param == null) {
                        param = model.getParameter(candidate);
                    }
                    if (param == null) {
                        param = getReferencingParameter(model, candidate);
                    }
                    if (param == null) {
                        throw new ParameterServiceException("Parameter '" + candidate + "' does not exist");
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
