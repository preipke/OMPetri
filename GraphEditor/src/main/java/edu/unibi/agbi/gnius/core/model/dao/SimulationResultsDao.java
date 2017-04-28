/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.dao;

import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.model.entity.simulation.SimulationLineChartData;
import edu.unibi.agbi.petrinet.entity.IElement;
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
public class SimulationResultsDao
{
    private final ObservableList<Simulation> simulations;
    private final Map<LineChart, ObservableList<SimulationLineChartData>> simulationChartTableLists;
    private final Map<Simulation, Map<IElement, Map<String, XYChart.Series>>> simulationChartDataSeries;
    
    public SimulationResultsDao() {
        simulations = FXCollections.observableArrayList();
        simulationChartTableLists = new HashMap();
        simulationChartDataSeries = new HashMap();
    }
    
    public void add(Simulation simulation) {
        simulations.add(simulation);
    }
    
    public void add(LineChart lineChart, ObservableList list) {
        simulationChartTableLists.put(lineChart, list);
    }
    
    public void add(SimulationLineChartData data) {
        if (!simulationChartDataSeries.containsKey(data.getSimulation())) {
            simulationChartDataSeries.put(data.getSimulation(), new HashMap());
        }
        if (!simulationChartDataSeries.get(data.getSimulation()).containsKey(data.getElement())) {
            simulationChartDataSeries.get(data.getSimulation()).put(data.getElement(), new HashMap());
        }
        simulationChartDataSeries.get(data.getSimulation()).get(data.getElement()).put(data.getValue(), data.getSeries());
    }
    
    public void add(LineChart lineChart, SimulationLineChartData data) {
        simulationChartTableLists.get(lineChart).add(data);
    }
    
    public boolean contains(LineChart lineChart) {
        return simulationChartTableLists.containsKey(lineChart);
    }
    
    public boolean contains(LineChart lineChart, SimulationLineChartData data) {
        if (!simulationChartTableLists.containsKey(lineChart)) {
            return false;
        }
        return simulationChartTableLists.get(lineChart).contains(data);
    }
    
    public boolean remove(LineChart lineChart, SimulationLineChartData data) {
        return simulationChartTableLists.get(lineChart).remove(data);
    }
    
    public ObservableList<Simulation> getSimulations() {
        return simulations;
    }
}
