/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.menu;

import edu.unibi.agbi.gnius.business.controller.MainController;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.petrinet.util.OpenModelicaExporter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class FileMenuController implements Initializable
{
    @Autowired private DataGraphService dataService;
    @Autowired private MainController mainController;
    @Autowired private OpenModelicaExporter omExporter;
    
    @FXML
    public void SaveFileAs() {
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Petri Net");
        
        File file = fileChooser.showSaveDialog(mainController.getStage());
        
        try {
            omExporter.exportMO(dataService.getDataDao() , file);
        } catch (IOException ex) {

        }
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
    }
}
