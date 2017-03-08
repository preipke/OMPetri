/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.dao;

import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 *
 * @author PR
 */
@Repository
public class SimulationDao
{
    private final List<Simulation> simulations;
    
    public SimulationDao() {
        simulations = new ArrayList();
    }
    
    public void addSimulation(Simulation simulation) {
        simulations.add(simulation);
    }
    
    public Simulation getLatestSimulation() {
        return simulations.get(simulations.size() -1);
    }
    
    public List<Simulation> getSimulations() {
        return simulations;
    }
}
