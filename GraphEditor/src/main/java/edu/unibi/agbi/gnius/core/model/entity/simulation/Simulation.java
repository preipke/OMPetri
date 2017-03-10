/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.simulation;

import edu.unibi.agbi.petrinet.entity.IPN_Element;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PR
 */
public class Simulation
{
    private final LocalDateTime simulationTime;
    
    private final String[] simulationVariables;
    private final Map<String,IPN_Element> simulationVariableReferences;
    private final List<Object>[] simulationResults;
    
    private String simulationName;
    private String simulationAuthor;
    private String simulationDescription;
    
    public Simulation(String[] variables, Map<String,IPN_Element> variableReferences) {
        
        simulationTime = LocalDateTime.now();
        
        simulationVariables = variables;
        simulationVariableReferences = variableReferences;
        
        simulationResults = new ArrayList[simulationVariables.length];
        for (int i = 0; i < simulationResults.length; i++) {
            simulationResults[i] = new ArrayList();
        }
    }
    
    public void addResult(Object[] data) {
        for (int i = 0; i < simulationResults.length; i++) {
            simulationResults[i].add(data[i]);
        }
    }
    
    /**
     * Gets the author of the simulation.
     * @return 
     */
    public String getAuthor() {
        return simulationAuthor;
    }
    
    /**
     * Gets the description for the simulation.
     * @return 
     */
    public String getDescription() {
        return simulationDescription;
    }
    
    /**
     * Gets the name of the simulation.
     * @return 
     */
    public String getName() {
        return simulationName;
    }
    
    /**
     * Gets the date and time at which the simulation was performed.
     * @return 
     */
    public LocalDateTime getTime() {
        return simulationTime;
    }
    
    /**
     * Gets the available variables.
     * @return 
     */
    public String[] getVariables() {
        return simulationVariables;
    }
    
    /**
     * Gets the variable references.
     * @return 
     */
    public Map<String,IPN_Element> getVariableReferences() {
        return simulationVariableReferences;
    }
    
    /**
     * Gets the variable name associated results.
     * @return 
     */
    public List<Object>[] getResults() {
        return simulationResults;
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
        
        if (!simulation.getTime().isEqual(simulationTime)) {
            return false;
        }
        
        return simulation.getName().matches(simulationName);
    }
}
