/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.model.dao.ResultsDao;
import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.model.entity.simulation.SimulationData;
import edu.unibi.agbi.gnius.core.exception.ResultsServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.petrinet.entity.IElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class ResultsService
{
    private final ResultsDao resultsDao;

    @Autowired private MessengerService messengerService;
    
    @Value("${regex.value.fire}") private String valueChoiceFire;
    @Value("${regex.value.speed}") private String valueChoiceSpeed;
    @Value("${regex.value.token}") private String valueChoiceToken;
    @Value("${regex.value.tokenIn.actual}") private String valueChoiceTokenInActual;
    @Value("${regex.value.tokenIn.total}") private String valueChoiceTokenInTotal;
    @Value("${regex.value.tokenOut.actual}") private String valueChoiceTokenOutActual;
    @Value("${regex.value.tokenOut.total}") private String valueChoiceTokenOutTotal;

    @Autowired
    public ResultsService(ResultsDao resultsDao) {
        this.resultsDao = resultsDao;
        this.resultsDao.getSimulations().addListener(new ListChangeListener()
        {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                change.next();
                if (change.wasAdded()) {
                    try {
                        Simulation simulation
                                = ResultsService.this.resultsDao.getSimulations()
                                        .get(ResultsService.this.resultsDao.getSimulations().size() - 1);
                        AutoAddData(simulation);
                    } catch (ResultsServiceException ex) {
                        messengerService.addException("Exception while auto adding results data!", ex);
                    }
                }
            }
        });
    }

    /**
     * Gets the data for all performed simulations.
     *
     * @return
     */
    public synchronized ObservableList<Simulation> getSimulations() {
        return resultsDao.getSimulations();
    }

    /**
     * Adds the given line chart and related table item list to the storage.
     *
     * @param lineChart
     * @param tableView
     * @throws ResultsServiceException
     */
    public synchronized void add(LineChart lineChart, TableView tableView) throws ResultsServiceException {
        if (resultsDao.contains(lineChart)) {
            throw new ResultsServiceException("Line chart has already been stored! Cannot overwrite existing data list.");
        }
        resultsDao.add(lineChart, tableView);
    }

    /**
     * Attempts to add data to a line charts corresponding table.
     *
     * @param lineChart
     * @param data
     * @throws ResultsServiceException
     */
    public synchronized void add(LineChart lineChart, SimulationData data) throws ResultsServiceException {
        if (resultsDao.contains(lineChart, data)) {
            throw new ResultsServiceException("Duplicate entry for line chart");
        }
        resultsDao.add(lineChart, data);
    }

    /**
     * Drops all data related to a line chart.
     *
     * @param lineChart the line chart that will be dropped
     */
    public synchronized void drop(LineChart lineChart) {
        resultsDao.remove(lineChart);
    }

    /**
     * Drops the given data from the given chart and the table.
     *
     * @param lineChart the line chart that will be modified
     * @param data      the data to be hidden and removed from the also given
     *                  chart
     */
    public synchronized void drop(LineChart lineChart, SimulationData data) {
        hide(lineChart, data);
        resultsDao.remove(lineChart, data);
    }

    /**
     * Get simulation data related to a line chart.
     *
     * @param lineChart
     * @return
     */
    public synchronized List<SimulationData> getChartData(LineChart lineChart) {
        return resultsDao.getChartResultsList(lineChart);
    }

    /**
     * Removes the given data from the given chart.
     *
     * @param lineChart
     * @param data
     */
    public synchronized void hide(LineChart lineChart, SimulationData data) {
        lineChart.getData().remove(data.getSeries());
        data.setShown(false);
    }

    /**
     * Shows the given data in the given chart.
     *
     * @param lineChart
     * @param data
     * @throws ResultsServiceException
     */
    public synchronized void show(LineChart lineChart, SimulationData data) throws ResultsServiceException {
        updateSeries(data);
        if (data.getSeries() != null) {
            if (!lineChart.getData().contains(data.getSeries())) {
                lineChart.getData().add(data.getSeries());
            }
            data.setShown(true);
        } else {
            throw new ResultsServiceException("No chart data available");
        }
    }
    
    public synchronized void addForAutoAdding(LineChart lineChart, SimulationData data) {
        resultsDao.addForAutoAdding(lineChart, data);
    }
    
    public synchronized boolean containsForAutoAdding(LineChart lineChart, SimulationData data) {
        return resultsDao.containsForAutoAdding(lineChart, data);
    }
    
    public synchronized void removeFromAutoAdding(LineChart lineChart, SimulationData data) {
        resultsDao.removeFromAutoAdding(lineChart, data);
    }
    
    public synchronized void UpdateAutoAddedData() throws ResultsServiceException {
        for (LineChart lineChart : resultsDao.getLineChartsWithAutoAdding()) {
            for (SimulationData data : resultsDao.getChartTable(lineChart).getItems()) {
                updateSeries(data);
            }
            resultsDao.getChartTable(lineChart).refresh();
        }
    }
    
    public String getValueName(String value, Simulation simulation) {
        if (value.matches(valueChoiceFire)) {
            return "Firing";
        } else if (value.matches(valueChoiceSpeed)) {
            return "Speed";
        } else if (value.matches(valueChoiceToken)) {
            return "Token";
        } else if (value.matches(valueChoiceTokenInActual)) {
            DataArc arc = (DataArc) simulation.getFilterElementReferences().get(value);
            return "Incoming from " + arc.getSource().toString() + " [ACTUAL]";
        } else if (value.matches(valueChoiceTokenInTotal)) {
            DataArc arc = (DataArc) simulation.getFilterElementReferences().get(value);
            return "Incoming from " + arc.getSource().toString() + " [TOTAL]";
        } else if (value.matches(valueChoiceTokenOutActual)) {
            DataArc arc = (DataArc) simulation.getFilterElementReferences().get(value);
            return "Outgoing to " + arc.getTarget().toString() + " [ACTUAL]";
        } else if (value.matches(valueChoiceTokenOutTotal)) {
            DataArc arc = (DataArc) simulation.getFilterElementReferences().get(value);
            return "Outgoing to " + arc.getTarget().toString() + " [TOTAL]";
        } else {
            return null;
        }
    }
    
    public Map<String,List<String>> getSharedValues(Simulation simulation, List<IElement> elements) {
        
        Map<String,List<String>> valuesTmp, valuesShared = null;
        
        List<String> values, valuesRemoved;
        String name;
        
        for (IElement element : elements) {
            
            values = simulation.getElementFilterReferences().get(element);
            valuesTmp = new HashMap();
            
            for (String value : values) {
                
                name = getValueName(value, simulation);
                
                if (!valuesTmp.containsKey(name)) {
                    valuesTmp.put(name, new ArrayList());
                }
                valuesTmp.get(name).add(value);
            }
            
            if (valuesShared == null) {
                
                valuesShared = valuesTmp;
                
            } else {
                
                valuesRemoved = new ArrayList();
                
                for (String key : valuesShared.keySet()) {
                    if (valuesTmp.containsKey(key)) {
                        valuesShared.get(key).addAll(valuesTmp.get(key));
                    } else {
                        valuesRemoved.add(key);
                    }
                }
                for (String key : valuesRemoved) {
                    valuesShared.remove(key);
                }
            }
        }
        return valuesShared;
    }
    
    private synchronized void AutoAddData(Simulation simulation) throws ResultsServiceException {

        Map<String, Map<IElement, Set<String>>> modelsToAutoAdd;
        Map<IElement, Set<String>> elementsToAutoAdd;
        Set<String> valuesToAutoAdd;
        SimulationData data;

        // validate all active charts
        for (LineChart lineChart : resultsDao.getLineChartsWithAutoAdding()) {

            modelsToAutoAdd = resultsDao.getDataAutoAdd(lineChart);
            
            if (modelsToAutoAdd != null) {

                elementsToAutoAdd = modelsToAutoAdd.get(simulation.getModelId());

                if (elementsToAutoAdd != null) {
                    
                    // validate elements chosen for auto adding to be available
                    for (IElement elem : elementsToAutoAdd.keySet()) {

                        valuesToAutoAdd = elementsToAutoAdd.get(elem);

                        if (valuesToAutoAdd != null) {

                            // validate values chosen for auto adding to be available
                            for (String valueToAutoAdd : valuesToAutoAdd) {

                                if (simulation.getFilterElementReferences().containsKey(valueToAutoAdd)) {

                                    // create and add data to chart
                                    data = new SimulationData(simulation, elem, valueToAutoAdd);

                                    try {
                                        add(lineChart, data);
                                    } catch (ResultsServiceException ex) {
                                        System.out.println("Duplicate results entry");
                                    }
                                    show(lineChart, data);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the series for the given data object. Loads data from the
     * simulation and adds all additional entries to the series. Updates the
     * related chart.
     *
     * @param data
     * @throws ResultsServiceException
     */
    private synchronized void updateSeries(SimulationData data) throws ResultsServiceException {

        XYChart.Series seriesOld = data.getSeries();
        List<Object>[] results = data.getSimulation().getResults();

        if (seriesOld == null || results[0].size() > seriesOld.getData().size()) { // update only if additional values available

            int variableIndex = 0;
            String[] variables = data.getSimulation().getVariables();
            String variableTarget = data.getVariable();
            XYChart.Series seriesNew = new XYChart.Series();

            for (String variable : variables) {
                if (variableTarget.contentEquals(variable)) {
                    break;
                }
                variableIndex++;
            }

            if (variableIndex > variables.length) {
                throw new ResultsServiceException("Filter variable '" + variableTarget + "' cannot be found in the references!");
            } else if (variableIndex > results.length) {
                throw new ResultsServiceException("Filter variable '" + variableTarget + "' was found but no results available!");
            }

            if (seriesOld != null) {
                seriesNew.getData().addAll(seriesOld.getData());
            }

            for (int i = seriesNew.getData().size(); i < results[variableIndex].size(); i++) {
                seriesNew.getData().add(new XYChart.Data(
                        (Number) results[0].get(i),
                        (Number) results[variableIndex].get(i)
                ));
            }
            seriesNew.setName("'" + data.getElementId() + "' (" + data.getSimulation().toString() + ")");

            if (seriesOld != null) {
                XYChart chart = seriesOld.getChart();
                if (chart != null) {
                    chart.getData().remove(seriesOld);
                    chart.getData().add(seriesNew);
                }
            }
            data.setSeries(seriesNew);
        }
    }
}
