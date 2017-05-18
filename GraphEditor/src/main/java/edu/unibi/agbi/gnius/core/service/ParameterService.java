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
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.util.FunctionBuilder;
import java.io.IOException;
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
                element.getParameters().put(param.getId(), param);
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
     * Sets the function of a transition.Replaces the existing function,
     * ensuring the integrity of parameter references.
     *
     * @param transition
     * @param functionString
     * @throws edu.unibi.agbi.gnius.core.exception.ParameterServiceException
     */
    public void setTransitionFunction(DataTransition transition, String functionString) throws ParameterServiceException {
        removeParameterReferences(transition);
        try {
            Function function = functionBuilder.build(functionString);
            transition.setFunction(function);
        } catch (IOException ex) {
            throw new ParameterServiceException("");
        }
        addParameterReferences(transition);
    }

    /**
     * Adds references to a transition for all parameters used in its function.
     *
     * @param transition
     */
    private void addParameterReferences(DataTransition transition) {
        transition.getFunction().getParameterIds().forEach(id -> {
            getParameter(id).getReferingNodes().add(transition);
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
            getParameter(id).getReferingNodes().remove(transition);
        });
    }

    /**
     * Gets all global parameters. List is sorted by parameter ids (natural
     * string order).
     *
     * @return
     */
    public List<Parameter> getGlobalParameters() {
        List<Parameter> parameters = new ArrayList();
        for (Parameter param : dataService.getActiveModel().getParameters().values()) {
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
        for (Parameter param : elem.getParameters().values()) {
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
        return dataService.getActiveModel().getParameters().get(id);
    }

    /**
     * Gets the list of currently used parameter ids.
     *
     * @return
     */
    public Set<String> getParameterIds() {
        return dataService.getActiveModel().getParameters().keySet();
    }

    /**
     * Attempts to remove a parameter.
     *
     * @param param
     * @param element
     * @throws ParameterServiceException
     */
    public void remove(Parameter param, IDataElement element) throws ParameterServiceException {
        ValidateRemoval(param, element);
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
                        if (!element.getParameters().keySet().contains(candidate)) {
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
     *
     * @param param
     * @param element
     * @throws ParameterServiceException
     */
    private void ValidateRemoval(Parameter param, IDataElement element) throws ParameterServiceException {
        if (Parameter.Type.LOCAL != param.getType()) {
            if (!param.getReferingNodes().isEmpty()) {
                if (param.getReferingNodes().size() != 1 || element != null && !param.getReferingNodes().contains(element)) {
                    throw new ParameterServiceException("Cannot delete parameter! It is referenced by another element.");
                }
            }
        }
    }

    /**
     *
     * @param params
     * @param element
     * @throws ParameterServiceException
     */
    public void ValidateRemoval(Collection<Parameter> params, IDataElement element) throws ParameterServiceException {
        for (Parameter param : params) {
            ValidateRemoval(param, element);
        }
    }
}
