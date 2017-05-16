/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.menu;

import edu.unibi.agbi.gnius.business.controller.MainController;
import edu.unibi.agbi.gnius.core.io.XmlModelConverter;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.petrinet.util.OpenModelicaExporter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class FileMenuController implements Initializable
{
    @Autowired private MessengerService messengerService;
    @Autowired private DataGraphService dataService;
    @Autowired private MainController mainController;

    @Autowired private XmlModelConverter xmlModelConverter;
    @Autowired private OpenModelicaExporter omExporter;

    @FXML private Menu menuOpenRecent;

    private final ExtensionFilter typeAll;
    private final ExtensionFilter typeXml;
    private final ExtensionFilter typeSbml;
    private final ExtensionFilter typeOm;

    private final FileChooser fileChooser;
    private final ObservableList<File> latestFiles;

    private ExtensionFilter latestFilter;
    private File latestFile;

    public FileMenuController() {

        typeAll = new ExtensionFilter("All files", "*");
        typeXml = new ExtensionFilter("XML file(s) (*.xml)", "*.xml", "*.XML");
        typeSbml = new ExtensionFilter("SBML file(s) (*.sbml)", "*.sbml", "*.SBML");
        typeOm = new ExtensionFilter("OpenModelica file(s) (*.om)", "*.om", "*.OM");

        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(typeXml);
        fileChooser.getExtensionFilters().add(typeSbml);
        fileChooser.getExtensionFilters().add(typeOm);

        latestFiles = FXCollections.observableArrayList();
    }

    private void Open(File file) {

        /**
         * File import here...
         */
        DataDao dataDao = xmlModelConverter.importXml(file);
        
        if (latestFiles.contains(file)) {
            menuOpenRecent.getItems().remove(latestFiles.indexOf(file));
            latestFiles.remove(latestFiles.indexOf(file));
        }
        if (latestFiles.size() == 5) {
            menuOpenRecent.getItems().remove(4);
            latestFiles.remove(4);
        }
        latestFiles.add(0, file);
    }

    private void SaveFile(File file, ExtensionFilter filter) {
        if (file == null || filter == null) {
            return;
        }
        if (typeXml == filter) {
            try {
                xmlModelConverter.exportXml(file, dataService.getGraphDao(), dataService.getDataDao());
                messengerService.setTopStatus("XML export complete!", null);
            } catch (FileNotFoundException | ParserConfigurationException | TransformerException ex) {
                messengerService.setTopStatus("XML export failed!", ex);
            }
        } else if (typeSbml == filter) {
            messengerService.setTopStatus("SBML export is not yet implemented!", null);
            SaveAs();
        } else if (typeOm == filter) {
            try {
                omExporter.exportMO(dataService.getDataDao(), file);
                messengerService.setTopStatus("OpenModelica export complete!", null);
            } catch (IOException ex) {
                messengerService.setTopStatus("OpenModelica export failed!", ex);
            }
        }
    }

    @FXML
    public void Open() {
        if (!fileChooser.getExtensionFilters().contains(typeAll)) {
            fileChooser.getExtensionFilters().add(typeAll);
        }
        fileChooser.setSelectedExtensionFilter(typeAll);
        fileChooser.setTitle("Open model data");
        File file = fileChooser.showOpenDialog(mainController.getStage());
        if (file != null) {
            Open(file);
        }
    }

    @FXML
    public void Save() {
        if (latestFile != null && latestFilter != null) {
            SaveFile(latestFile, latestFilter);
        } else {
            SaveAs();
        }
    }

    @FXML
    public void SaveAs() {
        fileChooser.getExtensionFilters().remove(typeAll);
        fileChooser.setTitle("Save model data");
        latestFile = fileChooser.showSaveDialog(mainController.getStage());
        latestFilter = fileChooser.getSelectedExtensionFilter();
        SaveFile(latestFile, latestFilter);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        latestFiles.addListener(new ListChangeListener()
        {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                if (change.next() && change.wasAdded()) {
                    change.getAddedSubList().forEach(f -> {
                        File file = (File) f;
                        MenuItem item = new MenuItem(file.getName() + " (" + file.getAbsolutePath() + ")");
                        item.setOnAction(e -> Open(file));
                        menuOpenRecent.getItems().add(0, item);
                    });
                }
            }
        });
    }
}
