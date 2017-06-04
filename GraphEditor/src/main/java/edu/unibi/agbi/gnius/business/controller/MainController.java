/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.business.controller.editor.TabsController;
import edu.unibi.agbi.gnius.business.controller.editor.elementpanel.ParameterController;
import edu.unibi.agbi.gnius.business.controller.editor.elementpanel.ElementController;
import edu.unibi.agbi.gnius.business.controller.editor.modelpanel.ModelController;
import edu.unibi.agbi.gnius.business.controller.menu.FileMenuController;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gravisfx.presentation.GraphPane;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class MainController implements Initializable
{
    @Autowired private DataService dataService;
    @Autowired private Calculator calculator;

    @Autowired private ElementController elementController;
    @Autowired private FileMenuController fileMenuController;
    @Autowired private ModelController modelController;
    @Autowired private ParameterController parameterController;
    @Autowired private TabsController tabsController;

    @FXML private HBox zoomFrame;
    @FXML private VBox modelFrame;
    @FXML private VBox elementFrame;
    @FXML private VBox parameterFrame;
    @FXML private Label statusTop;
    
    @Value("${zoom.scale.base}") private double scaleBase;
    @Value("${zoom.scale.factor}") private double scaleFactor;
    @Value("${zoom.scale.max}") private double scaleMax;
    @Value("${zoom.scale.min}") private double scaleMin;
    
    /**
     * Applies an offset for a given zoom factor to a graph pane. 
     * 
     * @param graphPane
     * @param zoomOffsetX
     * @param zoomOffsetY
     * @param zoomFactor 
     */
    public void ApplyZoomOffset(GraphPane graphPane, double zoomOffsetX, double zoomOffsetY, double zoomFactor) {

        double startX, startY, endX, endY;
        double translateX, translateY;

        startX = zoomOffsetX - graphPane.getTopLayer().translateXProperty().get();
        startY = zoomOffsetY - graphPane.getTopLayer().translateYProperty().get();

        endX = startX * zoomFactor;
        endY = startY * zoomFactor;

        translateX = startX - endX;
        translateY = startY - endY;

        graphPane.getTopLayer().setTranslateX(graphPane.getTopLayer().translateXProperty().get() + translateX);
        graphPane.getTopLayer().setTranslateY(graphPane.getTopLayer().translateYProperty().get() + translateY);
    }

    public Stage getStage() {
        return (Stage) elementFrame.getScene().getWindow();
    }
    
    public Label getStatusTop() {
        return statusTop;
    }

    public void HideElementPanel() {
        elementFrame.setVisible(false);
        parameterFrame.setVisible(false);
    }

    public void HideModelPanel() {
        zoomFrame.setVisible(false);
        modelFrame.setVisible(false);
    }

    public void ShowDialogExit(Event event) {

        List<DataDao> daos = dataService.getDataDaosWithChanges();

        ButtonType buttonSave = new ButtonType("Save");
        ButtonType buttonQuit = new ButtonType("Exit");
        ButtonType buttonCancel = new ButtonType("Cancel");

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm exit");

        if (daos.isEmpty()) {
            alert.setHeaderText("Close the application");
            alert.setContentText("Are you sure you want to close the application?");
            alert.getButtonTypes().setAll(buttonQuit, buttonCancel);
        } else {
            alert.setHeaderText("Close the application and discard latest changes");
            alert.setContentText("You made changes to your model(s). Are you sure you want to close the application and discard any changes?");
            alert.getButtonTypes().setAll(buttonSave, buttonQuit, buttonCancel);
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonSave) {
            for (DataDao dao : daos) {
                fileMenuController.SaveAs(dao);
            }
            if (event != null) {
                event.consume();
            }
        } else if (result.get() == buttonQuit) {
            try {
                System.out.println("Closing application...");
                System.exit(0);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        } else {
            if (event != null) {
                event.consume();
            }
        }
    }

    public void ShowElementDetails(IGraphElement element) {
        if (tabsController.getActiveGraphPane() == null) {
            return;
        }
        parameterFrame.setVisible(false);
        elementFrame.setVisible(true);
        elementController.ShowElementDetails(element);
    }

    public void ShowElementParameters(IDataElement element) {
        if (tabsController.getActiveGraphPane() == null) {
            return;
        }
        elementFrame.setVisible(false);
        parameterFrame.setVisible(true);
        parameterController.ShowParameters(element);
    }

    public void ShowModelPanel(DataDao dataDao) {
        zoomFrame.setVisible(true);
        modelFrame.setVisible(true);
        modelController.setModel(dataDao);
    }
    
    @FXML
    public void CenterNodes() {

        Point2D centerTarget;
        double scaleTarget, scaleCurrent, adjustedOffsetX, adjustedOffsetY;
        
        tabsController.getActiveGraphPane().getTopLayer().setTranslateX(0);
        tabsController.getActiveGraphPane().getTopLayer().setTranslateY(0);
        
        centerTarget = calculator.getNodeCenter(dataService.getActiveGraph().getNodes());
        
        adjustedOffsetX = centerTarget.getX() - (tabsController.getActiveGraphPane().getWidth() / 2) / tabsController.getActiveGraphPane().getTopLayer().getScale().getX();
        adjustedOffsetY = centerTarget.getY() - (tabsController.getActiveGraphPane().getHeight() / 2) / tabsController.getActiveGraphPane().getTopLayer().getScale().getX();
        
        dataService.getActiveGraph().getNodes().forEach(node -> {
            node.translateXProperty().set(node.translateXProperty().get() - adjustedOffsetX);
            node.translateYProperty().set(node.translateYProperty().get() - adjustedOffsetY);
        });
        
        scaleTarget = calculator.getScaleDifference(dataService.getActiveGraph(), tabsController.getActiveGraphPane());
        scaleCurrent = scaleBase * Math.pow(scaleFactor, dataService.getActiveDao().getScalePower());
        
        if (scaleTarget > scaleCurrent) {
            while (scaleCurrent < scaleMax && scaleCurrent < scaleTarget) {
                ZoomIn();
                scaleCurrent = scaleBase * Math.pow(scaleFactor, dataService.getActiveDao().getScalePower() + 1);
            }
        } else {
            while (scaleCurrent > scaleMin && scaleCurrent > scaleTarget) {
                ZoomOut();
                scaleCurrent = scaleBase * Math.pow(scaleFactor, dataService.getActiveDao().getScalePower() - 1);
            }
        }
    }
    
    @FXML
    public void ZoomIn() {
        
        double scale_t0 = scaleBase * Math.pow(scaleFactor, dataService.getActiveDao().getScalePower());
        double scale_t1 = scaleBase * Math.pow(scaleFactor, dataService.getActiveDao().getScalePower() + 1);
        
        if (scale_t1 > scaleMax) {
            return;
        }
        dataService.getActiveDao().setScalePower(dataService.getActiveDao().getScalePower() + 1);
        
        tabsController.getActiveGraphPane().getTopLayer().getScale().setX(scale_t1);
        tabsController.getActiveGraphPane().getTopLayer().getScale().setY(scale_t1);
        
        ApplyZoomOffset(
                tabsController.getActiveGraphPane(), 
                tabsController.getActiveGraphPane().getWidth() / 2, 
                tabsController.getActiveGraphPane().getHeight()/ 2, 
                scale_t1 / scale_t0
        );
    }
    
    @FXML
    public void ZoomOut() {
        
        double scale_t0 = scaleBase * Math.pow(scaleFactor, dataService.getActiveDao().getScalePower());
        double scale_t1 = scaleBase * Math.pow(scaleFactor, dataService.getActiveDao().getScalePower() - 1);
        
        if (scale_t1 < scaleMin) {
            return;
        }
        dataService.getActiveDao().setScalePower(dataService.getActiveDao().getScalePower() - 1);
        
        tabsController.getActiveGraphPane().getTopLayer().getScale().setX(scale_t1);
        tabsController.getActiveGraphPane().getTopLayer().getScale().setY(scale_t1);
        
        ApplyZoomOffset(
                tabsController.getActiveGraphPane(), 
                tabsController.getActiveGraphPane().getWidth() / 2, 
                tabsController.getActiveGraphPane().getHeight()/ 2, 
                scale_t1 / scale_t0
        );
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        zoomFrame.setVisible(false);
        modelFrame.setVisible(false);
        elementFrame.setVisible(false);
        parameterFrame.setVisible(false);
    }
}
