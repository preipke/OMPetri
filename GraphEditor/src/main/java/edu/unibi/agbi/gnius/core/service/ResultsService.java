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
    @Autowired private ResultsDao resultsDao;

    /**
     * Adds the given line chart and related table item list to the storage.
     *
     * @param lineChart
     * @param list
     * @throws ResultsServiceException
     */
    public synchronized void add(LineChart lineChart, ObservableList<SimulationData> list) throws ResultsServiceException {
        if (resultsDao.contains(lineChart)) {
            throw new ResultsServiceException("The given line chart has already been stored! Cannot overwrite existing observable list.");
        }
        resultsDao.add(lineChart, list);
    }

    /**
     * Adds data to the line chart if data is not yet contained.
     *
     * @param lineChart
     * @param data
     * @throws ResultsServiceException
     */
    public synchronized void add(LineChart lineChart, SimulationData data) throws ResultsServiceException {
        if (resultsDao.contains(lineChart, data)) {
            throw new ResultsServiceException("Duplicate data entry for line chart! Not adding.");
        }
        resultsDao.add(data);
        resultsDao.add(lineChart, data);
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

    public List<SimulationData> getChartData(LineChart lineChart) {
        return resultsDao.getResultsTableList(lineChart);
    }

    public ObservableList<Simulation> getSimulations() {
        return resultsDao.getSimulations();
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
        data.updateMilliSecondLastStatusChange();
    }

    /**
     * Shows the given data in the given chart.
     *
     * @param lineChart
     * @param data
     */
    public synchronized void show(LineChart lineChart, SimulationData data) {
        if (!lineChart.getData().contains(data.getSeries())) {
            lineChart.getData().add(data.getSeries());
        }
        data.setShown(true);
        data.updateMilliSecondLastStatusChange();
    }

    /**
     * Updates the series for the given data object.Loads data from the
     * simulation and adds all additional entries to the series. Updates the
     * related chart.
     *
     * @param data
     * @throws edu.unibi.agbi.gnius.core.exception.ResultsServiceException
     */
    public synchronized void UpdateSeries(SimulationData data) throws ResultsServiceException {

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
                throw new ResultsServiceException("Variable '" + variableTarget + "' cannot be found in the references!");
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
            seriesNew.setName("'" + data.getElementId() + "'");

            if (seriesOld != null) {
                XYChart chart = data.getSeries().getChart();
                chart.getData().remove(seriesOld);
                chart.getData().remove(seriesNew);
            }

            data.setSeries(seriesNew);
        }
    }
}
