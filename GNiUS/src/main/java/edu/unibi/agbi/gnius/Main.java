package edu.unibi.agbi.gnius;

import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.pane.GraphPane;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {
    
    private static Graph graph;

    @Override
    public void start(Stage stage) throws Exception {
        
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainWindow.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();
        
        graph = new Graph();
        
        GraphPane graphPane = new GraphPane();
        graphPane.setId("graphPane");
        graphPane.setStyle("-fx-background-color: white");
        graphPane.getChildren().add(graph.getTopLayer());
        
        BorderPane viewPane;
        viewPane = (BorderPane) scene.lookup("#viewPane");
        viewPane.setCenter(graphPane);
    }
    
    public static Graph getGraph() {
        return graph;
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
