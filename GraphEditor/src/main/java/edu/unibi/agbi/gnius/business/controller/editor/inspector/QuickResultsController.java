/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.inspector;

import edu.unibi.agbi.gnius.business.controller.ResultsController;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.result.ResultSet;
import edu.unibi.agbi.gnius.core.model.entity.result.SimulationResult;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.ResultsService;
import edu.unibi.agbi.gnius.core.service.exception.ResultsException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class QuickResultsController implements Initializable
{
    @Autowired private DataService dataService;
    @Autowired private ResultsController resultsController;
    @Autowired private ResultsService resultsService;
    @Autowired private MessengerService messengerService;

    @FXML private Button buttonViewer;
    @FXML private MenuButton buttonMenuResults;
    @FXML private LineChart lineChartResults;

    private DataDao dao;
    private IDataElement data;

    public void setElement(IDataElement element) {

        data = element;

        Refresh(element);

    }

    private IDataElement getElement() {
        return data;
    }

    private void Refresh(IDataElement element) {
        
        lineChartResults.getData().clear();

        if (element == null) {
            buttonMenuResults.getItems().clear();
            dao = null;
            return;
        }

        /**
         * Get relevant simulation results.
         */
        List<SimulationResult> results;

        results = new ArrayList();
        for (SimulationResult result : resultsService.getSimulationResults()) {
            if (result.getDao().getModelId().contentEquals(dataService.getDao().getModelId())) { // check if model is the same
                results.add(result);
            }
        }
        results.sort((r1, r2) -> r1.getDateTime().compareTo(r2.getDateTime()));

        RefreshResultsButton(results);
        RefreshChart(element);
    }

    private void RefreshChart(IDataElement element) {

        ResultsCheckBox checkBox;
        SimulationResult result;

        for (MenuItem item : buttonMenuResults.getItems()) {

            checkBox = (ResultsCheckBox) item.getGraphic();
            result = checkBox.getResult();

            if (checkBox.isSelected()) {

                addData(result, element);

            }
        }
    }

    private void RefreshResultsButton(List<SimulationResult> results) {

        /**
         * Check if choices have to be updated.
         */
        if (dao == null) {
            dao = dataService.getDao();
        } else {
            if (dao.equals(dataService.getDao())) {
                return; // no need to refresh if model didnt change
            } else {
                dao = dataService.getDao();
            }
        }

        buttonMenuResults.getItems().clear();

        CheckBox item = null;

        for (SimulationResult result : results) {
            item = addCheckBoxItem(result);
        }

        if (item != null) {
            item.setSelected(true);
        }
    }

    private ResultsCheckBox addCheckBoxItem(SimulationResult result) {

        ResultsCheckBox checkBox = new ResultsCheckBox(result);
        checkBox.setSelected(true);
        checkBox.setOnAction(cl -> {
            if (checkBox.isSelected()) {
                addData(result, getElement());
            } else {
                removeData(result, getElement());
            }
            cl.consume(); // keeps menu open
        });

        MenuItem item = new MenuItem();
        item.setText(result.toString());
        item.setGraphic(checkBox);

        buttonMenuResults.getItems().add(item);

        return checkBox;
    }

    private void addData(SimulationResult simulation, IDataElement element) {

        List<ResultSet> resultSets;
        try {
            resultSets = resultsService.getResultSets(simulation, element);

            System.out.println("Adding data");
            XYChart.Series series;
            for (ResultSet set : resultSets) {
                series = set.getSeries();
                if (!lineChartResults.getData().contains(series)) {
                    lineChartResults.getData().add(series);
                }
            }
        } catch (ResultsException ex) {
            messengerService.addException("Cannot find result(s) to add to inspector chart!", ex);
        }
    }

    private void removeData(SimulationResult simulation, IDataElement element) {

        List<ResultSet> resultSets;
        try {
            resultSets = resultsService.getResultSets(simulation, element);

            System.out.println("Removing data");
            XYChart.Series series;
            for (ResultSet set : resultSets) {
                series = set.getSeries();
                if (lineChartResults.getData().contains(series)) {
                    lineChartResults.getData().remove(series);
                }
            }
        } catch (ResultsException ex) {
            messengerService.addException("Cannot find result(s) to remove from chart!", ex);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        buttonViewer.setOnAction(eh -> resultsController.OpenWindow());
        lineChartResults.setAnimated(false);
        lineChartResults.createSymbolsProperty().set(false);
        resultsService.getSimulationResults().addListener(new ListChangeListener()
        {
            @Override
            public void onChanged(ListChangeListener.Change change) {

                List<SimulationResult> results;
                ResultsCheckBox checkBox;
                MenuItem item;

                change.next();
                if (change.wasAdded()) {

                    results = change.getAddedSubList();

                    for (SimulationResult result : results) {

                        checkBox = addCheckBoxItem(result);
                        checkBox.setSelected(true);

                        addData(result, getElement());
                    }

                } else if (change.wasRemoved()) {

                    results = change.getRemoved();

                    for (SimulationResult result : results) {

                        removeData(result, getElement());

                        item = null;
                        for (MenuItem menuItem : buttonMenuResults.getItems()) {

                            item = menuItem;
                            checkBox = (ResultsCheckBox) item.getGraphic();
                            if (checkBox.getResult().equals(result)) {
                                break;
                            }
                        }

                        buttonMenuResults.getItems().remove(item);
                    }
                }
            }
        });
    }

    private class ResultsCheckBox extends CheckBox
    {
        final private SimulationResult result;

        public ResultsCheckBox(SimulationResult result) {
            this.result = result;
        }

        public SimulationResult getResult() {
            return result;
        }
    }
}
