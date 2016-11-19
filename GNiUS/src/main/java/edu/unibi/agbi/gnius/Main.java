package edu.unibi.agbi.gnius;

import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.gui.GraphScene;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {
    
    private static Graph graph;
    public static Graph getGraph() {
        return graph;
    }

    @Override
    public void start(Stage stage) throws Exception {
        
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainWindow.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setTitle("GraVisFX");
        stage.setScene(scene);
        stage.show();
        
        GraphScene graphScene = new GraphScene();
        graphScene.setId("graphScene");
        graph = graphScene.getGraph();
        
        BorderPane viewPane;
        viewPane = (BorderPane) scene.lookup("#viewPane");
        viewPane.setCenter(graphScene);
        
        graphScene.widthProperty().bind(viewPane.widthProperty());
        graphScene.heightProperty().bind(viewPane.heightProperty());
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
