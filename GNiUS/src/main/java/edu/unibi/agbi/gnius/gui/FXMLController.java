package edu.unibi.agbi.gnius.gui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class FXMLController implements Initializable {
    
    @FXML
    private Label graphTestLabel;
    
    @FXML
    private void addNodes(ActionEvent event) {
        System.out.println("You clicked me!");
        graphTestLabel.setText("Added test nodes!");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
}
