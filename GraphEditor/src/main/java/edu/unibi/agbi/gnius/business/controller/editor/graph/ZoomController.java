/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.graph;

import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gravisfx.graph.GraphPane;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ZoomController implements Initializable
{
    @Autowired private Calculator calculator;
    @Autowired private DataService dataService;
    
    @Value("${zoom.scale.base}") private double scaleBase;
    @Value("${zoom.scale.factor}") private double scaleFactor;
    @Value("${zoom.scale.max}") private double scaleMax;
    @Value("${zoom.scale.min}") private double scaleMin;

    /**
     * Applies an offset in translation to keep the focus for a given zoom
     * factor to a graph pane.
     *
     * @param graphPane
     * @param zoomOffsetX
     * @param zoomOffsetY
     * @param zoomFactor
     */
    public void ApplyZoomOffset(GraphPane graphPane, double zoomOffsetX, double zoomOffsetY, double zoomFactor) {

        double startX, startY, endX, endY;
        double translateX, translateY;

        startX = zoomOffsetX - graphPane.getGraph().translateXProperty().get();
        startY = zoomOffsetY - graphPane.getGraph().translateYProperty().get();

        endX = startX * zoomFactor;
        endY = startY * zoomFactor;

        translateX = startX - endX;
        translateY = startY - endY;

        graphPane.getGraph().setTranslateX(graphPane.getGraph().translateXProperty().get() + translateX);
        graphPane.getGraph().setTranslateY(graphPane.getGraph().translateYProperty().get() + translateY);
    }
    
    @FXML
    public void CenterNodes() {

        Point2D center;
        double scaleDistance, scaleTarget, scaleCurrent, adjustedOffsetX, adjustedOffsetY;

        dataService.getGraph().setTranslateX(0);
        dataService.getGraph().setTranslateY(0);

        center = calculator.getCenter(dataService.getGraph().getNodes());

        adjustedOffsetX = center.getX()
                - (dataService.getDao().getGraphPane().getWidth() / 2)
                / dataService.getDao().getGraphPane().getGraph().getScale().getX();
        adjustedOffsetY = center.getY()
                - (dataService.getDao().getGraphPane().getHeight() / 2)
                / dataService.getDao().getGraphPane().getGraph().getScale().getY();

        dataService.getGraph().getNodes().forEach(node -> {
            Point2D pos = new Point2D(
                    node.translateXProperty().get() - adjustedOffsetX, 
                    node.translateYProperty().get() - adjustedOffsetY
            );
            if (dataService.isGridEnabled()) {
                pos = calculator.getPositionInGrid(pos, dataService.getGraph());
            }
            node.translateXProperty().set(pos.getX());
            node.translateYProperty().set(pos.getY());
        });

        scaleTarget = calculator.getOptimalScale(dataService.getDao().getGraphPane());
        scaleCurrent = scaleBase * Math.pow(scaleFactor, dataService.getDao().getScalePower());
        scaleDistance = Math.abs(scaleTarget - scaleCurrent);

        if (scaleTarget > scaleCurrent) {
            while (scaleCurrent < scaleMax && scaleCurrent < scaleTarget) {
                scaleCurrent = scaleBase * Math.pow(scaleFactor, dataService.getDao().getScalePower() + 1);
                if (scaleDistance > (scaleDistance = Math.abs(scaleTarget - scaleCurrent))) {
                    ZoomIn();
                }
            }
        } else if (scaleTarget < scaleCurrent) {
            while (scaleCurrent > scaleMin && scaleCurrent > scaleTarget) {
                scaleCurrent = scaleBase * Math.pow(scaleFactor, dataService.getDao().getScalePower() - 1);
                if (scaleDistance > (scaleDistance = Math.abs(scaleTarget - scaleCurrent))) {
                    ZoomOut();
                }
            }
        }
    }
    
    @FXML
    private void ZoomIn() {

        double scale_t0 = scaleBase * Math.pow(scaleFactor, dataService.getDao().getScalePower());
        double scale_t1 = scaleBase * Math.pow(scaleFactor, dataService.getDao().getScalePower() + 1);

        if (scale_t1 > scaleMax) {
            return;
        }
        dataService.getDao().setScalePower(dataService.getDao().getScalePower() + 1);

        dataService.getGraph().getScale().setX(scale_t1);
        dataService.getGraph().getScale().setY(scale_t1);

        ApplyZoomOffset(
                dataService.getDao().getGraphPane(),
                dataService.getDao().getGraphPane().getWidth() / 2,
                dataService.getDao().getGraphPane().getHeight() / 2,
                scale_t1 / scale_t0
        );
    }
    
    @FXML
    private void ZoomOut() {

        double scale_t0 = scaleBase * Math.pow(scaleFactor, dataService.getDao().getScalePower());
        double scale_t1 = scaleBase * Math.pow(scaleFactor, dataService.getDao().getScalePower() - 1);

        if (scale_t1 < scaleMin) {
            return;
        }
        dataService.getDao().setScalePower(dataService.getDao().getScalePower() - 1);

        dataService.getDao().getGraphPane().getGraph().getScale().setX(scale_t1);
        dataService.getDao().getGraphPane().getGraph().getScale().setY(scale_t1);

        ApplyZoomOffset(
                dataService.getDao().getGraphPane(),
                dataService.getDao().getGraphPane().getWidth() / 2,
                dataService.getDao().getGraphPane().getHeight() / 2,
                scale_t1 / scale_t0
        );
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }
}
