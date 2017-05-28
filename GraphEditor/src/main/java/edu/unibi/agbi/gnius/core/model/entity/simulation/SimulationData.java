/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.simulation;

import edu.unibi.agbi.petrinet.entity.IElement;
import java.util.Objects;
import javafx.scene.chart.XYChart;

/**
 * Data structure for data added to the LineChart. Contains the data series
 * aswell as references to the related simulation, element and value.
 *
 * @author PR
 */
public class SimulationData
{
    private final Simulation simulation;
    private final IElement element;
    private final String variable;
    private XYChart.Series series;
    
    private boolean isAutoAdding;
    private boolean isShown;
    private long timeLastStatusChange;

    public SimulationData(Simulation simulation, IElement element, String value) {
        this.simulation = simulation;
        this.element = element;
        this.variable = value;
    }

    public Simulation getSimulation() {
        return simulation;
    }
    
    public IElement getElement() {
        return element;
    }
    
    public String getElementId() {
        return element.getId();
    }
    
    public String getElementName() {
        return element.getName();
    }

    public String getVariable() {
        return variable;
    }
    
    public void setSeries(XYChart.Series series) {
        this.series = series;
    }

    public XYChart.Series getSeries() {
        return series;
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
        if (!(object instanceof SimulationData)) {
            return false;
        }
        SimulationData data = (SimulationData) object;
        if (!data.getSimulation().equals(simulation)) {
            return false;
        }
        if (!data.getElementId().contentEquals(element.getId())) {
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
