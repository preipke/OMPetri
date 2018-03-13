package edu.unibi.agbi.editor;

import edu.unibi.agbi.editor.core.util.GuiFactory;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;

/**
 * Boot and configuration class.
 * @author PR
 */
@SpringBootApplication // matches @Configuration @ComponentScan @EnableAutoConfig
public class Main extends Application {
    
    private ConfigurableApplicationContext springContext;
    
    @Override
    public void init() throws Exception {
        springContext = SpringApplication.run(Main.class);  // main configuration class
        springContext.registerShutdownHook(); // automatically releases all bean resources on application shutdown
    }

    @Override
    public void start(Stage mainStage) throws Exception {
        GuiFactory guiFactory = springContext.getBean(GuiFactory.class);
        guiFactory.BuildLogWindow();
        guiFactory.BuildMainWindow();
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
