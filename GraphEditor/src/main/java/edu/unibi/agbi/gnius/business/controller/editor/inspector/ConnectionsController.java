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
import javafx.scene.control.Label;
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

    @FXML private Label labelListConnectionsIn;
    @FXML private Label labelListConnectionsOut;
    
    @FXML private ListView<IDataArc> listConnectionsIncoming;
    @FXML private ListView<IDataArc> listConnectionsOutgoing;
    @FXML private ListView<IGraphElement> listGraphEntities;

    @FXML private Button buttonArcFromDisable;
    @FXML private Button buttonArcFromEdit;
    @FXML private Button buttonArcFromShow;

    @FXML private Button buttonArcToDisable;
    @FXML private Button buttonArcToEdit;
    @FXML private Button buttonArcToShow;

    private IDataElement data;

    public void setElement(IDataElement element) {
        data = element;
        Update();
    }

    public void Update() {
        setConnections(data);
        setGraphEntities(data);
        if (data instanceof IDataArc) {
            labelListConnectionsIn.setText("Source Node");
            labelListConnectionsOut.setText("Target Node");
            buttonArcFromDisable.setVisible(false);
            buttonArcToDisable.setVisible(false);
        } else {
            labelListConnectionsIn.setText("Incoming Arcs");
            labelListConnectionsOut.setText("Outgoing Arcs");
            buttonArcFromDisable.setVisible(true);
            buttonArcToDisable.setVisible(true);
        }
        buttonArcFromDisable.setText("Disable");
        buttonArcFromDisable.setDisable(true);
        buttonArcFromEdit.setDisable(true);
        buttonArcFromShow.setDisable(true);
        buttonArcToDisable.setText("Disable");
        buttonArcToDisable.setDisable(true);
        buttonArcToEdit.setDisable(true);
        buttonArcToShow.setDisable(true);
    }

    private void setConnections(IDataElement element) {

        listConnectionsIncoming.getItems().clear();
        listConnectionsOutgoing.getItems().clear();

        if (element == null) {
            return;
        }

        switch (element.getType()) {

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

    private void DisableSelectedElement(ListView<IDataArc> listView) {
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

        buttonArcFromDisable.setOnAction(eh -> DisableSelectedElement(listConnectionsIncoming));
        buttonArcFromEdit.setOnAction(eh -> EditElement(listConnectionsIncoming));
        buttonArcFromShow.setOnAction(eh -> ShowElement(listConnectionsIncoming));

        buttonArcToDisable.setOnAction(eh -> DisableSelectedElement(listConnectionsOutgoing));
        buttonArcToEdit.setOnAction(eh -> EditElement(listConnectionsOutgoing));
        buttonArcToShow.setOnAction(eh -> ShowElement(listConnectionsOutgoing));

        listConnectionsIncoming.setCellFactory(l -> new ConnectionSourceCellFormatter());
        listConnectionsIncoming.getSelectionModel().selectedItemProperty().addListener(cl -> {
            IDataArc arc = listConnectionsIncoming.getSelectionModel().getSelectedItem();
            if (arc != null) {
                if (arc.isDisabled()) {
                    buttonArcFromDisable.setText("Enable");
                } else {
                    buttonArcFromDisable.setText("Disable");
                }
                buttonArcFromDisable.setDisable(false);
                buttonArcFromEdit.setDisable(false);
                buttonArcFromShow.setDisable(false);
            } else {
                buttonArcFromDisable.setDisable(true);
                buttonArcFromEdit.setDisable(true);
                buttonArcFromShow.setDisable(true);
            }
        });

        listConnectionsOutgoing.setCellFactory(l -> new ConnectionTargetCellFormatter());
        listConnectionsOutgoing.getSelectionModel().selectedItemProperty().addListener(cl -> {
            IDataArc arc = listConnectionsOutgoing.getSelectionModel().getSelectedItem();
            if (arc != null) {
                if (arc.isDisabled()) {
                    buttonArcToDisable.setText("Enable");
                } else {
                    buttonArcToDisable.setText("Disable");
                }
                buttonArcToDisable.setDisable(false);
                buttonArcToEdit.setDisable(false);
                buttonArcToShow.setDisable(false);
            } else {
                buttonArcToDisable.setDisable(true);
                buttonArcToEdit.setDisable(true);
                buttonArcToShow.setDisable(true);
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
                    if (listGraphEntities.getSelectionModel().getSelectedItem() instanceof IGraphNode) {
                        IGraphNode node = (IGraphNode) listGraphEntities.getSelectionModel().getSelectedItem();
                        if (isNodeRelated(dataArcItem, node)) {
                            cell.setFont(Font.font(null, FontWeight.BOLD, 12));
                        }
                    } else {
                        System.out.println("NOW");
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
                switch (dataArcItem.getSource().getType()) {
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
                switch (dataArcItem.getTarget().getType()) {
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
                switch (item.getData().getType()) {
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
