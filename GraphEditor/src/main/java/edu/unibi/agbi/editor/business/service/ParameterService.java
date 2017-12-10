/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.business.service;

import edu.unibi.agbi.editor.business.exception.ParameterException;
import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataTransition;
import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Model;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.Weight;
import edu.unibi.agbi.petrinet.model.parameter.ReferencingParameter.ReferenceType;
import edu.unibi.agbi.petrinet.util.FunctionFactory;
import edu.unibi.agbi.petrinet.util.ParameterFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class ParameterService
{
    @Autowired private ModelService modelService;
    @Autowired private ParameterFactory parameterFactory;
    @Autowired private FunctionFactory functionBuilder;

    /**
     * Attempts to add a parameter.
     *
     * @param param
     * @throws ParameterException
     */
    public void add(Parameter param) throws ParameterException {
        add(modelService.getModel(), param);
    }

    /**
     * Attempts to add a parameter.
     *
     * @param model
     * @param param
     * @throws ParameterException
     */
    public void add(Model model, Parameter param) throws ParameterException {
        try {
            model.add(param);
        } catch (Exception ex) {
            throw new ParameterException(ex.getMessage());
        }
    }

    /**
     * Sets the function of an element. Replaces the existing function. Ensures
     * the integrity of parameter references.
     *
     * @param model
     * @param element
     * @param function
     * @param colour   null for transitions, a corresponding colour for arcs
     * @throws ParameterException
     */
    public void setElementFunction(Model model, IElement element, Function function, Colour colour) throws Exception {
        clearElementFunctionsUsedParameterReferences(model, element);
        try {
            switch (element.getElementType()) {
                case ARC:
                    ((Arc) element)
                            .getWeight(colour)
                            .setFunction(function);
                    break;

                case TRANSITION:
                    ((Transition) element)
                            .setFunction(function);
                    break;

                default:
                    throw new ParameterException("The given element is not using functions!");
            }
        } finally {
            setElementFunctionsUsedParameterReferences(model, element);
        }
    }

    /**
     * Removes references for all parameters used in a function.
     *
     * @param model
     * @param element
     * @throws ParameterException
     */
    private void clearElementFunctionsUsedParameterReferences(Model model, IElement element) throws ParameterException {

        for (String id : getIdsForUsedParameters(element)) {

            Parameter param;
            param = findParameter(model, id, element);

            if (param == null) {
                throw new ParameterException("Unavailable parameter '" + id + "' used by '" + element.toString() + "'.");
            }

            param.getUsingElements()
                    .remove(element);
        }
    }

    /**
     * Adds a reference to a given element for all parameters that it is using
     * inside its function.
     *
     * @param element
     */
    private void setElementFunctionsUsedParameterReferences(Model model, IElement element) throws ParameterException {

        for (String id : getIdsForUsedParameters(element)) {

            Parameter param;
            param = findParameter(model, id, element);

            if (param == null) {
                throw new ParameterException("Unavailable parameter '" + id + "' refered to by '" + element.toString() + "'.");
            }

            param.getUsingElements()
                    .add(element);
        }
    }

    public Parameter findParameter(String id, IElement element) {
        return findParameter(modelService.getModel(), id, element);
    }
    
    public Parameter findParameter(Model model, String id, IElement element) {
        return model.findParameter(parameterFactory, id, element);
    }

    /**
     * Gets the parameter with the given id from the current dao.
     *
     * @param id
     * @return
     */
    public Parameter getParameter(String id) {
        return modelService.getModel().getParameter(id);
    }

    /**
     * Gets all global parameters for a given model. List is sorted by parameter
     * ids (natural string order).
     *
     * @param model
     * @return
     */
    public Collection<Parameter> getSortedGlobalParameters(Model model) {
        List<Parameter> params;
        params = new ArrayList();
        params.addAll(model.getParameters());
        params.sort(Comparator.comparing(Parameter::getId));
        return params;
    }

    /**
     * Gets all parameters usable for an element. List is sorted by parameter
     * ids (natural string order).
     *
     * @param elem
     * @return
     */
    public List<Parameter> getSortedLocalParameters(IDataElement elem) {
        List<Parameter> parameters = new ArrayList();
        if (elem instanceof DataTransition) {
            parameters.addAll(((DataTransition) elem).getLocalParameters());
        }
        parameters.sort(Comparator.comparing(Parameter::getId));
        return parameters;
    }

    /**
     * Gets a collection of all parameters for the currently active model. Also
     * applies the given filter.
     *
     * @param element
     * @param filter
     * @return
     */
    public List<Parameter> getFilteredAndSortedParameterList(IDataElement element, String filter) {

        List<Parameter> all = new ArrayList();
        List<Parameter> locals = new ArrayList();
        List<Parameter> others = new ArrayList();

        modelService.getModel().getArcs().forEach(arc -> {
            if (arc.equals(element)) {
                locals.addAll(arc.getLocalParameters());
            } else {
                others.addAll(arc.getLocalParameters());
            }
        });

        modelService.getModel().getTransitions().forEach(transition -> {
            if (transition.equals(element)) {
                locals.addAll(transition.getLocalParameters());
            } else {
                others.addAll(transition.getLocalParameters());
            }
        });

        locals.stream()
                .filter(param -> filter(param, filter))
                .sorted((p1, p2) -> p1.getId().compareTo(p2.getId()))
                .forEach(param -> all.add(param));
        modelService.getModel().getParameters().stream()
                .filter(param -> param.getType() == Parameter.Type.GLOBAL && filter(param, filter))
                .sorted((p1, p2) -> p1.getId().compareTo(p2.getId()))
                .forEach(param -> all.add(param));
        others.stream()
                .filter(param -> filter(param, filter))
                .sorted((p1, p2) -> p1.getId().compareTo(p2.getId()))
                .forEach(param -> all.add(param));

        return all;
    }

    /**
     * Gets a list of all elements that can store and use local parameters.
     * Applies the given filter.
     *
     * @param filter
     * @return
     */
    public List<IDataElement> getFilteredChoicesForLocalParameters(String filter) {

        List<IDataElement> list = new ArrayList();

        modelService.getModel().getTransitions().stream()
                .filter(elen
                        -> elen.getId().toLowerCase().contains(filter)
                || ((IDataElement) elen).getLabelText().toLowerCase().contains(filter))
                .sorted((e1, e2) -> e1.toString().compareTo(e2.toString()))
                .forEach(elem -> list.add((IDataElement) elem));

        modelService.getModel().getArcs().stream()
                .filter(elem
                        -> elem.getId().toLowerCase().contains(filter)
                || ((IDataElement) elem).getLabelText().toLowerCase().contains(filter))
                .sorted((e1, e2) -> e1.toString().compareTo(e2.toString()))
                .forEach(elem -> list.add((IDataElement) elem));

        return list;
    }

    /**
     * Gets the IDs for all parameters used by a given Element.
     *
     * @param element
     * @return
     * @throws ParameterException
     */
    private Collection<String> getIdsForUsedParameters(IElement element) throws ParameterException {

        HashSet<String> parameterIds;

        switch (element.getElementType()) {
            case ARC:
                parameterIds = new HashSet();
                for (Weight weight : ((DataArc) element).getWeights()) {
                    parameterIds.addAll(weight.getFunction().getParameterIds());
                }
                break;

            case TRANSITION:
                parameterIds = ((DataTransition) element).getFunction().getParameterIds();
                break;

            default:
                throw new ParameterException("Given element has no function! (" + element.toString() + ")");
        }

        return parameterIds;
    }

    /**
     * Attempts to remove a parameter.
     *
     * @param param
     * @throws ParameterException
     */
    public void remove(Parameter param) throws ParameterException {
        ValidateRemoval(param);
        if (param.getType() == Parameter.Type.LOCAL) {
            if (param.getRelatedElement() != null
                    && param.getRelatedElement() instanceof DataTransition) {
                ((DataTransition) param.getRelatedElement()).getLocalParameters().remove(param);
            } else {
                throw new ParameterException("Inconsistency found. LOCAL parameter related to non-transition element.");
            }
        } else {
            modelService.getModel().remove(param);
        }
    }

    /**
     * Updates the value and unit of a given parameter. Only applicable to
     * global and local parameters.
     *
     * @param param
     * @param value
     * @param unit
     * @throws ParameterException
     */
    public void updateParameter(Parameter param, String value, String unit) throws ParameterException {
        switch (param.getType()) {

            case GLOBAL:
                break;

            case LOCAL:
                break;

            default:
                throw new ParameterException("Cannot update parameters other than local or global!");

        }
        param.setValue(value);
        param.setUnit(unit);
    }

    public void updateRelatedParameterIds(final IElement element, final String elementIdNew) throws Exception {

        String paramIdOld, paramIdNew;
        ReferenceType referenceType;

        DataArc arc;
        DataTransition transition;

        for (Parameter param : element.getRelatedParameters()) {

            referenceType = parameterFactory.recoverReferenceTypeFromParameterValue(param.getValue());
            paramIdOld = param.getId();
            paramIdNew = parameterFactory.generateIdForReferencingParameter(elementIdNew, referenceType);

            for (IElement usingElement : param.getUsingElements()) {

                if (usingElement instanceof DataArc) {

                    arc = (DataArc) usingElement;

                    for (Weight weight : arc.getWeights()) {
                        updateParameterIdsInFunction(weight.getFunction(), paramIdOld, paramIdNew);
                    }

                } else if (usingElement instanceof DataTransition) {

                    transition = (DataTransition) usingElement;
                    updateParameterIdsInFunction(transition.getFunction(), paramIdOld, paramIdNew);
                }
            }
            
            param.setId(paramIdNew);
            param.setValue(parameterFactory.generateValueForReferencingParamter(elementIdNew, referenceType));
        }
    }

    /**
     * Updates the parameter ids in a given function. Every function element
     * with the oldId as their value will have its value replaced by the newId.
     *
     * @param function
     * @param oldId
     * @param newId
     */
    private void updateParameterIdsInFunction(Function function, String oldId, String newId) {

        for (Function functionElement : function.getElements()) {

            if (functionElement.getType() == Function.Type.PARAMETER) {

                if (functionElement.getValue().contentEquals(oldId)) {

                    functionElement.setValue(newId);
                }
            }
        }
    }

    /**
     * Validates a function in relation to a given element.Ensures that all used
     * parameters exist and that their access is not restricted (i.e. access to
     * LOCAL parameters of a different element).
     *
     * @param function
     * @param element
     * @return
     * @throws ParameterException
     */
    public Function validateAndGetFunction(IDataElement element, String function) throws ParameterException {
        return validateAndGetFunction(modelService.getModel(), element, function);
    }

    /**
     * Validates a function in relation to a given element.Ensures that all used
     * parameters exist and that their access is not restricted (i.e. access to
     * LOCAL parameters of a different element).
     *
     * @param model
     * @param functionString
     * @param element
     * @return
     * @throws ParameterException
     */
    public Function validateAndGetFunction(Model model, IElement element, String functionString) throws ParameterException {

        Function func;
        Parameter param;

        try {
            functionString = functionString.replace(" ", "");
            func = functionBuilder.build(functionString);
        } catch (Exception ex) {
            throw new ParameterException("Malformed function string!", ex);
        }

        for (String candidate : func.getParameterIds()) {

            param = findParameter(model, candidate, element);

            if (param == null) {
                throw new ParameterException("Parameter for candidate '" + candidate + "' cannot be generated!");
            }
        }

        return func;
    }

    /**
     * Validates the removal of a data element. Removal is only valid if none of
     * the element's related referencing parameters are used by other elements.
     *
     * @param element
     * @throws ParameterException
     */
    public void ValidateRemoval(IDataElement element) throws ParameterException {
        for (Parameter param : element.getRelatedParameters()) {
            ValidateRemoval(param);
        }
    }

    /**
     * Validates the removal of a parameter in relation to a data element.
     * Removal of the parameter is valid if no element except for the given
     * element is refering to the parameter.
     *
     * @param param
     * @param element
     * @throws ParameterException
     */
    private void ValidateRemoval(Parameter param) throws ParameterException {
        
        if (param.getUsingElements().size() > 1) {

            throw new ParameterException("Parameter '" + param.getId() + "' is referenced by another element!");
            
        } else if (param.getUsingElements().size() == 1) {
            
            if (param.getRelatedElement() == null || 
                    !param.getUsingElements().contains(param.getRelatedElement())) {

                throw new ParameterException(param.getRelatedElement() + "'s parameter '" + param.getId() + "' is referenced by another element!");
            }
        }
    }

    private boolean filter(Parameter param, String filter) {
        return param.getId().contains(filter)
                || param.getRelatedElement().getId().contains(filter)
                || ((IDataElement) param.getRelatedElement()).getLabelText().contains(filter);
    }
}
