/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller.editor.panel;

import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataCluster;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataClusterArc;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.presentation.controller.editor.GraphController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ClusterPanelController implements IPanel
{
    @Autowired private GraphController graphController;
    @Autowired private ElementPanelController elementPanelController;
    
    @FXML private Parent clusterPanel;
    
    @FXML private ListView<IGraphElement> listClusteredElements;
    @FXML private Button buttonEditClustered;
    @FXML private Button buttonDisableClustered;
    
    public void setElement(IDataElement element) {
        
        switch (element.getType()) {

            case CLUSTER:
                DataCluster cluster = (DataCluster) element;
                listClusteredElements.getItems().clear();
                cluster.getGraph().getNodes().forEach(n -> {
                    listClusteredElements.getItems().add((IGraphElement) n);
                });
                break;

            case CLUSTERARC:
                DataClusterArc clusterArc = (DataClusterArc) element;
                listClusteredElements.getItems().clear();
                listClusteredElements.getItems().addAll(clusterArc.getStoredArcs().values());
                break;
        }
        
    }
    
    private void updateControlButtons(IGraphElement element) {
        if (element != null) {
            buttonDisableClustered.setDisable(false);
            buttonEditClustered.setDisable(false);
            if (element.getData().isDisabled()) {
                buttonDisableClustered.setText("Enable");
            } else {
                buttonDisableClustered.setText("Disable");
            }
        } else {
            buttonDisableClustered.setDisable(true);
            buttonEditClustered.setDisable(true);
        }
    }
    
    @Override
    public Parent getPanel() {
        return clusterPanel;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        buttonEditClustered.setOnAction(eh -> {
            if (listClusteredElements.getSelectionModel().getSelectedItem() != null) {
                IGraphElement elem = listClusteredElements.getSelectionModel().getSelectedItem();
                graphController.ShowInspector(listClusteredElements.getSelectionModel().getSelectedItem().getData());
            }
        });
        
        buttonDisableClustered.setOnAction(eh -> {
            
            IGraphElement elem = listClusteredElements.getSelectionModel().getSelectedItem();
            
            if (elem != null) {
                
                elem.setElementDisabled(!elem.isElementDisabled());
                updateControlButtons(elem);
                
                int index = listClusteredElements.getSelectionModel().getSelectedIndex();
                listClusteredElements.getItems().remove(elem);
                listClusteredElements.getItems().add(index, elem);
            }
            
            elem = elementPanelController.getSelectedGraphElement();
            elementPanelController.updateControlButtons(elem);
            ((DataCluster) elem.getData()).UpdateShape();
        });

        listClusteredElements.setCellFactory(l -> new ClusterCellFormatter());
        listClusteredElements.getSelectionModel().selectedItemProperty().addListener(cl -> {
            updateControlButtons(listClusteredElements.getSelectionModel().getSelectedItem());
        });
        
    }
    
    private class ClusterCellFormatter extends ListCell<IGraphElement>
    {
        @Override
        protected void updateItem(IGraphElement item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                setText(item.toString());
                setOpacity(1.0);
                if (item.isElementDisabled()) {
                    setOpacity(0.5);
                }
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        try {
                            elementPanelController.setElement(item);
                        } catch (Exception ex) {
//                            Logger.getLogger(ClusterPanelController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            } else {
                setText("");
            }
        }
    }
}
