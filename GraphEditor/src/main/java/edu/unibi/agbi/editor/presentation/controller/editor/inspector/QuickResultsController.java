/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller.editor.inspector;

import edu.unibi.agbi.editor.business.exception.ResultsException;
import edu.unibi.agbi.editor.business.service.MessengerService;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.ResultService;
import edu.unibi.agbi.editor.core.data.dao.ModelDao;
import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.result.ResultSet;
import edu.unibi.agbi.editor.core.data.entity.result.Simulation;
import edu.unibi.agbi.editor.core.util.GuiFactory;
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 * @author PR
 */
@Controller
public class QuickResultsController implements Initializable
{
    @Autowired private GuiFactory guiFactory;
    @Autowired private ModelService dataService;
    @Autowired private ResultService resultsService;
    @Autowired private MessengerService messengerService;

    @FXML private Button buttonViewer;
    @FXML private MenuButton buttonMenuResults;
    @FXML private LineChart lineChartResults;

    private ModelDao dao;
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
        List<Simulation> results;

        results = new ArrayList();
        for (Simulation result : resultsService.getSimulationResults()) {
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
        Simulation result;

        for (MenuItem item : buttonMenuResults.getItems()) {

            checkBox = (ResultsCheckBox) item.getGraphic();
            result = checkBox.getResult();

            if (checkBox.isSelected()) {

                addData(result, element);

            }
        }
    }

    private void RefreshResultsButton(List<Simulation> results) {

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

        for (Simulation result : results) {
            item = addCheckBoxItem(result);
        }

        if (item != null) {
            item.setSelected(true);
        }
    }

    private ResultsCheckBox addCheckBoxItem(Simulation result) {

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

    private void addData(Simulation simulation, IDataElement element) {

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

    private void removeData(Simulation simulation, IDataElement element) {

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

        buttonViewer.setOnAction(eh -> {
            try {
                guiFactory.BuildResultsViewer();
            } catch (IOException ex) {
                messengerService.addException("Cannot open results viewer!", ex);
            }
        });
        lineChartResults.setAnimated(false);
        lineChartResults.createSymbolsProperty().set(false);
        resultsService.getSimulationResults().addListener(new ListChangeListener()
        {
            @Override
            public void onChanged(ListChangeListener.Change change) {

                List<Simulation> results;
                ResultsCheckBox checkBox;
                MenuItem item;

                change.next();
                if (change.wasAdded()) {

                    results = change.getAddedSubList();

                    for (Simulation result : results) {

                        checkBox = addCheckBoxItem(result);
                        checkBox.setSelected(true);

                        addData(result, getElement());
                    }

                } else if (change.wasRemoved()) {

                    results = change.getRemoved();

                    for (Simulation result : results) {

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
        final private Simulation result;

        public ResultsCheckBox(Simulation result) {
            this.result = result;
        }

        public Simulation getResult() {
            return result;
        }
    }
}
