package edu.unibi.agbi.gnius;

import edu.unibi.agbi.gnius.handler.KeyEventHandler;
import edu.unibi.agbi.gnius.service.DataService;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Boot and configuration class.
 * @author PR
 */
@SpringBootApplication // worth three annotations: @Configuration @ComponentScan @EnableAutoConfig
public class Main extends Application {
    
    private ConfigurableApplicationContext springContext;
    private Parent root;
    
    @Override
    public void init() throws Exception {
        springContext = SpringApplication.run(Main.class);  // main configuration class
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean); // tell fxml loader who is in charge of instantiating controllers, java 8 method reference to spring
        root = fxmlLoader.load();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        scene.getStylesheets().add("/styles/gravis/nodes.css");
        
        stage.setTitle("GraVisFX - PetriNet Editor");
        stage.setScene(scene);
        stage.show();
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
