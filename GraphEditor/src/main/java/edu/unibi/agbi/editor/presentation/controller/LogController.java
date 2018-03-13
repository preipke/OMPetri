/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 *
 * @author PR
 */
@Controller
public class LogController implements Initializable
{
    @FXML private TextArea textLog;
    @FXML private TableView<TableLogData> tableLog;
    @FXML private TableColumn<TableLogData, String> columnTime;
    @FXML private TableColumn<TableLogData, String> columnType;
    @FXML private TableColumn<TableLogData, String> columnMessage;
    @FXML private TableColumn<TableLogData, String> columnCause;
    
    private ObservableList<TableLogData> tableLogData;
    private Stage stage;
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void ShowWindow() {
        stage.show();
        stage.setIconified(false);
        stage.toFront();
    }
    
    public void toTextArea(String msg) {
        textLog.appendText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "   " + msg + "\n");
        textLog.setScrollTop(Double.MAX_VALUE);
    }
    
    public void toExceptionsTable(Throwable cause) {
        toExceptionsTable(null, cause);
    }
    
    public void toExceptionsTable(String msg, Throwable cause) {
        tableLogData.add(0, new TableLogData(msg, cause));
        if (tableLogData.size() > 100) {
            tableLogData.remove(100);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tableLogData = tableLog.getItems();
        columnTime.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTime().format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"))));
        columnType.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getThrowable().getClass().getSimpleName()));
        columnMessage.setCellValueFactory(cellData -> {
            if (cellData.getValue().getMessage() != null) {
                return new ReadOnlyStringWrapper(cellData.getValue().getMessage());
            } else {
                return new ReadOnlyStringWrapper("-");
            }
        });
        columnCause.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getThrowable().getMessage()));
    }
    
    private class TableLogData {
        
        private final LocalDateTime time;
        private final String msg;
        private final Throwable thr;
        
        private TableLogData(String msg, Throwable thr) {
            this.time = LocalDateTime.now();
            this.msg = msg;
            this.thr = thr;
        }
        
        private LocalDateTime getTime() {
            return time;
        }
        
        private String getMessage() {
            return msg;
        }
        
        private Throwable getThrowable() {
            return thr;
        }
    }
}
