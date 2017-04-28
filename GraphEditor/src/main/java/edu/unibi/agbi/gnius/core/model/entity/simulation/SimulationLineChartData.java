/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.simulation;

import edu.unibi.agbi.petrinet.entity.IElement;
import javafx.scene.chart.XYChart;

/**
 * Data structure for data added to the LineChart. Contains the data series
 * aswell as references to the related simulation, element and value.
 *
 * @author PR
 */
public class SimulationLineChartData
{
    private final Simulation simulation;
    private final IElement element;
    private final String value;
    private final XYChart.Series series;
    private long timeLastStatusChange;

    public SimulationLineChartData(Simulation simulation, IElement element, String value) {
        this.simulation = simulation;
        this.element = element;
        this.value = value;
        this.series = new XYChart.Series();
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public IElement getElement() {
        return element;
    }

    public String getValue() {
        return value;
    }

    public XYChart.Series getSeries() {
        return series;
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
        if (!(object instanceof SimulationLineChartData)) {
            return false;
        }
        SimulationLineChartData data = (SimulationLineChartData) object;
        if (!data.getSimulation().equals(simulation)) {
            return false;
        }
        if (!data.getElement().getId().matches(element.getId())) {
            return false;
        }
        return data.getValue().matches(value);
    }
}
