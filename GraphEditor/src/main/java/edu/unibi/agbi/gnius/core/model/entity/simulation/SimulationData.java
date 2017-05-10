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
    private final String elementId;
    private final String elementName;
    private final String variable;
    private XYChart.Series series;
    
    private boolean isShown;
    private long timeLastStatusChange;

    public SimulationData(Simulation simulation, IElement element, String value) {
        this.simulation = simulation;
        this.elementId = element.getId();
        this.elementName = element.getName();
        this.variable = value;
    }

    public Simulation getSimulation() {
        return simulation;
    }
    
    public String getElementId() {
        return elementId;
    }
    
    public String getElementName() {
        return elementName;
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
    
    public void setShown(boolean value) {
        isShown = value;
    }
    
    public boolean isShown() {
        return isShown;
    }

    public void updateMilliSecondLastStatusChange() {
        timeLastStatusChange = System.currentTimeMillis();
    }

    public long timeMilliSecondLastStatusChange() {
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
        if (!data.getElementId().contentEquals(elementId)) {
            return false;
        }
        return data.getVariable().contentEquals(variable);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.simulation);
        hash = 23 * hash + Objects.hashCode(this.elementId);
        hash = 23 * hash + Objects.hashCode(this.variable);
        return hash;
    }
}
