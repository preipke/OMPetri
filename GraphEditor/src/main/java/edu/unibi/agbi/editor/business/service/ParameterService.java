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
import edu.unibi.agbi.petrinet.model.parameter.ReferencingParameter;
import edu.unibi.agbi.petrinet.util.FunctionBuilder;
import edu.unibi.agbi.petrinet.util.ParameterFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
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
    @Autowired private FactoryService factoryService;
    @Autowired private ModelService modelService;

    @Autowired private ParameterFactory parameterFactory;
    @Autowired private FunctionBuilder functionBuilder;

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

        switch (param.getType()) {

            case GLOBAL:

                if (model.containsParameter(param.getId())) {
                    throw new ParameterException("Conflict! Another parameter has already been stored using the same ID!");
                }
                model.add(param);
                break;

            case LOCAL:

                if (param.getRelatedElement().getLocalParameter(param.getId()) != null) {
                    throw new ParameterException("Conflict! Another parameter has already been stored using the same ID!");
                }
                param.getRelatedElement().addLocalParameter(param);
                break;

            case REFERENCE: // reference parameters are never being added manually

                Parameter tmp;
                tmp = findReferencingParameter(model, param.getId());

//                if (tmp != null) {
//                    
//                    tmp.getUsingElements()
//                            .addAll(param.getUsingElements());
//                    
//                } else {
//                    
//                    
//                    
//                }
//                
//                if (model.contains(param.getId())) {
//                    throw new ParameterException("Conflict! Another parameter has already been stored using the same ID!");
//                }
//                model.add(param);
                break;

            default:
                throw new ParameterException("Unhandled parameter type detected!");

        }

    }

    public void validateRelatedParameterNameChange(IElement element, String newId) throws ParameterException {

        if (element.getLocalParameter(newId) != null) {
            throw new ParameterException("The element name is already assigned to a local parameter inside that element!");
        }

        for (Parameter param : element.getRelatedParameters()) {

//            if (param.) {
//
//            }
        }

    }

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

