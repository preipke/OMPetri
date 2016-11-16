package tutorial.grapheditor;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

/**
 * http://stackoverflow.com/questions/30679025/graph-visualisation-like-yfiles-in-javafx
 * 
 * The application instantiates the graph, adds cells and connects them via edges.
 */
public class MainApp extends Application {
    
    Graph graph = new Graph();

    @Override
    public void start(Stage stage) throws Exception {
        
        graph = new Graph();
        
        /*
        ScrollPane scrollPane;
        scrollPane = graph.getScrollPane();
        
        SubScene viewScene = new SubScene(scrollPane, 1000, 400);
        
        BorderPane actionsPane = new BorderPane();
        
        Group root = new Group();
        root.getChildren().add(viewScene);
        root.getChildren().add(actionsPane);
        
        Scene mainScene = new Scene(root, 1000, 700);
        mainScene.getStylesheets().add("/styles/Styles.css");
        
        //viewScene.heightProperty().bind(mainScene.heightProperty());
        viewScene.widthProperty().bind(mainScene.widthProperty());

        stage.setScene(mainScene);
        stage.show();
        */
        
        Parent fxmlRoot = FXMLLoader.load(getClass().getResource("/fxml/MainWindow.fxml"));
        Scene fxmlScene = new Scene(fxmlRoot);
        fxmlScene.getStylesheets().add("/styles/Styles.css");
        
        stage.setScene(fxmlScene);
        
        stage.show();
        
        ScrollPane graphPane = (ScrollPane) fxmlScene.lookup("#graphPane");
        graphPane.setContent(graph.getCanvas());

        addGraphComponents();

        //Layout layout = new RandomLayout(graph);
        //layout.execute();
    }
    
    
    private void addGraphComponents() {

        Model model = graph.getModel();

        graph.beginUpdate();

        model.addCell("Cell A", CellType.RECTANGLE);
        model.addCell("Cell B", CellType.RECTANGLE);
        model.addCell("Cell C", CellType.RECTANGLE);
        model.addCell("Cell D", CellType.TRIANGLE);
        model.addCell("Cell E", CellType.TRIANGLE);
        model.addCell("Cell F", CellType.RECTANGLE);
        model.addCell("Cell G", CellType.RECTANGLE);

        model.addEdge("Cell A", "Cell B");
        model.addEdge("Cell A", "Cell C");
        model.addEdge("Cell B", "Cell C");
        model.addEdge("Cell C", "Cell D");
        model.addEdge("Cell B", "Cell E");
        model.addEdge("Cell D", "Cell F");
        model.addEdge("Cell D", "Cell G");

        graph.endUpdate();

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
