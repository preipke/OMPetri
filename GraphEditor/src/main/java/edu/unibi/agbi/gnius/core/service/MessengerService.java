/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class MessengerService implements Initializable {
    
    public void addToLog(String msg) {
        System.out.println(msg);
    }
    
    public void addToLog(Throwable thr) {
        System.out.println(thr.getMessage());
    }
    
    public void setLeftStatus(String msg) {
        
    }
    
    public void setRightStatus(String msg) {
        
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
