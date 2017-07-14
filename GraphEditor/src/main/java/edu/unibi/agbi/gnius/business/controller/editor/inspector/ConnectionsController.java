/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.inspector;

import edu.unibi.agbi.gnius.business.controller.editor.InspectorController;
import edu.unibi.agbi.gnius.business.controller.editor.GraphController;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ConnectionsController implements Initializable
{
    @Autowired private InspectorController inspectorController;
    @Autowired private GraphController graphController;

    @FXML private ListView<IDataArc> listConnectionsIncoming;
    @FXML private ListView<IDataArc> listConnectionsOutgoing;
    @FXML private ListView<IGraphElement> listGraphEntities;

    @FXML private Button buttonConnectionFromDisable;
    @FXML private Button buttonConnectionToDisable;
    @FXML private Button buttonConnectionFromEdit;
    @FXML private Button buttonConnectionToEdit;
    @FXML private Button buttonConnectionFromShow;
    @FXML private Button buttonConnectionToShow;

    private IDataElement data;

    public void setElement(IDataElement element) {
        data = element;
        Update();
    }

    public void Update() {
        setConnections(data);
        setGraphEntities(data);
        buttonConnectionToDisable.setText("Disable");
        buttonConnectionFromDisable.setDisable(true);
        buttonConnectionFromEdit.setDisable(true);
        buttonConnectionFromShow.setDisable(true);
        buttonConnectionToDisable.setDisable(true);
        buttonConnectionToEdit.setDisable(true);
        buttonConnectionToShow.setDisable(true);
    }

    private void setConnections(IDataElement element) {

        listConnectionsIncoming.getItems().clear();
        listConnectionsOutgoing.getItems().clear();
        
        if (element == null) {
            return;
        }

        switch (element.getDataType()) {

            case ARC:
                IDataArc arc = (IDataArc) element;
                listConnectionsIncoming.getItems().add(arc);
                listConnectionsOutgoing.getItems().add(arc);
                break;

            default:
                IDataNode node = (IDataNode) element;
                node.getArcsIn().forEach(a -> listConnectionsIncoming.getItems().add((IDataArc) a));
                node.getArcsOut().forEach(a -> listConnectionsOutgoing.getItems().add((IDataArc) a));
        }

    }

    private void setGraphEntities(IDataElement element) {
        listGraphEntities.getItems().clear();
        if (element != null) {
            listGraphEntities.getItems().addAll(element.getShapes());
        }
    }

    private void DisableElement(ListView<IDataArc> listView) {
        IDataArc arc = listView.getSelectionModel().getSelectedItem();
        int index = listView.getSelectionModel().getSelectedIndex();
        if (arc != null) {
            arc.setDisabled(!arc.isDisabled());
            listView.getItems().remove(arc);
            listView.getItems().add(index, arc);
            listView.getSelectionModel().select(arc);
        }
        if (arc == data) { // rebuild if active element is arc
            Update();
        }
    }

    private void EditElement(ListView<IDataArc> listView) {
        IDataArc arc = listView.getSelectionModel().getSelectedItem();
        if (arc != null) {
            inspectorController.setElement(arc.getShapes().iterator().next().getData());
        }
    }

    private void ShowElement(ListView<IDataArc> listView) {
        IDataArc arc = listView.getSelectionModel().getSelectedItem();
        if (arc != null) {
            graphController.FocusGraphElement(arc.getShapes().iterator().next());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        buttonConnectionFromDisable.setOnAction(eh -> DisableElement(listConnectionsIncoming));
        buttonConnectionFromEdit.setOnAction(eh -> EditElement(listConnectionsIncoming));
        buttonConnectionFromShow.setOnAction(eh -> ShowElement(listConnectionsIncoming));
        buttonConnectionToDisable.setOnAction(eh -> DisableElement(listConnectionsOutgoing));
        buttonConnectionToEdit.setOnAction(eh -> EditElement(listConnectionsOutgoing));
        buttonConnectionToShow.setOnAction(eh -> ShowElement(listConnectionsOutgoing));

        listConnectionsIncoming.setCellFactory(l -> new ConnectionSourceCellFormatter());
        listConnectionsIncoming.getSelectionModel().selectedItemProperty().addListener(cl -> {
            IDataArc arc = listConnectionsIncoming.getSelectionModel().getSelectedItem();
            if (arc != null) {
                if (arc.isDisabled()) {
                    buttonConnectionFromDisable.setText("Enable");
                } else {
                    buttonConnectionFromDisable.setText("Disable");
                }
                buttonConnectionFromDisable.setDisable(false);
                buttonConnectionFromEdit.setDisable(false);
                buttonConnectionFromShow.setDisable(false);
            } else {
                buttonConnectionFromDisable.setDisable(true);
                buttonConnectionFromEdit.setDisable(true);
                buttonConnectionFromShow.setDisable(true);
            }
        });
        listConnectionsOutgoing.setCellFactory(l -> new ConnectionTargetCellFormatter());
        listConnectionsOutgoing.getSelectionModel().selectedItemProperty().addListener(cl -> {
            IDataArc arc = listConnectionsOutgoing.getSelectionModel().getSelectedItem();
            if (arc != null) {
                if (arc.isDisabled()) {
                    buttonConnectionToDisable.setText("Enable");
                } else {
                    buttonConnectionToDisable.setText("Disable");
                }
                buttonConnectionToDisable.setDisable(false);
                buttonConnectionToEdit.setDisable(false);
                buttonConnectionToShow.setDisable(false);
            } else {
                buttonConnectionToDisable.setDisable(true);
                buttonConnectionToEdit.setDisable(true);
                buttonConnectionToShow.setDisable(true);
            }
        });
        listGraphEntities.setCellFactory(l -> new GraphEntityCellFormatter());
        listGraphEntities.getSelectionModel().selectedItemProperty().addListener(cl -> {
            if (listGraphEntities.getSelectionModel().getSelectedItem() != null) {
                IDataArc arcIn = listConnectionsIncoming.getSelectionModel().getSelectedItem();
                IDataArc arcOut = listConnectionsOutgoing.getSelectionModel().getSelectedItem();
                setConnections(data);
                if (arcIn != null) {
                    listConnectionsIncoming.getSelectionModel().select(arcIn);
                }
                if (arcOut != null) {
                    listConnectionsOutgoing.getSelectionModel().select(arcOut);
                }
            }
        });
    }

    /**
     * Indicates wether or not a given graph node is related to a given data
     * arc.
     *
     * @param arc
     * @param node
     * @return
     */
    private boolean isNodeRelated(IDataArc arc, IGraphNode node) {
        if (arc.getSource().equals(node.getData())) { // node is source ->
            for (IGraphElement element : arc.getTarget().getShapes()) {
                if (node.getChildren().contains(element)) {
                    return true;
                }
            }
        } else if (arc.getTarget().equals(node.getData())) { // node is target ->
            for (IGraphElement element : arc.getSource().getShapes()) {
                if (node.getParents().contains(element)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void InitConnectionCell(ListCell<IDataArc> cell, IDataArc dataArcItem) {
        cell.setFont(Font.font(null, FontWeight.NORMAL, 12));
        if (data != null) { // mark connections related to selected graph entity
            if (data instanceof IDataNode) {
                if (listGraphEntities.getSelectionModel().getSelectedItem() != null) {
                    IGraphNode node = (IGraphNode) listGraphEntities.getSelectionModel().getSelectedItem();
                    if (isNodeRelated(dataArcItem, node)) {
                        cell.setFont(Font.font(null, FontWeight.BOLD, 12));
                    }
                }
            } else { // for an arc, all are related
                cell.setFont(Font.font(null, FontWeight.BOLD, 12));
            }
        }
        if (!dataArcItem.isDisabled()) {
            cell.setOpacity(1.0);
        } else {
            cell.setOpacity(0.5);
        }
    }

    private class ConnectionSourceCellFormatter extends ListCell<IDataArc>
    {
        @Override
        protected void updateItem(IDataArc dataArcItem, boolean empty) {
            super.updateItem(dataArcItem, empty);
            setText("");
            if (dataArcItem != null) {
                InitConnectionCell(this, dataArcItem);
                switch (dataArcItem.getSource().getDataType()) {
                    case PLACE:
                        setText(getText() + "\u25CB\u2192  " + dataArcItem.getSource().toString());
                        break;
                    case TRANSITION:
                        setText(getText() + "\u25AF\u2192  " + dataArcItem.getSource().toString());
                        break;
                }
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        inspectorController.setElement(dataArcItem.getSource());
                    }
                });
            }
        }
    }

    private class ConnectionTargetCellFormatter extends ListCell<IDataArc>
    {
        @Override
        protected void updateItem(IDataArc dataArcItem, boolean empty) {
            super.updateItem(dataArcItem, empty);
            setText("");
            if (dataArcItem != null) {
                InitConnectionCell(this, dataArcItem);
                switch (dataArcItem.getTarget().getDataType()) {
                    case PLACE:
                        setText("\u2192\u25CB  " + dataArcItem.getTarget().toString());
                        break;
                    case TRANSITION:
                        setText("\u2192\u25AF  " + dataArcItem.getTarget().toString());
                        break;
                }
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        inspectorController.setElement(dataArcItem.getTarget());
                    }
                });
            }
        }
    }

    private class GraphEntityCellFormatter extends ListCell<IGraphElement>
    {
        @Override
        protected void updateItem(IGraphElement item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                switch (item.getData().getDataType()) {
                    case PLACE:
                        setText("\u25CB  " + item.toString());
                        break;
                    case TRANSITION:
                        setText("\u25AF  " + item.toString());
                        break;
                    default:
                        setText(item.toString());
                }
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        graphController.FocusGraphElement(item);
                    }
                });
            } else {
                setText("");
            }
        }
    }
}
