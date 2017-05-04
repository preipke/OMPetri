package edu.unibi.agbi.gnius;

import edu.unibi.agbi.gnius.business.controller.GraphPaneController;
import edu.unibi.agbi.gnius.business.controller.ResultsController;
import edu.unibi.agbi.gnius.business.handler.KeyEventHandler;
import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.business.handler.ScrollEventHandler;
import edu.unibi.agbi.gravisfx.presentation.GraphPane;
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
    
    private final String mainFxml = "/fxml/Main.fxml";
    private final String mainTitle = "GraVisFX - Editor";
    private final String resultsFxml = "/fxml/Results.fxml";
    private final String resultsTitle = "GraVisFX - Results Viewer";
    
    private final String mainCss = "/styles/main.css";
    private final String graphCss = "/styles/graph.css";
    
    private ConfigurableApplicationContext springContext;
    private Parent mainRoot;
    private Parent resultsRoot;
    
    @Override
    public void init() throws Exception {
        
        springContext = SpringApplication.run(Main.class);  // main configuration class
        
        // init main window
        FXMLLoader mainLoader = new FXMLLoader();
        mainLoader.setControllerFactory(springContext::getBean); // tell fxml loader who is in charge of instantiating controllers, java 8 method reference to spring
        mainLoader.setLocation(getClass().getResource(mainFxml));
        mainRoot = mainLoader.load();
        
        // init results window
        FXMLLoader resultsLoader = new FXMLLoader();
        resultsLoader.setControllerFactory(springContext::getBean); // tell fxml loader who is in charge of instantiating controllers, java 8 method reference to spring
        resultsLoader.setLocation(getClass().getResource(resultsFxml));
        resultsRoot = resultsLoader.load();
    }

    @Override
    public void start(Stage mainStage) throws Exception {
        
        Scene resultsScene = new Scene(resultsRoot);
        resultsScene.getStylesheets().add(mainCss);
        
        Stage resultsStage = new Stage();
        resultsStage.setTitle(resultsTitle);
        resultsStage.setScene(resultsScene);
        
        ResultsController resultsController = (ResultsController) springContext.getBean(ResultsController.class);
        resultsController.setStage(resultsStage);
        
        Scene mainScene = new Scene(mainRoot);
        mainScene.getStylesheets().add(mainCss);
        mainScene.getStylesheets().add(graphCss);
        
        mainStage.setTitle(mainTitle);
        mainStage.setScene(mainScene);
        mainStage.setOnCloseRequest(e -> {
            try {
                System.exit(0);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        });
        mainStage.show();
        
        KeyEventHandler keyEventHandler = (KeyEventHandler) springContext.getBean(KeyEventHandler.class);
        MouseEventHandler mouseEventHandler = (MouseEventHandler) springContext.getBean(MouseEventHandler.class);
        ScrollEventHandler scrollEventHandler = (ScrollEventHandler) springContext.getBean(ScrollEventHandler.class);
        
        GraphPaneController graphPaneController = (GraphPaneController) springContext.getBean(GraphPaneController.class);
        GraphPane graphPane = graphPaneController.getGraphPane();
        
        keyEventHandler.registerTo(mainStage.getScene());
        mouseEventHandler.registerTo(graphPane);
        scrollEventHandler.registerTo(graphPane);
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
