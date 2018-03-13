/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.util;

import edu.unibi.agbi.editor.business.service.ResultService;
import edu.unibi.agbi.editor.presentation.controller.LogController;
import edu.unibi.agbi.editor.presentation.controller.MainController;
import edu.unibi.agbi.editor.presentation.controller.ResultViewerController;
import edu.unibi.agbi.editor.presentation.handler.KeyEventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Factory class used to generate the GUI.
 * 
 * @author PR
 */
@Component
public class GuiFactory
{
    @Autowired private ConfigurableApplicationContext applicationContext;
    @Autowired private ResultService resultService;
    
    private final String fxmlMain = "/fxml/Main.fxml";
    private final String fxmlLog = "/fxml/Log.fxml";
    private final String fxmlResults = "/fxml/Results.fxml";
    
    private final String titleMain = "JFX PetriNet - Editor";
    private final String titleLog = "JFX PetriNet - Log";
    private final String titleResult = "JFX PetriNet - Results Viewer";
    
    private final String cssMain = "/styles/main.css";
    private final String cssGraph = "/styles/graph.css";
    
    private Stage mainStage = null;
    private Stage logStage = null;
    
    public void BuildMainWindow() throws IOException {
        
        if (mainStage != null) {
            throw new IOException("The main window has already been constructed!");
        }
        
        Parent mainRoot = loadFxml(fxmlMain);
        
        Scene mainScene = new Scene(mainRoot);
        mainScene.getStylesheets().add(cssMain);
        mainScene.getStylesheets().add(cssGraph);
        
        mainStage = new Stage();
        mainStage.setTitle(titleMain);
        mainStage.setScene(mainScene);
        mainStage.setOnCloseRequest(e -> {
            MainController mainController = (MainController) applicationContext.getBean(MainController.class);
            mainController.ShowDialogExit(e);
        });
        
        KeyEventHandler keyEventHandler = (KeyEventHandler) applicationContext.getBean(KeyEventHandler.class);
        keyEventHandler.registerTo(mainStage.getScene());
        
        mainStage.show();
        mainStage.toFront();
    }
    
    public void BuildLogWindow() throws IOException {
        
        if (logStage != null) {
            throw new IOException("The log window has already been constructed!");
        }
        
        Parent logRoot = loadFxml(fxmlLog);
        
        Scene logScene = new Scene(logRoot);
        logScene.getStylesheets().add(cssMain);
        
        logStage = new Stage();
        logStage.setTitle(titleLog);
        logStage.setScene(logScene);
        
        LogController logController = (LogController) applicationContext.getBean(LogController.class);
        logController.setStage(logStage);
        
        logStage.show();
    }

    public void BuildResultsViewer() throws IOException {

        ResultViewerController controller = new ResultViewerController();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(controller);

        // init results window
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(fxmlResults));
        loader.setController(controller);
        
        Parent resultRoot = loader.load();

        Scene scene = new Scene(resultRoot);
        scene.getStylesheets().add(cssMain);

        Stage stg = new Stage();
        stg.setScene(scene);
        stg.setTitle(titleResult);
        stg.show();
        stg.setIconified(false);
        stg.toFront();
        stg.setOnCloseRequest(e -> {
            resultService.drop(controller.getLineChart());
        });

        controller.setStage(stg);
    }
    
    private Parent loadFxml(String fxml) throws IOException {
        FXMLLoader mainLoader = new FXMLLoader();
        mainLoader.setControllerFactory(applicationContext::getBean); // tell fxml loader who is in charge of instantiating controllers, java 8 method reference to spring
        mainLoader.setLocation(getClass().getResource(fxml));
        return mainLoader.load();
    }
}
