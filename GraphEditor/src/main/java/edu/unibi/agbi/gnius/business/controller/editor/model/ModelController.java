/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.model;

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
    
    public void setModel(DataDao dataDao) {
        if (dataDaoActive != null) {
            updateModelInfo();
        }
        dataDaoActive = dataDao;
        inputAuthor.setText(dataDaoActive.getModel().getAuthor());
        inputDescription.setText(dataDaoActive.getModel().getDescription());
        inputName.setText(dataDaoActive.getModel().getName());
    }
    
    public void updateModelInfo() {
        dataDaoActive.getModel().setAuthor(inputAuthor.getText());
        dataDaoActive.getModel().setDescription(inputDescription.getText());
        dataDaoActive.getModel().setName(inputName.getText());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inputName.textProperty().addListener(cl -> dataDaoActive.getModel().setName(inputName.getText()));
    }
}
