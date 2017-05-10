/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.simulation;

import edu.unibi.agbi.petrinet.model.References;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import edu.unibi.agbi.petrinet.entity.IElement;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 *
 * @author PR
 */
public class Simulation
{
    private final LocalDateTime dateTime;
    private final String authorName;
    private final String modelName;
    private final String[] variables;
    private final References variableReferences;
    private final List<Object>[] results;

    public Simulation(String authorName, String modelName, String[] variables, References variableReferences) {
        this.dateTime = LocalDateTime.now();
        this.authorName = authorName;
        this.modelName = modelName;
        this.variables = variables;
        this.variableReferences = variableReferences;
        this.results = new ArrayList[variables.length];
        for (int i = 0; i < results.length; i++) {
            this.results[i] = new ArrayList();
        }
    }

    /**
     * Adds a result and appends it to the existing result lists.
     *
     * @param data
     */
    public void addResult(Object[] data) {
        for (int i = 0; i < results.length; i++) {
            results[i].add(data[i]);
        }
    }

    /**
     * Gets simulation author's name.
     *
     * @return
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * Gets the simulation model's name.
     *
     * @return
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Gets the date and time at which this simulation was performed.
     *
     * @return
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Gets the available variables.
     *
     * @return
     */
    public String[] getVariables() {
        return variables;
    }

    /**
     * Gets the variable references.
     *
     * @return
     */
    public Map<IElement, List<String>> getElementFilterReferences() {
        return variableReferences.getElementFilterReferences();
    }

    /**
     * Gets the element references.
     *
     * @return
     */
    public Map<String,IElement> getFilterElementReferences() {
        return variableReferences.getFilterElementReferences();
    }

    /**
     * Gets the results. The lists at each index represents the data for a
     * variable name at the exact same index position in the variables list.
     *
     * @return
     */
    public List<Object>[] getResults() {
        return results;
    }

    @Override
    public boolean equals(Object object) {

        if (object == null) {
            return false;
        }
        if (!(object instanceof Simulation)) {
            return false;
        }

        Simulation simulation = (Simulation) object;

        if (!simulation.getDateTime().isEqual(dateTime)) {
            return false;
        }

        if (!modelName.contentEquals(simulation.getModelName())) {
            return false;
        }

        return authorName.contentEquals(simulation.getAuthorName());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.dateTime);
        hash = 59 * hash + Objects.hashCode(this.authorName);
        hash = 59 * hash + Objects.hashCode(this.modelName);
        return hash;
    }

    @Override
    public String toString() {
        return dateTime.format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss")) + " " + getModelName();
    }
}
