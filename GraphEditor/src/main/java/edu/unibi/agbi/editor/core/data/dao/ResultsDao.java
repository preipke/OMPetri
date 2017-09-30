/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.data.dao;

import edu.unibi.agbi.editor.core.data.entity.result.Simulation;
import edu.unibi.agbi.editor.core.data.entity.result.ResultSet;
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
    // create storage for result
    private final ObservableList<Simulation> simulations;
    private final Map<String, Map<Simulation, Map<String, Map<String,ResultSet>>>> resultSetStorageMap;
    
    // data related to resultsviewer linecharts
    private final Map<LineChart, TableView<ResultSet>> chartSimulationDataTableViews;
    private final Map<LineChart, Map<String, Map<IElement, Set<String>>>> chartSimulationDataAutoAdd;
    
    public ResultsDao() {
        simulations = FXCollections.observableArrayList();
        resultSetStorageMap = new HashMap();
        chartSimulationDataAutoAdd = new HashMap();
        chartSimulationDataTableViews = new HashMap();
    }
    
    public void add(ResultSet resultSet) {
        
        String modelId = resultSet.getSimulation().getDao().getModelId();
        if (!resultSetStorageMap.containsKey(modelId)) {
            resultSetStorageMap.put(modelId, new HashMap());
        }

        Simulation simulationResult = resultSet.getSimulation();
        if (!resultSetStorageMap.get(modelId).containsKey(simulationResult)) {
            resultSetStorageMap.get(modelId).put(simulationResult, new HashMap());

        }
        String elementId = resultSet.getElement().getId();
        if (!resultSetStorageMap.get(modelId).get(simulationResult).containsKey(elementId)) {
            resultSetStorageMap.get(modelId).get(simulationResult).put(elementId, new HashMap());
        }
        
        resultSetStorageMap.get(modelId).get(simulationResult).get(elementId).put(resultSet.getVariable(), resultSet);
    }
    
    public void add(Simulation simulation) {
        simulations.add(simulation);
    }
    
    public void add(LineChart lineChart, TableView<ResultSet> tableView) {
        chartSimulationDataTableViews.put(lineChart, tableView);
    }
    
    public void add(LineChart lineChart, ResultSet data) {
        chartSimulationDataTableViews.get(lineChart).getItems().add(data);
    }
    
    public void addForAutoAdding(LineChart lineChart, ResultSet data) {
        if (!chartSimulationDataAutoAdd.containsKey(lineChart)) {
            chartSimulationDataAutoAdd.put(lineChart, new HashMap());
        }
        if (!chartSimulationDataAutoAdd.get(lineChart).containsKey(data.getSimulation().getDao().getModelId())) {
            chartSimulationDataAutoAdd.get(lineChart).put(data.getSimulation().getDao().getModelId(), new HashMap());
        }
        if (!chartSimulationDataAutoAdd.get(lineChart).get(data.getSimulation().getDao().getModelId()).containsKey(data.getElement())) {
            chartSimulationDataAutoAdd.get(lineChart).get(data.getSimulation().getDao().getModelId()).put(data.getElement(), new TreeSet());
        }
        chartSimulationDataAutoAdd.get(lineChart).get(data.getSimulation().getDao().getModelId()).get(data.getElement()).add(data.getVariable());
    }
    
    public boolean contains(ResultSet resultSet) {
        
        String modelId = resultSet.getSimulation().getDao().getModelId();
        if (resultSetStorageMap.containsKey(modelId)) {
            
            Simulation simulationResult = resultSet.getSimulation();
            if (resultSetStorageMap.get(modelId).containsKey(simulationResult)) {
                
                String elementId = resultSet.getElement().getId();
                if (resultSetStorageMap.get(modelId).get(simulationResult).containsKey(elementId)) {
                    
                    return resultSetStorageMap.get(modelId).get(simulationResult).get(elementId).containsKey(resultSet.getVariable());
                }
            }
        }
        return false;
    }
    
    public boolean contains(Simulation simulationResult) {
        return simulations.contains(simulationResult);
    }
    
    public boolean contains(LineChart lineChart) {
        return chartSimulationDataTableViews.containsKey(lineChart);
    }
    
    public boolean contains(LineChart lineChart, ResultSet resultSet) {
        if (!chartSimulationDataTableViews.containsKey(lineChart)) {
            return false;
        }
        return chartSimulationDataTableViews.get(lineChart).getItems().contains(resultSet);
    }
    
    public boolean containsForAutoAdding(LineChart lineChart, ResultSet data) {
        
        Map<String, Map<IElement, Set<String>>> modelsToAutoAdd;
        Map<IElement, Set<String>> elementsToAutoAdd;
        Set<String> valuesToAutoAdd;
        
        modelsToAutoAdd = chartSimulationDataAutoAdd.get(lineChart);
        
        if (modelsToAutoAdd != null) {
            
            elementsToAutoAdd = modelsToAutoAdd.get(data.getSimulation().getDao().getModelId());
            
            if (elementsToAutoAdd != null) {
                
                valuesToAutoAdd = elementsToAutoAdd.get(data.getElement());
                
                if (valuesToAutoAdd != null) {
                    
                    return valuesToAutoAdd.contains(data.getVariable());
                }
            }
        }
        return false;
    }
    
    public ResultSet get(ResultSet resultSet) {
        
        String modelId = resultSet.getSimulation().getDao().getModelId();
        if (resultSetStorageMap.containsKey(modelId)) {
            
            Simulation simulationResult = resultSet.getSimulation();
            if (resultSetStorageMap.get(modelId).containsKey(simulationResult)) {
                
                String elementId = resultSet.getElement().getId();
                if (resultSetStorageMap.get(modelId).get(simulationResult).containsKey(elementId)) {
                    
                    return resultSetStorageMap.get(modelId).get(simulationResult).get(elementId).get(resultSet.getVariable());
                }
            }
        }
        return null;
    }
    
    public void remove(ResultSet resultSet) {
        
        String modelId = resultSet.getSimulation().getDao().getModelId();
        if (resultSetStorageMap.containsKey(modelId)) {

            Simulation simulationResult = resultSet.getSimulation();
            if (resultSetStorageMap.get(modelId).containsKey(simulationResult)) {

                String elementId = resultSet.getElement().getId();
                if (resultSetStorageMap.get(modelId).get(simulationResult).containsKey(elementId)) {
                    
                    resultSetStorageMap.get(modelId).get(simulationResult).get(elementId).remove(resultSet.getVariable());
                }
            }
        }
    }
    
    public void remove(LineChart lineChart) {
        chartSimulationDataTableViews.remove(lineChart);
        chartSimulationDataAutoAdd.remove(lineChart);
    }
    
    public boolean remove(LineChart lineChart, ResultSet data) {
        return chartSimulationDataTableViews.get(lineChart).getItems().remove(data);
    }
    
    public void removeFromAutoAdding(LineChart lineChart, ResultSet data) {
        if (chartSimulationDataAutoAdd.containsKey(lineChart)) {
            if (chartSimulationDataAutoAdd.get(lineChart).containsKey(data.getSimulation().getDao().getModelId())) {
                if (chartSimulationDataAutoAdd.get(lineChart).get(data.getSimulation().getDao().getModelId()).containsKey(data.getElement())) {
                    chartSimulationDataAutoAdd.get(lineChart).get(data.getSimulation().getDao().getModelId()).get(data.getElement()).remove(data.getVariable());
                }
            }
        }
    }
    
    public Set<LineChart> getLineChartsWithAutoAdding() {
        return chartSimulationDataAutoAdd.keySet();
    }
    
    public Map<String, Map<IElement, Set<String>>> getDataAutoAdd(LineChart lineChart) {
        return chartSimulationDataAutoAdd.get(lineChart);
    }
    
    public TableView<ResultSet> getChartTable(LineChart lineChart) {
        return chartSimulationDataTableViews.get(lineChart);
    }
    
    public ObservableList<ResultSet> getChartResultsList(LineChart lineChart) {
        return chartSimulationDataTableViews.get(lineChart).getItems();
    }
    
    public ObservableList<Simulation> getSimulationResults() {
        return simulations;
    }
}
