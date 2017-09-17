/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.result;

import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.service.exception.ResultsException;
import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.util.References;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A data structure for storing the results of a simulation.
 *
 * @author PR
 */
public class SimulationResult
{
    private final DataDao dao;
    private final String[] variables;
    private final References variableReferences;
    private final Map<String, List<Object>> resultsMap;
    private LocalDateTime dateTime;

    public SimulationResult(DataDao dataDao, String[] variables, References variableReferences) throws ResultsException {
        this.dao = dataDao;
        this.dateTime = LocalDateTime.now();
        this.variables = variables;
        this.variableReferences = variableReferences;
        this.resultsMap = new HashMap();
        for (String variable : variables) {
            if (this.resultsMap.containsKey(variable)) {
                throw new ResultsException("Results data structure already contains list for variable '" + variable + "'! Data integrity not given.");
            }
            this.resultsMap.put(variable, new ArrayList());
        }
    }

    /**
     * Adds data. Appends data to the existing data lists for each variable.
     *
     * @param data
     * @throws edu.unibi.agbi.gnius.core.service.exception.ResultsException
     */
    public void addData(Object[] data) throws ResultsException {
        if (data.length != variables.length) {
            throw new ResultsException("Incoming data size does not match size of available variables! Data integrity not given.");
        }
        for (int i = 0; i < data.length; i++) {
            resultsMap.get(variables[i]).add(data[i]);
        }
    }

    public DataDao getDao() {
        return dao;
    }

    /**
     * Gets the date and time at which this simulation was performed.
     *
     * @return
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Gets the filter variables related to an element.
     *
     * @param element
     * @return
     */
    public Set<String> getElementFilter(IElement element) {
        return variableReferences.getElementToFilterReferences().get(element);
    }

    /**
     * Gets the referenced element for a filter variable.
     *
     * @param variable
     * @return
     */
    public IElement getFilterElement(String variable) {
        return variableReferences.getFilterToElementReferences().get(variable);
    }

    /**
     * Gets all elements that are referenced in the simulation data.
     *
     * @return
     */
    public Collection<IElement> getElements() {
        return variableReferences.getElementToFilterReferences().keySet();
    }

    /**
     * Gets data associated to a filter variable. 
     *
     * @param variable
     * @return
     */
    public List<Object> getData(String variable) {
        return resultsMap.get(variable);
    }

    public List<Object> getTimeData() {
        return resultsMap.get("time");
    }

    @Override
    public boolean equals(Object object) {

        if (object == null) {
            return false;
        }
        if (!(object instanceof SimulationResult)) {
            return false;
        }

        SimulationResult simulation = (SimulationResult) object;

        if (!simulation.getDateTime().isEqual(dateTime)) {
            return false;
        }

        return dao.getModelId().contentEquals(simulation.getDao().getModelId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.dateTime);
        hash = 59 * hash + Objects.hashCode(this.dao.getModelId());
        return hash;
    }

    @Override
    public String toString() {
        return dateTime.format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"));
    }

    public String toStringShort() {
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
