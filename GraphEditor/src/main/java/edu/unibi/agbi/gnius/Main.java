package edu.unibi.agbi.gnius;

import edu.unibi.agbi.gnius.business.controller.LogController;
import edu.unibi.agbi.gnius.business.controller.MainController;
import edu.unibi.agbi.gnius.business.handler.KeyEventHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Boot and configuration class.
 * @author PR
 */
@SpringBootApplication // matches @Configuration @ComponentScan @EnableAutoConfig
public class Main extends Application {
    
    private final String fxmlMain = "/fxml/Main.fxml";
    private final String fxmlLog = "/fxml/Log.fxml";
    
    private final String titleMain = "Application - Main Window";
    private final String titleLog = "Application - Logging Window";
    
    private final String cssMain = "/styles/main.css";
    private final String cssGraph = "/styles/graph.css";
    
    private ConfigurableApplicationContext springContext;
    private Parent mainRoot;
    private Parent logRoot;
    
    @Override
    public void init() throws Exception {
        
        springContext = SpringApplication.run(Main.class);  // main configuration class
        
        // init main
        FXMLLoader mainLoader = new FXMLLoader();
        mainLoader.setControllerFactory(springContext::getBean); // tell fxml loader who is in charge of instantiating controllers, java 8 method reference to spring
        mainLoader.setLocation(getClass().getResource(fxmlMain));
        mainRoot = mainLoader.load();

        // init log
        FXMLLoader logLoader = new FXMLLoader();
        logLoader.setControllerFactory(springContext::getBean); // tell fxml loader who is in charge of instantiating controllers, java 8 method reference to spring
        logLoader.setLocation(getClass().getResource(fxmlLog));
        logRoot = logLoader.load();
    }

    @Override
    public void start(Stage mainStage) throws Exception {
        
        // main window
        Scene mainScene = new Scene(mainRoot);
        mainScene.getStylesheets().add(cssMain);
        mainScene.getStylesheets().add(cssGraph);
        
        mainStage.setTitle(titleMain);
        mainStage.setScene(mainScene);
        mainStage.setOnCloseRequest(e -> {
            MainController mainController = (MainController) springContext.getBean(MainController.class);
            mainController.ShowDialogExit(e);
        });
        mainStage.show();
        
        KeyEventHandler keyEventHandler = (KeyEventHandler) springContext.getBean(KeyEventHandler.class);
        keyEventHandler.registerTo(mainStage.getScene());
        
        // log window
        Scene logScene = new Scene(logRoot);
        logScene.getStylesheets().add(cssMain);
        
        Stage logStage = new Stage();
        logStage.setTitle(titleLog);
        logStage.setScene(logScene);
        logStage.show();
        
        LogController logController = (LogController) springContext.getBean(LogController.class);
        logController.setStage(logStage);
        
        mainStage.toFront();
    }
    
    /**
     * Called by Spring once it has created an instance of 
     * Application and injected any dependencies.
     */
    @PostConstruct
    public void register() {
    }
    
    @Override
    public void stop() throws Exception {
        springContext.close();
    }

    public static void main(String[] args) {
        // some internals require that AWT is initialized before JavaFX
        // (related to the use of SwingNode in rendering transition functions)
        java.awt.Toolkit.getDefaultToolkit(); 
        Application.launch(args);
    }
}
