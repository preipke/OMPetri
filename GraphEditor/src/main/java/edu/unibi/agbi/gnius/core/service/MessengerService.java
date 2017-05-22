/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.business.controller.MainController;
import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class MessengerService
{
    @Autowired private MainController mainController;
    
    private FadeTransition topStatusFader;

    public void addToLog(String msg) {
        System.out.println(msg);
    }

    public void addToLog(Throwable thr) {
        System.out.println(thr.getMessage());
    }

    public void addToLog(String msg, Throwable thr) {
        System.out.println(msg + " [" + thr.getMessage() + "]");
    }

    public void setLeftStatus(String msg) {

    }

    public void setRightStatus(String msg) {

    }

    /**
     * Prints a message in the top status label.
     *
     * @param msg the message to print
     * @param thr indicates wether this status is related to an error, otherwise
     *            set to null
     */
    public void setTopStatus(String msg, Throwable thr) {
        
        setTopStatusText(msg);
        
        if (thr != null) {
            addToLog(msg, thr);
        }
    }
    
    private void setTopStatusText(final String msg) {
        
        Label label = mainController.getStatusTop();
        label.setText(msg);
        label.setVisible(true);
        
        if (topStatusFader == null) {
            topStatusFader = new FadeTransition(Duration.millis(3000));
            topStatusFader.setNode(label);
            topStatusFader.setFromValue(1.0);
            topStatusFader.setToValue(0.0);
            topStatusFader.setCycleCount(1);
            topStatusFader.setAutoReverse(false);
        }
        topStatusFader.playFromStart();
    }
}
