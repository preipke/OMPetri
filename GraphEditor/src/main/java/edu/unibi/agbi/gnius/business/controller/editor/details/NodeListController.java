/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.details;

import edu.unibi.agbi.gnius.business.controller.editor.ElementEditorController;
import edu.unibi.agbi.gnius.business.controller.editor.GraphEditorController;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class NodeListController implements Initializable
{
    @Autowired private GraphEditorController graphController;
    @Autowired private ElementEditorController elementEditorController;
    
    @FXML private ListView<IDataElement> listNodes;
    @FXML private TextField inputFilter;
    @FXML private Button buttonReturn;
    
    private List<IDataElement> nodes; 
    private Set<IDataElement> sources;
    private Set<IDataElement> targets;
    private IDataElement data; 
    
    public void setData(List nodes, IDataElement data) {
        this.nodes = nodes;
        this.data = data;
        this.sources = null;
        this.targets = null;
        if (data != null) {
            sources = new HashSet();
            targets = new HashSet();
            if (data instanceof IDataNode) {
                ((IDataNode) data).getArcsIn().forEach(arc -> sources.add((IDataNode) arc.getSource()));
                ((IDataNode) data).getArcsOut().forEach(arc -> targets.add((IDataNode) arc.getTarget()));
            } else if (data instanceof IDataArc) {
                sources.add(((IDataArc) data).getSource());
                targets.add(((IDataArc) data).getTarget());
            }
        }
        setNodes(nodes);
    }
    
    public void Update() {
        setNodes(nodes);
    }
    
    private void setNodes(List<IDataElement> nodes) {
        String filter = inputFilter.getText();
        ObservableList items = FXCollections.observableArrayList();
        items.addAll(
                nodes.stream()
                        .filter(n -> n.getId().contains(filter) || n.getName().contains(filter) || n.getLabelText().contains(filter))
                        .sorted((n1, n2) -> {
                            if (n1.isSticky() != n2.isSticky()) {
                                if (n1.isSticky()) {
                                    return -1;
                                } else {
                                    return 1;
                                }
                            } else {
                                return n1.toString().compareTo(n2.toString());
                            }
                        }).toArray());
        listNodes.getItems().clear();
        listNodes.setItems(items);
        listNodes.getSelectionModel().select(data);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buttonReturn.setOnAction(eh -> graphController.ShowGraphEditor());
        buttonReturn.setPadding(Insets.EMPTY);
        inputFilter.textProperty().addListener(cl -> setNodes(nodes));
        listNodes.setCellFactory(l -> new NodeCellFormatter());
    }
    
    private class NodeCellFormatter extends ListCell<IDataElement>
    {
        @Override
        protected void updateItem(IDataElement item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                if (item.isSticky()) {
                    setText("> ");
                } else {
                    setText("");
                }
                switch (item.getElementType()) {
                    case PLACE:
                        setText(getText() + "\u25CB " + item.toString());
                        break;
                    case TRANSITION:
                        setText(getText() + "\u25AF " + item.toString());
                        break;
                }
                if (data != null) {
                    if (sources.contains(item)) {
                        setText(getText() + " \u2192");
                        setFont(Font.font(null, FontWeight.BOLD, 12));
                    } else if (targets.contains(item)) {
                        setText(getText() + " \u2190");
                        setFont(Font.font(null, FontWeight.BOLD, 12));
                    } else if (item.getId().contentEquals(data.getId())) {
                        setFont(Font.font(null, FontWeight.BOLD, 13));
                    } else {
                        setFont(Font.font(null, FontWeight.NORMAL, 12));
                    }
                    if (!item.isDisabled()) {
                        setOpacity(1.0);
                    } else {
                        setOpacity(0.5);
                    }
                }
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        elementEditorController.setElement(item);
                    }
                });
            } else {
                setText("");
            }
        }
    }
}
