package edu.unibi.agbi.gnius;

import edu.unibi.agbi.gnius.controller.data.DataController;
import edu.unibi.agbi.gnius.controller.fxml.PresentationController;
import edu.unibi.agbi.gnius.exception.controller.GraphNotNullException;
import edu.unibi.agbi.gnius.handler.MouseGestures;
import edu.unibi.agbi.gnius.handler.ZoomHandler;
import edu.unibi.agbi.gravisfx.presentation.GraphScene;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainWindow.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        scene.getStylesheets().add("/styles/gravis/nodes.css");
        
        stage.setTitle("GraVisFX - PetriNet Editor");
        stage.setScene(scene);
        stage.show();
        
        GraphScene graphScene = new GraphScene();
        graphScene.setId("graphScene");
        
        BorderPane borderPane = (BorderPane) scene.lookup("#viewPane");
        
        graphScene.widthProperty().bind(borderPane.widthProperty());
        graphScene.heightProperty().bind(borderPane.heightProperty());
        
        borderPane.setCenter(graphScene);
        try {
            PresentationController.setGraph(graphScene.getGraph());
        } catch (GraphNotNullException ex) {
            System.out.println(ex);
        }
        
        // register mouse actions
        ZoomHandler.applyZoomHandler(graphScene.getGraphPane());
        
        MouseGestures.registerTo(graphScene.getGraphPane());
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
