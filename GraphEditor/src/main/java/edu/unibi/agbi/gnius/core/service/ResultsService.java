/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.model.dao.ResultsDao;
import edu.unibi.agbi.gnius.core.model.entity.result.SimulationResult;
import edu.unibi.agbi.gnius.core.model.entity.result.ResultSet;
import edu.unibi.agbi.gnius.core.service.exception.ResultsException;
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
        this.resultsDao.getSimulationResults().addListener(new ListChangeListener()
        {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                change.next();
                if (change.wasAdded()) {
                    try {
                        SimulationResult simulation
                                = ResultsService.this.resultsDao.getSimulationResults()
                                        .get(ResultsService.this.resultsDao.getSimulationResults().size() - 1);
                        AutoAddData(simulation);
                    } catch (ResultsException ex) {
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
    public synchronized ObservableList<SimulationResult> getSimulationResults() {
        return resultsDao.getSimulationResults();
    }

    /**
     * Adds the given line chart and related table item list to the storage.
     *
     * @param lineChart
     * @param tableView
     * @throws ResultsException
     */
    public synchronized void add(LineChart lineChart, TableView tableView) throws ResultsException {
        if (resultsDao.contains(lineChart)) {
            throw new ResultsException("Line chart has already been stored! Cannot overwrite existing data list.");
        }
        resultsDao.add(lineChart, tableView);
    }

    /**
     * Attempts to add data to a line charts corresponding table.
     *
     * @param lineChart
     * @param data
     * @throws ResultsException
     */
    public synchronized void add(LineChart lineChart, ResultSet data) throws ResultsException {
        if (resultsDao.contains(lineChart, data)) {
            throw new ResultsException("Duplicate entry for line chart");
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
    public synchronized void drop(LineChart lineChart, ResultSet data) {
        hide(lineChart, data);
        resultsDao.remove(lineChart, data);
    }

    /**
     * Get simulation data related to a line chart.
     *
     * @param lineChart
     * @return
     */
    public synchronized List<ResultSet> getChartData(LineChart lineChart) {
        return resultsDao.getChartResultsList(lineChart);
    }

    /**
     * Removes the given data from the given chart.
     *
     * @param lineChart
     * @param data
     */
    public synchronized void hide(LineChart lineChart, ResultSet data) {
        lineChart.getData().remove(data.getSeries());
        data.setShown(false);
    }

    /**
     * Shows the given data in the given chart.
     *
     * @param lineChart
     * @param data
     * @throws ResultsException
     */
    public synchronized void show(LineChart lineChart, ResultSet data) throws ResultsException {
        updateSeries(data);
        if (data.getSeries() != null) {
            if (!lineChart.getData().contains(data.getSeries())) {
                lineChart.getData().add(data.getSeries());
            }
            data.setShown(true);
        } else {
            throw new ResultsException("No chart data available");
        }
    }
    
    public synchronized void addForAutoAdding(LineChart lineChart, ResultSet data) {
        resultsDao.addForAutoAdding(lineChart, data);
    }
    
    public synchronized boolean containsForAutoAdding(LineChart lineChart, ResultSet data) {
        return resultsDao.containsForAutoAdding(lineChart, data);
    }
    
    public synchronized void removeFromAutoAdding(LineChart lineChart, ResultSet data) {
        resultsDao.removeFromAutoAdding(lineChart, data);
    }
    
    public synchronized void UpdateAutoAddedData() throws ResultsException {
        for (LineChart lineChart : resultsDao.getLineChartsWithAutoAdding()) {
            for (ResultSet data : resultsDao.getChartTable(lineChart).getItems()) {
                updateSeries(data);
            }
            resultsDao.getChartTable(lineChart).refresh();
        }
    }
    
    public String getValueName(String value, SimulationResult simulation) {
        if (value.matches(valueChoiceFire)) {
            return "Firing";
        } else if (value.matches(valueChoiceSpeed)) {
            return "Speed";
        } else if (value.matches(valueChoiceToken)) {
            return "Token";
        } else if (value.matches(valueChoiceTokenInActual)) {
            DataArc arc = (DataArc) simulation.getFilterElement(value);
            return "Token from " + arc.getSource().toString() + " [ACTUAL]";
        } else if (value.matches(valueChoiceTokenInTotal)) {
            DataArc arc = (DataArc) simulation.getFilterElement(value);
            return "Token from " + arc.getSource().toString() + " [TOTAL]";
        } else if (value.matches(valueChoiceTokenOutActual)) {
            DataArc arc = (DataArc) simulation.getFilterElement(value);
            return "Token to " + arc.getTarget().toString() + " [ACTUAL]";
        } else if (value.matches(valueChoiceTokenOutTotal)) {
            DataArc arc = (DataArc) simulation.getFilterElement(value);
            return "Token to " + arc.getTarget().toString() + " [TOTAL]";
        } else {
            return null;
        }
    }
    
    public Map<String,List<String>> getSharedValues(SimulationResult results, List<IElement> elements) {
        
        Map<String,List<String>> valuesTmp, valuesShared = null;
        
        List<String> values, valuesRemoved;
        String name;
        
        for (IElement element : elements) {
            
            values = results.getElementFilter(element);
            valuesTmp = new HashMap();
            
            for (String value : values) {
                
                name = getValueName(value, results);
                
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
    
    private synchronized void AutoAddData(SimulationResult simulation) throws ResultsException {

        Map<String, Map<IElement, Set<String>>> modelsToAutoAdd;
        Map<IElement, Set<String>> elementsToAutoAdd;
        Set<String> valuesToAutoAdd;
        ResultSet data;

        // validate all active charts
        for (LineChart lineChart : resultsDao.getLineChartsWithAutoAdding()) {

            modelsToAutoAdd = resultsDao.getDataAutoAdd(lineChart);
            
            if (modelsToAutoAdd != null) {

                elementsToAutoAdd = modelsToAutoAdd.get(simulation.getDao().getModelId());

                if (elementsToAutoAdd != null) {
                    
                    // validate elements chosen for auto adding to be available
                    for (IElement elem : elementsToAutoAdd.keySet()) {

                        valuesToAutoAdd = elementsToAutoAdd.get(elem);

                        if (valuesToAutoAdd != null) {

                            // validate values chosen for auto adding to be valid
                            for (String valueToAutoAdd : valuesToAutoAdd) {

                                if (simulation.getFilterElement(valueToAutoAdd) != null) {

                                    // create and add data to chart
                                    data = new ResultSet(simulation, elem, valueToAutoAdd);

                                    try {
                                        add(lineChart, data);
                                    } catch (ResultsException ex) {
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
     * @param resultSe
     * @throws ResultsException
     */
    private synchronized void updateSeries(ResultSet resultSe) throws ResultsException {

//        Map<String, List<Object>> resultsMap = data.getSimulation().getResultsData();
//        String variable = data.getVariable();
        List<Object> data = resultSe.getData();
        List<Object> time = resultSe.getSimulation().getTimeData();
        int indexDataProcessed = resultSe.getDataProcessedIndex();
        
        if (data == null) {
            throw new ResultsException("");
        }
        
        XYChart.Series seriesOld = resultSe.getSeries();
//        List<Object>[] results = data.getSimulation().getSimulationResults();

        if (seriesOld == null || data.size() > indexDataProcessed) { // update only if additional values available

            XYChart.Series seriesNew = new XYChart.Series();

            if (seriesOld != null) {
                seriesNew.getData().addAll(seriesOld.getData());
            }

            /**
             * Attach data to series.
             * TODO replace by downsampling.
             */
            for (int i = indexDataProcessed; i < data.size(); i++) {
                seriesNew.getData().add(new XYChart.Data(
                        (Number) time.get(i),
                        (Number) data.get(i)
                ));
                indexDataProcessed++;
            }
            resultSe.setDataProcessedIndex(indexDataProcessed);
            
            // Create label
            if (resultSe.getElement().getName() != null
                    && !resultSe.getElement().getName().isEmpty()) {
                seriesNew.setName("'" + resultSe.getElement().getName()+ "' (" + resultSe.getSimulation().toStringShort() + ")");
            } else {
                seriesNew.setName("'" + resultSe.getElement().getId() + "' (" + resultSe.getSimulation().toStringShort() + ")");
            }

            // Replace in chart
            if (seriesOld != null) {
                XYChart chart = seriesOld.getChart();
                if (chart != null) {
                    chart.getData().remove(seriesOld);
                    chart.getData().add(seriesNew);
                }
            }
            resultSe.setSeries(seriesNew);
        }
    }
}
