/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.exception.ParameterServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.entity.INode;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
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

    @Value("${regex.param.ident.flow.actual}") private String regexParamPlaceFlowNow;
    @Value("${regex.param.ident.flow.total}") private String regexParamPlaceFlowTotal;

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
                    throw new ParameterServiceException("Conflict: Another parameter has already been stored using the same ID.");
                }
                model.add(param);
                if (Parameter.Type.LOCAL == param.getType()) {
                    if (element instanceof DataTransition) {
                        ((DataTransition) element).addParameter(param);
                    } else {
                        throw new ParameterServiceException("Trying to store LOCAL parameter for non-transition element.");
                    }
                }
                element.getRelatedParameterIds().add(param.getId());
            } else {
                throw new ParameterServiceException("A reference element is required for storing non global parameters.");
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
                    param.getRelatedElement().getRelatedParameterIds().remove(param.getId());
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
            parameters.addAll(((DataTransition) elem).getParameters());
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
        List<Parameter> restricted = new ArrayList();

        dataService.getModel().getTransitions().forEach(transition -> {
            if (transition.equals(element)) {
                locals.addAll(transition.getParameters());
            } else {
                restricted.addAll(transition.getParameters());
            }
        });

        locals.stream()
                .filter(param -> param.getId().contains(filter))
                .sorted((p1, p2) -> p1.getId().compareTo(p2.getId()))
                .forEach(param -> all.add(param));
        dataService.getModel().getParameters().stream()
                .filter(param -> param.getType() == Parameter.Type.GLOBAL
                        && param.getId().contains(filter))
                .sorted((p1, p2) -> p1.getId().compareTo(p2.getId()))
                .forEach(param -> all.add(param));
        restricted.stream()
                .filter(param -> param.getId().contains(filter))
                .sorted((p1, p2) -> p1.getId().compareTo(p2.getId()))
                .forEach(param -> all.add(param));

        return all;
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
     * @throws ParameterServiceException
     */
    private Parameter getReferencingParameter(Model model, String candidate) throws ParameterServiceException {

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
                                throw new ParameterServiceException("Cannot get parameter. Unexpected element type for arc source. '" + source.getElementType() + "' in candidate '" + candidate + "'.");
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
     * @throws ParameterServiceException
     */
    private Parameter CreateReferencingParameter(Model model, String id, String value, IElement element) throws ParameterServiceException {
        Parameter param = new Parameter(id, value, "", Parameter.Type.REFERENCE, element);
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
        if (param.getType() == Parameter.Type.LOCAL) {
            if (param.getRelatedElement() != null
                    && param.getRelatedElement() instanceof DataTransition) {
                ((DataTransition) param.getRelatedElement()).getParameters().remove(param);
            } else {
                throw new ParameterServiceException("Inconsistency found. LOCAL parameter related to non-transition element.");
            }
        } else {
            dataService.getModel().remove(param);
        }
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
    public void ValidateFunction(DataTransition element, String function) throws ParameterServiceException {
        ValidateFunction(dataService.getModel(), element, function);
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
    public void ValidateFunction(Model model, Transition transition, String function) throws ParameterServiceException {

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
