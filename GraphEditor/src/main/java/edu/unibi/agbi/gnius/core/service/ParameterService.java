/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.exception.ParameterServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.petrinet.entity.IElement;
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

    @Value("${regex.function.number}") private String regexFunctionNumber;
    @Value("${regex.function.operator}") private String regexFunctionOperator;

    /**
     * Attempts to add a parameter.
     *
     * @param element
     * @param param
     * @throws ParameterServiceException
     */
    public void addParameter(Parameter param, IElement element) throws ParameterServiceException {
        if (Parameter.Type.GLOBAL == param.getType()) {
            add(param);
        } else {
            if (element != null) {
                add(param);
                element.getRelatedParameterIds().add(param.getId());
            } else {
                throw new ParameterServiceException("An additional element is required for storing non global parameters!");
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
        if (dataService.getActiveModel().containsAndNotEqual(param)) {
            throw new ParameterServiceException("Conflict! Another parameter has already been stored using the same ID!");
        }
        dataService.getActiveModel().add(param);
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
        removeParameterReferences(transition);
        try {
            transition.setFunction(functionBuilder.build(functionString));
        } finally {
            addParameterReferences(transition);
            removeUnusedReferencingParameter();
        }
    }

    /**
     * Adds references to a transition for all parameters used in its function.
     *
     * @param transition
     */
    private void addParameterReferences(DataTransition transition) {
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
    private void removeParameterReferences(DataTransition transition) {
        transition.getFunction().getParameterIds().forEach(id -> {
            getParameter(id)
                    .getUsingElements()
                    .remove(transition);
        });
    }
    
    /**
     * Removes any unused parameter that is a reference to an element.
     */
    private void removeUnusedReferencingParameter() {
        List<Parameter> paramsUnused = new ArrayList();
        dataService.getActiveModel().getParameters().forEach(param -> {
            if (param.getType() == Parameter.Type.REFERENCE) {
                if (param.getUsingElements().isEmpty()) {
                    dataService.getActiveModel()
                            .getElement(param.getRelatedElementId()).getRelatedParameterIds()
                            .remove(param.getId());
                    paramsUnused.add(param);
                }
            }
        });
        paramsUnused.forEach(param -> dataService.getActiveModel().remove(param));
    }

    /**
     * Gets all global parameters. List is sorted by parameter ids (natural
     * string order).
     *
     * @return
     */
    public List<Parameter> getGlobalParameters() {
        List<Parameter> parameters = new ArrayList();
        for (Parameter param : dataService.getActiveModel().getParameters()) {
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
        for (String id : elem.getRelatedParameterIds()) {
            param = getParameter(id);
            if (param.getType() == Parameter.Type.LOCAL) {
                parameters.add(param);
            }
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
        return dataService.getActiveModel().getParameter(id);
    }

    /**
     * Gets the list of currently used parameter ids.
     *
     * @return
     */
    public Set<String> getParameterIds() {
        return dataService.getActiveModel().getParameterIds();
    }

    /**
     * Attempts to remove a parameter.
     *
     * @param param
     * @throws ParameterServiceException
     */
    public void remove(Parameter param) throws ParameterServiceException {
        ValidateRemoval(param);
        dataService.getActiveModel().remove(param);
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

        String[] candidates = function.split(regexFunctionOperator);
        Parameter param;

        for (String candidate : candidates) {
            if (!candidate.matches(regexFunctionNumber)) { // candidate is NaN
                param = getParameter(candidate);
                if (param != null) {
                    if (param.getType() == Parameter.Type.LOCAL) {
                        if (!element.getRelatedParameterIds().contains(candidate)) {
                            throw new ParameterServiceException("'" + candidate + "' is a LOCAL parameter for a different element");
                        }
                    }
                } else {
                    throw new ParameterServiceException("Parameter '" + candidate + "' does not exist");
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
                throw new ParameterServiceException("Cannot delete parameter! It is referenced by another element.");
            }
            param.getUsingElements().add(element);
        } else {
            if (!param.getUsingElements().isEmpty()) {
                throw new ParameterServiceException("Cannot delete parameter! It is referenced by another element.");
            }
        }
    }
}
