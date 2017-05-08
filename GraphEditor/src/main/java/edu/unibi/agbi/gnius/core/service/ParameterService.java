/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.core.exception.InputValidationException;
import edu.unibi.agbi.gnius.core.exception.ParameterServiceException;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private final DataDao dataDao;
    
    @Value("${regex.function.number}")
    private String regexFunctionNumber;
    @Value("${regex.function.operator}")
    private String regexFunctionOperator;
    @Value("${regex.function.parameter}")
    private String regexFunctionParameter;
    
    @Autowired
    public ParameterService(DataDao dataDao) {
        this.dataDao = dataDao;
    }
    
    public void add(Parameter param) throws ParameterServiceException {
        switch (param.getType()) {
            case REFERENCE:
                addReference(param);
                break;
            default:
                addParameter(param);
        }
        if (dataDao.containsAndNotEqual(param)) {
            throw new ParameterServiceException("Conflict! Another parameter has already been stored using the same ID!");
        }
        dataDao.add(param);
    }
    
    private void addReference(Parameter param) {
        
    }

    /**
     * Attempts to add the given parameter.
     *
     * @param param
     * @throws ParameterServiceException
     */
    private void addParameter(Parameter param) throws ParameterServiceException {
        if (param.getId().matches("") | !param.getId().matches(regexFunctionParameter)) {
            throw new ParameterServiceException("Invalid parameter name!");
        }
        if (param.getValue().matches("")| !param.getValue().matches(regexFunctionNumber)) {
            throw new ParameterServiceException("Invalid parameter value!");
        }
        switch (param.getType()) {
            case LOCAL:
                localParameters.add(param);
                selectedElement.getParameters().put(param.getId(), param);
                break;
            case GLOBAL:
                globalParameters.add(param);
                break;
        }
    }

    private Parameter createOrGet(String id, String value, Parameter.Type type) throws ParameterServiceException {
        Parameter param = getParameter(id);
        if (param != null) {
            if (!param.getValue().matches(value)) {
                
            } else if (param.getType() != type) {
                
            }
        } else {
            param = new Parameter(id, "", value, Parameter.Type.REFERENCE);
            add(param);
        }
        return param;
    }
    
    public List<Parameter> getGlobalParameters() {
        
        List<Parameter> parameters = new ArrayList();
        
        parameters.addAll(globalParameters);
        parameters.sort(Comparator.comparing(Parameter::toString));
        
        return parameters;
    }

    /**
     * Gets all parameters usable for an element.
     *
     * @param elem
     * @return
     */
    public List<Parameter> getLocalParameters(IDataElement elem) {

        List<Parameter> parameters = new ArrayList();

        for (Parameter param : elem.getParameters().values()) {
            if (param.getType()==Parameter.Type.LOCAL) {
                parameters.add(param);
            }
        }

        return parameters;
    }

    /**
     * Gets the parameter with the given id.
     *
     * @param id
     * @return
     */
    public Parameter getParameter(String id) {
        return dataDao.getParameters().get(id);
    }

    /**
     * Gets the list of currently used parameter ids.
     *
     * @return
     */
    public Set<String> getParameterIds() {
        return dataDao.getParameters().keySet();
    }

    /**
     * Validates wether an element references a parameter or not.
     *
     * @param elem
     * @param param
     * @return
     */
    public boolean isElementReferencingParameter(IDataElement elem, Parameter param) {
        switch (elem.getElementType()) {
            case TRANSITION:
                DataTransition transition = (DataTransition) elem;
                if (transition.getFunction().getParameters().contains(param)) {
                    return true;
                }
        }
        return false;
    }

    /**
     * Removes the given parameter.
     *
     * @param param
     * @throws DataGraphServiceException
     */
    public void remove(Parameter param) throws DataGraphServiceException {
        if (!param.getReferingNodes().isEmpty()) {
            throw new DataGraphServiceException("Cannot delete parameter '" + param.getId() + "'! There is elements refering to it.");
        }
        dataDao.remove(param);
    }
    
    /**
     * Validates the function input for a transition.
     *
     * @param element
     * @param functionInput
     * @throws InputValidationException
     */
    public void ValidateFunctionInput(DataTransition element, String functionInput) throws InputValidationException {

        String[] candidates = functionInput.split(regexFunctionOperator);
        List<Parameter> currentParameter = new ArrayList();
        Parameter param;

        for (String candidate : candidates) {
            if (!candidate.matches(regexFunctionNumber)) {
                if (getParameterIds().contains(candidate)) {
                    param = getParameter(candidate);
                    if (param.getType() == Parameter.Type.LOCAL) {
                        if (!element.getParameters().keySet().contains(candidate)) {
                            throw new InputValidationException("'" + candidate + "' is a LOCAL parameter for a different element");
                        }
                    }
                    param.addReferingNode(element);
                    currentParameter.add(param);
                } else {
                    throw new InputValidationException("Parameter '" + candidate + "' does not exist");
                }
            }
        }

        element.getFunction().getParameters().clear();
        currentParameter.forEach(parameter -> {
            element.getFunction().getParameters().add(parameter);
        });
    }
    
    /**
     * Refreshes the number of referring nodes for each global parameter.
     */
    private void RefreshGlobalParameterReferences() {
        globalParameters.parallelStream().forEach(param -> {
            IDataElement[] elements = new IDataElement[param.getReferingNodes().size()];
            param.getReferingNodes().toArray(elements);
            for (IDataElement element : elements) {
                if (!isElementReferencingParameter(element, param)) {
                    param.removeReferingNode(element);
                }
            }
        });
    }
}
