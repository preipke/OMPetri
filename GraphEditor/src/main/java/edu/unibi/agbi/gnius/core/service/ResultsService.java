/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.core.model.dao.SimulationResultsDao;
import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.model.entity.simulation.SimulationLineChartData;
import edu.unibi.agbi.gnius.core.service.exception.ResultsServiceException;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class ResultsService
{
    @Autowired private SimulationResultsDao simulationResultsDao;

    /**
     * Adds the given line chart and related table item list to the storage.
     *
     * @param lineChart
     * @param list
     * @throws ResultsServiceException
     */
    public synchronized void add(LineChart lineChart, ObservableList<SimulationLineChartData> list) throws ResultsServiceException {
        if (simulationResultsDao.contains(lineChart)) {
            throw new ResultsServiceException("The given line chart has already been stored! Cannot overwrite existing observable list.");
        }
        simulationResultsDao.add(lineChart, list);
    }

    /**
     * Adds data to the line chart if data is not yet contained.
     *
     * @param lineChart
     * @param data
     * @return true if data has been added, false if not
     */
    public synchronized boolean add(LineChart lineChart, SimulationLineChartData data) {
        simulationResultsDao.add(data);
        if (!simulationResultsDao.contains(lineChart, data)) {
            simulationResultsDao.add(lineChart, data);
            return true;
        }
        return false;
    }

    /**
     * Drops the given data from the given chart and the table.
     *
     * @param lineChart the line chart that will be modified
     * @param data      the data to be hidden and removed from the also given
     *                  chart
     */
    public synchronized void drop(LineChart lineChart, SimulationLineChartData data) {
        hide(lineChart, data);
        simulationResultsDao.remove(lineChart, data);
    }

    /**
     * Removes the given data from the given chart.
     *
     * @param lineChart
     * @param data
     */
    public synchronized void hide(LineChart lineChart, SimulationLineChartData data) {
        lineChart.getData().remove(data.getSeries());
    }

    /**
     *
     * @param lineChart
     * @param data
     * @return
     */
    public synchronized boolean isShown(LineChart lineChart, SimulationLineChartData data) {
        return lineChart.getData().contains(data.getSeries());
    }

    /**
     * Shows the given data in the given chart.
     *
     * @param lineChart
     * @param data
     */
    public synchronized void show(LineChart lineChart, SimulationLineChartData data) {
        if (!lineChart.getData().contains(data.getSeries())) {
            lineChart.getData().add(data.getSeries());
        }
    }

    /**
     * Updates the series for the given data object. Loads data from the
     * simulation and adds all additional entries to the series.
     *
     * @param data
     */
    public synchronized void UpdateSeries(SimulationLineChartData data) {

        Simulation simulation = data.getSimulation();
        XYChart.Series series = data.getSeries();
        List<Object>[] results = simulation.getResults();
        
        if (results[0].size() > series.getData().size()) { // update only if additional values available
            String[] variables = simulation.getVariables();
            String variableTarget = data.getValue();
            int index = 0;
            for (String variable : variables) {
                if (variable.matches(variableTarget)) {
                    break;
                }
                index++;
            }
            for (int i = series.getData().size(); i < results[0].size(); i++) {
                series.getData().add(new XYChart.Data(
                        (Number) results[0].get(i),
                        (Number) results[index].get(i)
                ));
            }
            series.setName("'" + data.getElement().getId() + " '");
        }
    }
}
