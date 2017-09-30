/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.business.service;

import edu.unibi.agbi.editor.business.exception.ParameterException;
import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataTransition;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.entity.INode;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Model;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.Weight;
import edu.unibi.agbi.petrinet.util.FunctionBuilder;
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
    @Autowired private ModelService dataService;
    @Autowired private FunctionBuilder functionBuilder;

    @Value("${regex.param.ident.flow.actual}") private String regexParamPlaceFlowNow;
    @Value("${regex.param.ident.flow.total}") private String regexParamPlaceFlowTotal;

    /**
     * Attempts to add a parameter.
     *
     * @param param
     * @throws ParameterException
     */
    public void add(Parameter param) throws ParameterException {
        add(dataService.getModel(), param);
    }

    /**
     * Attempts to add a parameter.
     *
     * @param model
     * @param param
     * @throws ParameterException
     */
    public void add(Model model, Parameter param) throws ParameterException {
        if (Parameter.Type.GLOBAL == param.getType()) {
            if (model.contains(param)) {
                throw new ParameterException("Conflict: Another parameter has already been stored using the same ID.");
            }
            model.add(param);
        } else {
            if (param.getRelatedElement() == null) {
                throw new ParameterException("A reference element is required for storing non global parameters.");
            }
            if (Parameter.Type.LOCAL == param.getType()) {
                if (param.getRelatedElement() instanceof DataTransition) {
                    DataTransition transition = (DataTransition) param.getRelatedElement();
                    if (transition.getLocalParameter(param.getId()) != null) {
                        throw new ParameterException("Conflict: Another parameter has already been stored using the same ID.");
                    }
                    transition.addLocalParameter(param);
                } else {
                    throw new ParameterException("Trying to store LOCAL parameter for non-transition element.");
                }
            } else {
                if (model.contains(param)) {
                    throw new ParameterException("Conflict: Another parameter has already been stored using the same ID.");
                }
                model.add(param);
            }
            param.getRelatedElement().getRelatedParameters().add(param);
        }
    }

    /**
     * Sets the function of an element. Replaces the existing function. Ensures 
     * the integrity of parameter references.
     *
     * @param model
     * @param element
     * @param functionString
     * @param colour null for transitions, a corresponding colour for arcs
     * @throws ParameterException
     */
    public void setFunction(Model model, IElement element, String functionString, Colour colour) throws Exception {
        clearTransitionFunctionParameterReferences(model, element);
        try {
            switch (element.getElementType()) {
                case ARC:
                    ((Arc) element).getWeight(colour).setFunction(functionBuilder.build(functionString, false));
                    break;
                    
                case TRANSITION:
                    ((Transition) element).setFunction(functionBuilder.build(functionString, false));
                    break;
                    
                default:
                    throw new ParameterException("The given element cannot be set any Function!");
                        
            }
        } finally {
            setTransitionFunctionParameterReferences(model, element);
        }
    }
    
    /**
     * Gets the IDs for all parameters used by a given Element.
     * 
     * @param element
     * @return
     * @throws ParameterException 
     */
    private Collection<String> getParameterIds(IElement element) throws ParameterException {
        
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
     * Adds references to a transition for all parameters used in its function.
     *
     * @param element
     */
    private void setTransitionFunctionParameterReferences(Model model, IElement element) throws ParameterException {
        for (String id : getParameterIds(element)) {
            Parameter param = element.getLocalParameter(id);
            if (param == null) {
                param = model.getParameter(id);
            }
            if (param != null) {
                param.getUsingElements()
                        .add(element);
            } else {
                throw new ParameterException("Unavailable parameter '" + id + "' refered to by '" + element.toString() + "'.");
            }
        }
    }

    /**
     * Removes references to a transition for all parameters used in its
     * function.
     *
     * @param element
     */
    private void clearTransitionFunctionParameterReferences(Model model, IElement element) throws ParameterException {
        for (String id : getParameterIds(element)) {
            Parameter param = element.getLocalParameter(id);
            if (param == null) {
                param = model.getParameter(id);
            }
            if (param != null) {
                param.getUsingElements()
                        .remove(element);
            } else {
                throw new ParameterException("Unavailable parameter '" + id + "' refered to by '" + element.toString() + "'.");
            }
        }
    }

    /**
     * Removes any unused parameters that are references to an element.
     */
    private void removeUnusedReferencingParameters(Model model) {
        List<Parameter> paramsUnused = new ArrayList();
        model.getParameters().forEach(param -> {
            if (param.getType() == Parameter.Type.REFERENCE) {
                if (param.getUsingElements().isEmpty()) {
                    param.getRelatedElement().getRelatedParameters().remove(param);
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
        if (elem instanceof DataTransition) {
            parameters.addAll(((DataTransition) elem).getLocalParameters());
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
     *
     * @return
     */
    public Collection<Parameter> getParameters() {
        return dataService.getModel().getParameters();
    }

    /**
     * Gets a collection of all parameters for the current dao.
     *
     * @param element
     * @param filter
     * @return
     */
    public List<Parameter> getFilteredAndSortedParameterList(IDataElement element, String filter) {

        List<Parameter> all = new ArrayList();
        List<Parameter> locals = new ArrayList();
        List<Parameter> others = new ArrayList();

        dataService.getModel().getTransitions().forEach(transition -> {
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
        dataService.getModel().getParameters().stream()
                .filter(param -> param.getType() == Parameter.Type.GLOBAL
                        && filter(param, filter))
                .sorted((p1, p2) -> p1.getId().compareTo(p2.getId()))
                .forEach(param -> all.add(param));
        others.stream()
                .filter(param -> filter(param, filter))
                .sorted((p1, p2) -> p1.getId().compareTo(p2.getId()))
                .forEach(param -> all.add(param));

        return all;
    }
    
    private boolean filter(Parameter param, String filter) {
        return param.getId().contains(filter)
                || param.getRelatedElement().getId().contains(filter)
                || param.getRelatedElement().getName().contains(filter)
                || ((IDataElement) param.getRelatedElement()).getLabelText().contains(filter);
    }

    public List<DataTransition> getReferenceChoices(String filter) {
        List<DataTransition> list = new ArrayList();
        dataService.getModel().getTransitions().stream()
                .filter(transition -> transition.getId().toLowerCase().contains(filter)
                        || transition.getName().toLowerCase().contains(filter)
                        || ((DataTransition) transition).getLabelText().toLowerCase().contains(filter))
                .sorted((t1, t2) -> t1.toString().compareTo(t2.toString()))
                .forEach(transition -> list.add((DataTransition) transition));
        return list;
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

        IElement element;

        if ((element = model.getElement(candidate)) != null) {
            switch (element.getElementType()) {
                case PLACE:
                    return CreateReferencingParameter(model, candidate, "'" + element.getId() + "'.t", element);
                case TRANSITION:
                    return CreateReferencingParameter(model, candidate, "'" + element.getId() + "'.actualSpeed", element);
            }

        } else if (candidate.matches(regexParamPlaceFlowNow) || candidate.matches(regexParamPlaceFlowTotal)) {

            String[] tmp = candidate.split("_");
            IElement source, target, arc;
            String idSource, idTarget, value;
            int index, arcIndex;

            idSource = tmp[0];
            index = 1;
            while (((source = model.getElement(idSource)) == null && source instanceof INode) && index < tmp.length) {
                idSource = idSource + "_" + tmp[index];
                index++;
            }

            if (source != null && index < tmp.length - 2) {

                idTarget = tmp[index];
                while (((target = model.getElement(idTarget)) == null && target instanceof INode) && index < tmp.length) {
                    idTarget = idTarget + "_" + tmp[index];
                    index++;
                }

                if (target != null && index == tmp.length - 1) {

                    arc = model.getElement(dataService.getArcId((IDataNode) source, (IDataNode) target));
                    if (arc != null && arc instanceof IArc) {

                        arcIndex = 1;
                        switch (source.getElementType()) {

                            case PLACE:
                                for (IArc a : ((IArc) arc).getSource().getArcsOut()) { // place -> transition
                                    if (a.equals(arc)) {
                                        break;
                                    }
                                    arcIndex++;
                                }
                                value = "'" + idSource + "'.tokenFlow.outflow[" + arcIndex + "]";
                                break;

                            case TRANSITION:
                                for (IArc a : ((IArc) arc).getSource().getArcsIn()) { // transition -> place
                                    if (a.equals(arc)) {
                                        break;
                                    }
                                    arcIndex++;
                                }
                                value = "'" + idTarget + "'.tokenFlow.inflow[" + arcIndex + "]";
                                break;

                            default:
                                throw new ParameterException("Cannot get parameter. Unexpected element type for arc source. '" + source.getElementType() + "' in candidate '" + candidate + "'.");
                        }

                        if (candidate.matches(regexParamPlaceFlowTotal)) {
                            return CreateReferencingParameter(model, candidate, value, arc);
                        } else {
                            return CreateReferencingParameter(model, candidate, "der(" + value + ")", arc);
                        }
                    }
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
     * @throws ParameterException
     */
    private Parameter CreateReferencingParameter(Model model, String id, String value, IElement element) throws ParameterException {
        Parameter param = new Parameter(id, value, "", Parameter.Type.REFERENCE, element);
        add(model, param);
        return param;
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
            dataService.getModel().remove(param);
        }
    }

    /**
     * Validates a function in relation to a given element. Ensures that all
     * used parameters exist and that their access is not restricted (i.e.
     * access to LOCAL parameters of a different element).
     *
     * @param function
     * @param element
     * @throws ParameterException
     */
    public void ValidateFunction(IDataElement element, String function) throws ParameterException {
        ValidateFunction(dataService.getModel(), element, function);
    }

    /**
     * Validates a function in relation to a given element. Ensures that all
     * used parameters exist and that their access is not restricted (i.e.
     * access to LOCAL parameters of a different element).
     *
     * @param model
     * @param function
     * @param element
     * @throws ParameterException
     */
    public void ValidateFunction(Model model, IElement element, String function) throws ParameterException {

        Parameter param;
        String[] candidates;

        function = function.replace(" ", "");
        candidates = function.split(functionBuilder.getOperatorExtRegex());

        for (String candidate : candidates) {
            if (!candidate.matches("")) {
                if (!candidate.matches(functionBuilder.getNumberRegex())) { // candidate is NaN

                    param = element.getLocalParameter(candidate);
                    if (param == null) {
                        param = model.getParameter(candidate);
                    }
                    if (param == null) {
                        param = getReferencingParameter(model, candidate);
                    }
                    if (param == null) {
                        throw new ParameterException("Parameter '" + candidate + "' does not exist");
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
     * @throws ParameterException
     */
    public void ValidateRemoval(Parameter param) throws ParameterException {
        if (!param.getUsingElements().isEmpty()) {
            throw new ParameterException("Cannot delete parameter! It is referenced by another element.");
        }
    }

    /**
     * Validates the removal of a data element. Removal is valid if the element
     * is not related to any parameters that are referenced by other elements.
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
                throw new ParameterException(element.getId() + "'s parameter '" + param.getId() + "' is referenced by another element.");
            }
            param.getUsingElements().add(element);
        } else {
            if (!param.getUsingElements().isEmpty()) {
                throw new ParameterException(element.getId() + "'s parameter '" + param.getId() + "' is referenced by another element.");
            }
        }
    }
}
