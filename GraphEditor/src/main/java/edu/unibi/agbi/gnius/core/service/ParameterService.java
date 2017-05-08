/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.core.exception.ParameterServiceException;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class ParameterService
{
    private final DataDao dataDao;
    
    @Autowired
    public ParameterService(DataDao dataDao) {
        this.dataDao = dataDao;
    }

    /**
     * Attempts to add the given parameter.
     *
     * @param param
     * @throws ParameterServiceException
     */
    public void add(Parameter param) throws ParameterServiceException {
        if (dataDao.containsAndNotEqual(param)) {
            throw new ParameterServiceException("Conflict! Another parameter has already been stored using the same ID!");
        }
        dataDao.add(param);
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
}
