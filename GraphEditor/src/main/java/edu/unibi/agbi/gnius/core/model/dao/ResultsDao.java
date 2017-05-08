/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.dao;

import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.model.entity.simulation.SimulationData;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.springframework.stereotype.Repository;

/**
 *
 * @author PR
 */
@Repository
public class ResultsDao
{
    private final ObservableList<Simulation> simulations;
    private final Map<LineChart, ObservableList<SimulationData>> chartSimulationDataLists;
    private final Map<Simulation, Map<String, Map<String, XYChart.Series>>> simulationDataMap;
    
    public ResultsDao() {
        simulations = FXCollections.observableArrayList();
        chartSimulationDataLists = new HashMap();
        simulationDataMap = new HashMap();
    }
    
    public void add(Simulation simulation) {
        simulations.add(simulation);
    }
    
    public void add(LineChart lineChart, ObservableList list) {
        chartSimulationDataLists.put(lineChart, list);
    }
    
    public void add(SimulationData data) {
        if (!simulationDataMap.containsKey(data.getSimulation())) {
            simulationDataMap.put(data.getSimulation(), new HashMap());
        }
        if (!simulationDataMap.get(data.getSimulation()).containsKey(data.getElementId())) {
            simulationDataMap.get(data.getSimulation()).put(data.getElementId(), new HashMap());
        }
        simulationDataMap.get(data.getSimulation()).get(data.getElementId()).put(data.getVariable(), data.getSeries());
    }
    
    public void add(LineChart lineChart, SimulationData data) {
        chartSimulationDataLists.get(lineChart).add(data);
    }
    
    public boolean contains(LineChart lineChart) {
        return chartSimulationDataLists.containsKey(lineChart);
    }
    
    public boolean contains(LineChart lineChart, SimulationData data) {
        if (!chartSimulationDataLists.containsKey(lineChart)) {
            return false;
        }
        return chartSimulationDataLists.get(lineChart).contains(data);
    }
    
    public boolean remove(LineChart lineChart, SimulationData data) {
        return chartSimulationDataLists.get(lineChart).remove(data);
    }
    
    public ObservableList<SimulationData> getResultsTableList(LineChart lineChart) {
        return chartSimulationDataLists.get(lineChart);
    }
    
    public ObservableList<Simulation> getSimulations() {
        return simulations;
    }
}
