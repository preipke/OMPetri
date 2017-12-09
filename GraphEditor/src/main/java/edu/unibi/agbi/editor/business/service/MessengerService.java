/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.business.service;

import edu.unibi.agbi.editor.presentation.controller.LogController;
import edu.unibi.agbi.editor.presentation.controller.MainController;
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
    @Autowired private LogController logController;

    private FadeTransition topStatusFader;

    public void addMessage(String msg) {
        logController.toTextArea(msg);
    }

    public void addWarning(String msg) {
        logController.toTextArea(msg);
    }

    public void addException(Throwable thr) {
        logController.toTextArea("Exception! [" + thr.getMessage() + "]");
        logController.toExceptionsTable(thr);
    }

    public void addException(String msg, Throwable thr) {
        logController.toTextArea(msg + " [" + thr.getMessage() + "]");
        logController.toExceptionsTable(msg, thr);
    }

    public void printMessage(String msg) {
        setTopStatusText(msg);
    }

    public void setStatusAndAddExceptionToLog(String msg, Throwable thr) {
        setTopStatusText(msg);
        addException(msg, thr);
    }

    public void setLeftStatus(String msg) {

    }

    public void setRightStatus(String msg) {
        
    }

    public void setRightStatus(String msg, Throwable thr) {
        addException(msg, thr);
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
