/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.result;

import edu.unibi.agbi.gnius.core.service.exception.ResultsException;
import edu.unibi.agbi.petrinet.entity.IElement;
import java.util.List;
import java.util.Objects;
import javafx.scene.chart.XYChart;

/**
 * Data structure for storing a result set. Contains a data series aswell as
 * references to the related simulation, element and value.
 *
 * @author PR
 */
public class ResultSet
{
    private final SimulationResult simulation;
    private final IElement element;
    private final String variable;

    private int dataProcessIndex;
    private XYChart.Series series;

    private boolean isAutoAdding;
    private boolean isShown;
    private long timeLastStatusChange;

    public ResultSet(SimulationResult simulation, IElement element, String variable) throws ResultsException {
        this.simulation = simulation;
        this.element = element;
        this.variable = variable;
        if (simulation.getData(variable) == null) {
            throw new ResultsException("Data for variable '" + variable + "' cannot be found in the associated simulation results!");
        }
    }

    public SimulationResult getSimulation() {
        return simulation;
    }

    public IElement getElement() {
        return element;
    }

    public String getVariable() {
        return variable;
    }

    public List<Object> getData() {
        return simulation.getData(variable);
    }

    /**
     * Gets the index that indicates which data points have already been
     * processed for producing the line chart series.
     *
     * @return
     */
    public int getDataProcessedIndex() {
        return dataProcessIndex;
    }

    /**
     * Sets the index to which data points have already been processed for
     * producing the line chart series.
     *
     * @param dataProcessIndex
     */
    public void setDataProcessedIndex(int dataProcessIndex) {
        this.dataProcessIndex = dataProcessIndex;
    }

    public XYChart.Series getSeries() {
        return series;
    }

    public void setSeries(XYChart.Series series) {
        this.series = series;
    }

    public void setAutoAdding(boolean value) {
        isAutoAdding = value;
    }

    public boolean isAutoAdding() {
        return isAutoAdding;
    }

    public void setShown(boolean value) {
        isShown = value;
        timeLastStatusChange = System.currentTimeMillis();
    }

    public boolean isShown() {
        return isShown;
    }

    /**
     * Gets the time in milliseconds the for the latest shown status change.
     *
     * @return
     */
    public long getTimeLastShownStatusChange() {
        return timeLastStatusChange;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof ResultSet)) {
            return false;
        }
        ResultSet data = (ResultSet) object;
        if (!data.getSimulation().equals(simulation)) {
            return false;
        }
        if (!data.getElement().getId().contentEquals(element.getId())) {
            return false;
        }
        return data.getVariable().contentEquals(variable);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.simulation);
        hash = 23 * hash + Objects.hashCode(this.element.getId());
        hash = 23 * hash + Objects.hashCode(this.variable);
        return hash;
    }
}
