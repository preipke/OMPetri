/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.dao;

import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.model.entity.simulation.SimulationData;
import edu.unibi.agbi.petrinet.entity.IElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Repository;

/**
 *
 * @author PR
 */
@Repository
public class ResultsDao
{
    private final ObservableList<Simulation> simulations;
    private final Map<LineChart, TableView<SimulationData>> chartSimulationDataTableViews;
    private final Map<LineChart, Map<String, Map<IElement, Set<String>>>> chartSimulationDataAutoAdd;
    
    public ResultsDao() {
        simulations = FXCollections.observableArrayList();
        chartSimulationDataAutoAdd = new HashMap();
        chartSimulationDataTableViews = new HashMap();
    }
    
    public void add(Simulation simulation) {
        simulations.add(simulation);
    }
    
    public void add(LineChart lineChart, TableView<SimulationData> tableView) {
        chartSimulationDataTableViews.put(lineChart, tableView);
    }
    
    public void add(LineChart lineChart, SimulationData data) {
        chartSimulationDataTableViews.get(lineChart).getItems().add(data);
    }
    
    public void addForAutoAdding(LineChart lineChart, SimulationData data) {
        if (!chartSimulationDataAutoAdd.containsKey(lineChart)) {
            chartSimulationDataAutoAdd.put(lineChart, new HashMap());
        }
        if (!chartSimulationDataAutoAdd.get(lineChart).containsKey(data.getSimulation().getModelId())) {
            chartSimulationDataAutoAdd.get(lineChart).put(data.getSimulation().getModelId(), new HashMap());
        }
        if (!chartSimulationDataAutoAdd.get(lineChart).get(data.getSimulation().getModelId()).containsKey(data.getElement())) {
            chartSimulationDataAutoAdd.get(lineChart).get(data.getSimulation().getModelId()).put(data.getElement(), new TreeSet());
        }
        chartSimulationDataAutoAdd.get(lineChart).get(data.getSimulation().getModelId()).get(data.getElement()).add(data.getVariable());
    }
    
    public boolean contains(LineChart lineChart) {
        return chartSimulationDataTableViews.containsKey(lineChart);
    }
    
    public boolean contains(LineChart lineChart, SimulationData data) {
        if (!chartSimulationDataTableViews.containsKey(lineChart)) {
            return false;
        }
        return chartSimulationDataTableViews.get(lineChart).getItems().contains(data);
    }
    
    public boolean containsForAutoAdding(LineChart lineChart, SimulationData data) {
        
        Map<String, Map<IElement, Set<String>>> modelsToAutoAdd;
        Map<IElement, Set<String>> elementsToAutoAdd;
        Set<String> valuesToAutoAdd;
        
        modelsToAutoAdd = chartSimulationDataAutoAdd.get(lineChart);
        
        if (modelsToAutoAdd != null) {
            
            elementsToAutoAdd = modelsToAutoAdd.get(data.getSimulation().getModelId());
            
            if (elementsToAutoAdd != null) {
                
                valuesToAutoAdd = elementsToAutoAdd.get(data.getElement());
                
                if (valuesToAutoAdd != null) {
                    
                    return valuesToAutoAdd.contains(data.getVariable());
                }
            }
        }
        return false;
    }
    
    public void remove(LineChart lineChart) {
        chartSimulationDataTableViews.remove(lineChart);
        chartSimulationDataAutoAdd.remove(lineChart);
    }
    
    public boolean remove(LineChart lineChart, SimulationData data) {
        return chartSimulationDataTableViews.get(lineChart).getItems().remove(data);
    }
    
    public void removeFromAutoAdding(LineChart lineChart, SimulationData data) {
        if (chartSimulationDataAutoAdd.containsKey(lineChart)) {
            if (chartSimulationDataAutoAdd.get(lineChart).containsKey(data.getSimulation().getModelId())) {
                if (chartSimulationDataAutoAdd.get(lineChart).get(data.getSimulation().getModelId()).containsKey(data.getElement())) {
                    chartSimulationDataAutoAdd.get(lineChart).get(data.getSimulation().getModelId()).get(data.getElement()).remove(data.getVariable());
                }
            }
        }
    }
    
    public Set<LineChart> getLineChartWithAutoAdding() {
        return chartSimulationDataAutoAdd.keySet();
    }
    
    public Map<String, Map<IElement, Set<String>>> getDataAutoAdd(LineChart lineChart) {
        return chartSimulationDataAutoAdd.get(lineChart);
    }
    
    public TableView<SimulationData> getChartTable(LineChart lineChart) {
        return chartSimulationDataTableViews.get(lineChart);
    }
    
    public ObservableList<SimulationData> getChartResultsList(LineChart lineChart) {
        return chartSimulationDataTableViews.get(lineChart).getItems();
    }
    
    public ObservableList<Simulation> getSimulations() {
        return simulations;
    }
}
