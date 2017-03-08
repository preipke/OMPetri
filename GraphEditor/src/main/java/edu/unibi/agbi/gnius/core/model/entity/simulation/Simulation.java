/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.simulation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class Simulation
{
    private final LocalDateTime time;
    
    private final String[] variables;
    private final List<Object>[] results;
    
    private String name;
    private String author;
    private String description;
    
    public Simulation(String[] vars) {
        
        time = LocalDateTime.now();
        variables = vars;
        
        results = new ArrayList[variables.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = new ArrayList();
        }
    }
    
    public void addResult(Object[] data) {
        for (int i = 0; i < results.length; i++) {
            results[i].add(data[i]);
        }
    }
    
    public String[] getVariables() {
        return variables;
    }
    
    public List<Object>[] getResults() {
        return results;
    }
}