//    /**
//     * Validates if a given ID is free to be used.
//     *
//     * @param id
//     * @return
//     */
//    public boolean isIdAvailable(String id) {
//        return !modelService.getDao().getModel().contains(id);
//    }

    /**
     * Sets the function of an element. Replaces the existing function. Ensures
     * the integrity of parameter references.
     *
     * @param model
     * @param element
     * @param function
     * @param colour         null for transitions, a corresponding colour for
     *                       arcs
     * @throws ParameterException
     */
    public void setFunction(Model model, IElement element, Function function, Colour colour) throws Exception {
        clearFunctionParameterReferences(model, element);
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
            setFunctionParameterReferences(model, element);
        }
    }

    public Parameter findParameter(String id, IElement element) {
        return findParameter(modelService.getModel(), id, element);
    }

    public Parameter findParameter(Model model, String id, IElement element) {

        Parameter param;
        param = null;

        // highest priority - LOCAL param
        if (element != null) {
            param = element.getLocalParameter(id);
        }

        // GLOBAL param
        if (param == null) {
            param = model.getParameter(id);
        }

        // REFERENCE param
        if (param == null) {
            param = findReferencingParameter(model, id);
        }

        return param;
    }

    private Parameter findReferencingParameter(Model model, String id) {

        Parameter param;
        param = null;
        
        IElement element;
        element = model.getElement(id);

        if (element != null) {
            
            for (Parameter par : element.getRelatedParameters()) {

                // TODO use regex pattern to detect variations of referencing parameters for the same element
                if (par.getId().contentEquals(id)) {
                    param = par;
                }
            }
            
            if (param == null) {
                
                try {

                    // TODO use regex pattern to detect variations of referencing parameters for the same element
                    switch (element.getElementType()) {

                        case PLACE:
                            param = parameterFactory.createReferencingParameter(id, element, ReferencingParameter.ReferenceType.TOKEN);
                            break;

                        case TRANSITION:
                            param = parameterFactory.createReferencingParameter(id, element, ReferencingParameter.ReferenceType.SPEED);
                            break;
                    }
                    
                    if (param != null) {
                        param.getRelatedElement().getRelatedParameters().add(param);
                    }

                } catch (Exception ex) {
                    
                    
                }
            }
        }

        return param;
    }

    /**
     * Removes references for all parameters used in a function.
     *
     * @param model
     * @param element
     * @throws ParameterException
     */
    private void clearFunctionParameterReferences(Model model, IElement element) throws ParameterException {

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
     * Adds references to a transition for all parameters used in its function.
     *
     * @param element
     */
    private void setFunctionParameterReferences(Model model, IElement element) throws ParameterException {
        
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
     * Gets the set of IDs representing all parameters from the current dao.
     *
     * @return
     */
    public Set<String> getParameterIds() {
        return modelService.getModel().getParameterIds();
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
     * Gets a collection of all parameters for the currently active model.
     * Also applies the given filter.
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
                .filter(elen -> 
                        elen.getId().toLowerCase().contains(filter) || 
                                ((IDataElement) elen).getLabelText().toLowerCase().contains(filter))
                .sorted((e1, e2) -> e1.toString().compareTo(e2.toString()))
                .forEach(elem -> list.add((IDataElement) elem));
        
        modelService.getModel().getArcs().stream()
                .filter(elem -> 
                        elem.getId().toLowerCase().contains(filter) || 
                                ((IDataElement) elem).getLabelText().toLowerCase().contains(filter))
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
     * Attempts to get a referencing parameter for a given candidate identifier.
     *
     * @param model
     * @param candidate
     * @return
     * @throws ParameterException
     */
    private Parameter getReferencingParameter(Model model, String candidate) throws ParameterException {

        IElement element = model.getElement(candidate);
        Parameter param = null;

        if (element != null) {
            try {
                switch (element.getElementType()) {
                    case PLACE:
                        param = parameterFactory.createReferencingParameter(candidate, element, ReferencingParameter.ReferenceType.TOKEN);
                    case TRANSITION:
                        param = parameterFactory.createReferencingParameter(candidate, element, ReferencingParameter.ReferenceType.SPEED);
                }
            } catch (Exception ex) {
                throw new ParameterException(ex.getMessage());
            }
        }
//        else if (candidate.matches(regexParamPlaceFlowNow) || candidate.matches(regexParamPlaceFlowTotal)) {
//
//            String[] tmp = candidate.split("_");
//            IElement source, target, arc;
//            String idSource, idTarget, value;
//            int index, arcIndex;
//
//            idSource = tmp[0];
//            index = 1;
//            while (((source = model.getElement(idSource)) == null && source instanceof INode) && index < tmp.length) {
//                idSource = idSource + "_" + tmp[index];
//                index++;
//            }
//
//            if (source != null && index < tmp.length - 2) {
//
//                idTarget = tmp[index];
//                while (((target = model.getElement(idTarget)) == null && target instanceof INode) && index < tmp.length) {
//                    idTarget = idTarget + "_" + tmp[index];
//                    index++;
//                }
//
//                if (target != null && index == tmp.length - 1) {
//
//                    arc = model.getElement(factoryService.getArcId((INode) source, (INode) target));
//                    if (arc != null && arc instanceof IArc) {
//
//                        arcIndex = 1;
//                        switch (source.getElementType()) {
//
//                            case PLACE:
//                                for (IArc a : ((IArc) arc).getSource().getArcsOut()) { // place -> transition
//                                    if (a.equals(arc)) {
//                                        break;
//                                    }
//                                    arcIndex++;
//                                }
//                                value = "'" + idSource + "'.tokenFlow.outflow[" + arcIndex + "]";
//                                break;
//
//                            case TRANSITION:
//                                for (IArc a : ((IArc) arc).getSource().getArcsIn()) { // transition -> place
//                                    if (a.equals(arc)) {
//                                        break;
//                                    }
//                                    arcIndex++;
//                                }
//                                value = "'" + idTarget + "'.tokenFlow.inflow[" + arcIndex + "]";
//                                break;
//
//                            default:
//                                throw new ParameterException("Cannot get parameter. Unexpected element type for arc source. '" + source.getElementType() + "' in candidate '" + candidate + "'.");
//                        }
//
//                        if (candidate.matches(regexParamPlaceFlowTotal)) {
//                            return CreateReferencingParameter(model, candidate, value, arc);
//                        } else {
//                            return CreateReferencingParameter(model, candidate, "der(" + value + ")", arc);
//                        }
//                    }
//                }
//            }
//        }

        return param;
    }

    // find all params that have related element that has its name changed. change value if its of type reference (recreate referncing parameter / reassign value and id)
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

    public void UpdateRelatedParameter(IDataElement data) {

        for (Parameter param : data.getRelatedParameters()) {

            for (IElement elem : param.getUsingElements()) {

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
        return ParameterService.this.validateAndGetFunction(modelService.getModel(), element, function);
    }

    /**
     * Validates a function in relation to a given element.Ensures that all
 used parameters exist and that their access is not restricted (i.e. access to LOCAL parameters of a different element).
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
     * Validates the removal of a parameter. Removal is valid if the parameter
     * is not referenced by any other elements.
     *
     * @param param
     * @throws ParameterException
     */
    public void ValidateRemoval(Parameter param) throws ParameterException {
        if (!param.getUsingElements().isEmpty()) {
            throw new ParameterException("Cannot delete parameter! It is referenced by another element.");
        }
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
     * @throws ParameterException
     */
    private void ValidateRemoval(Parameter param, IDataElement element) throws ParameterException {
        if (param.getUsingElements().contains(element)) {
            param.getUsingElements().remove(element);
            if (!param.getUsingElements().isEmpty()) {
                param.getUsingElements().add(element);
                throw new ParameterException(element.getId() + "'s parameter '" + param.getId() + "' is referenced by another element!");
            }
            param.getUsingElements().add(element);
        } else {
            if (!param.getUsingElements().isEmpty()) {
                throw new ParameterException(element.getId() + "'s parameter '" + param.getId() + "' is referenced by another element!");
            }
        }
    }

    private boolean filter(Parameter param, String filter) {
        return param.getId().contains(filter)
                || param.getRelatedElement().getId().contains(filter)
                || ((IDataElement) param.getRelatedElement()).getLabelText().contains(filter);
    }
}
