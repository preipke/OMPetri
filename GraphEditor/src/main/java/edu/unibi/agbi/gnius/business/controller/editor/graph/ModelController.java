/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.graph;

import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ModelController implements Initializable
{
    @FXML private TextField inputAuthor;
    @FXML private TextArea inputDescription;
    @FXML private TextField inputName;
    
    private DataDao dataDaoActive;
    
    public void setDao(DataDao dataDao) {
        if (dataDaoActive != null) {
            updateModelInfo();
        }
        dataDaoActive = dataDao;
        inputAuthor.setText(dataDaoActive.getAuthor());
        inputDescription.setText(dataDaoActive.getModelDescription());
        inputName.setText(dataDaoActive.getModelName());
    }
    
    public void updateModelInfo() {
        dataDaoActive.setAuthor(inputAuthor.getText());
        dataDaoActive.setModelDescription(inputDescription.getText());
        dataDaoActive.setModelName(inputName.getText());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inputName.textProperty().addListener(cl -> dataDaoActive.setModelName(inputName.getText()));
    }
}
