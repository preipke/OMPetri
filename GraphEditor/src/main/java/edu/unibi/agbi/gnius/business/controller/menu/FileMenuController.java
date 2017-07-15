/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.menu;

import edu.unibi.agbi.gnius.business.controller.editor.GraphController;
import edu.unibi.agbi.gnius.business.controller.MainController;
import edu.unibi.agbi.gnius.core.io.SbmlModelConverter;
import edu.unibi.agbi.gnius.core.io.XmlModelConverter;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.service.DataService;
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
import javafx.stage.Stage;
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
    @Autowired private DataService dataService;
    @Autowired private MessengerService messengerService;
    
    @Autowired private GraphController graphController;
    @Autowired private MainController mainController;

    @Autowired private OpenModelicaExporter omExporter;
    @Autowired private SbmlModelConverter sbmlModelConverter;
    @Autowired private XmlModelConverter xmlModelConverter;

    @FXML private Menu menuLatestFiles;
    @FXML private MenuItem buttonNew;
    @FXML private MenuItem buttonOpen;
    @FXML private MenuItem buttonQuit;
    @FXML private MenuItem buttonSave;
    @FXML private MenuItem buttonSaveAs;

    private final ExtensionFilter typeAll;
    private final ExtensionFilter typeXml;
    private final ExtensionFilter typeSbml;
//    private final ExtensionFilter typeOm;

    private final FileChooser fileChooser;
    private final ObservableList<File> latestFiles;
    private ExtensionFilter latestFilter;

    public FileMenuController() {

        typeAll = new ExtensionFilter("All files", "*");
        typeXml = new ExtensionFilter("XML file(s) (*.xml)", "*.xml", "*.XML");
        typeSbml = new ExtensionFilter("SBML file(s) (*.sbml)", "*.sbml", "*.SBML");
//        typeOm = new ExtensionFilter("OpenModelica file(s) (*.om)", "*.om", "*.OM");

        fileChooser = new FileChooser();

        latestFiles = FXCollections.observableArrayList();
    }

    private void Open(File file) throws Exception {

        DataDao dataDao; 
        String[] tmp;
        String fileExtension;
        
        tmp = file.getName().split("\\.");
        fileExtension = tmp[tmp.length-1].toLowerCase();
        
        if (fileExtension.contentEquals("xml")) {
            dataDao = xmlModelConverter.importXml(file);
        } else if (fileExtension.contentEquals("sbml")) {
            dataDao = sbmlModelConverter.importSbml(file);
        } else {
            throw new IOException("Unrecognized file extension.");
        }
        dataDao.setFile(file);
        graphController.CreateTab(dataDao);
        
        if (latestFiles.contains(file)) {
            menuLatestFiles.getItems().remove(latestFiles.indexOf(file));
            latestFiles.remove(latestFiles.indexOf(file));
        }
        if (latestFiles.size() == 5) {
            menuLatestFiles.getItems().remove(4);
            latestFiles.remove(4);
        }
        latestFiles.add(0, file);
    }

    private boolean SaveFile(DataDao dao, File file, ExtensionFilter filter) {
        if (file == null || filter == null) {
            return false;
        }
        if (typeXml == filter) {
            return SaveXml(dao, file);
//        } else if (typeSbml == filter) {
//            return SaveSbml(dao, file);
//        } else if (typeOm == filter) {
//            return SaveOm(dao, file);
        } else {
            return false;
        }
    }
    
    private boolean SaveXml(DataDao dao, File file) {
        try {
            xmlModelConverter.exportXml(file, dao);
            messengerService.printMessage("XML export complete!");
            dataService.getDao().setFile(file);
            dataService.getDao().setHasChanges(false);
            return true;
        } catch (IOException | ParserConfigurationException | TransformerException ex) {
            messengerService.printMessage("XML export failed!");
            messengerService.setStatusAndAddExceptionToLog("XML export to '" + file.getName() + "' failed!", ex);
            return false;
        }
    }

    private boolean SaveSbml(DataDao dao, File file) {
        messengerService.printMessage("SBML export is not yet implemented!");
        messengerService.addWarning("SBML export is not yet implemented! Select a different format.");
        return false;
    }

    private boolean SaveOm(DataDao dao, File file) {
        try {
            omExporter.exportMO(dao.getModelName(), dao.getModel(), file);
            messengerService.printMessage("OpenModelica export complete!");
        } catch (IOException ex) {
            messengerService.printMessage("OpenModelica export failed!");
            messengerService.setStatusAndAddExceptionToLog("OpenModelica export failed!", ex);
        }
        return false;
    }
    
    public File ShowSaveFile(Stage stage, DataDao dao) {
        
        if (dao == null) {
            return null;
        }
        
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(typeXml);
//        fileChooser.getExtensionFilters().add(typeSbml);
//        fileChooser.getExtensionFilters().add(typeOm);
        fileChooser.setTitle("Save model '" + dao.getModelName() + "'");
        
        if (dao.getFile() != null) {
            fileChooser.setInitialDirectory(dao.getFile().getParentFile());
            fileChooser.setInitialFileName(dao.getFile().getName());
        } else {
            fileChooser.setInitialFileName(dao.getModelName());
        }
        
        File file =  fileChooser.showSaveDialog(stage);
        if (file != null) {
            latestFilter = fileChooser.getSelectedExtensionFilter();
        }
        return file;
    }
    
    public File ShowOpenFile() {
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(typeAll);
        fileChooser.getExtensionFilters().add(typeXml);
        fileChooser.getExtensionFilters().add(typeSbml);
        fileChooser.setTitle("Open model");
        return fileChooser.showOpenDialog(mainController.getStage());
    }

    public void Open() {
        File file = ShowOpenFile();
        if (file != null) {
            try {
                Open(file);
            } catch (Exception ex) {
                ex.printStackTrace();
                messengerService.printMessage("File import failed!");
                messengerService.setStatusAndAddExceptionToLog("Importing data from '" + file.getName() + "' failed!", ex);
            }
        }
    }

    public void Save() {
        
        DataDao dao = dataService.getDao();
        File file = dao.getFile();
        ExtensionFilter filter = null;
        
        if (file != null) {
            
            String tmp[] = file.getName().replace(".", " ").split(" ");
            
            if (tmp.length > 0) {

                String fileExt = "*." + tmp[tmp.length - 1];

                for (String ext : typeXml.getExtensions()) {
                    if (fileExt.equalsIgnoreCase(ext)) {
                        filter = typeXml;
                        break;
                    }
                }
            }
        }
        
        if (filter != null) {
            SaveFile(dao, file, filter);
        } else {
            SaveAs(dao);
        }
    }
    
    /**
     * 
     * @param stage
     * @param dao
     * @return indicates wether the model has been saved or not
     */
    public boolean SaveAs(DataDao dao) {
        File file = ShowSaveFile(mainController.getStage(), dao);
        return SaveFile(dao, file, latestFilter);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        buttonNew.setOnAction(eh -> graphController.CreateTab());
        buttonQuit.setOnAction(eh -> mainController.ShowDialogExit(null));
        buttonOpen.setOnAction(eh -> Open());
        buttonSave.setOnAction(eh -> Save());
        buttonSaveAs.setOnAction(eh -> SaveAs(dataService.getDao()));
        
        buttonSave.setDisable(true);
        buttonSaveAs.setDisable(true);
        
        menuLatestFiles.setDisable(true);
        latestFiles.addListener(new ListChangeListener()
        {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                if (change.next() && change.wasAdded()) {
                    change.getAddedSubList().forEach(f -> {
                        File file = (File) f;
                        MenuItem item = new MenuItem(file.getName() + " (" + file.getAbsolutePath() + ")");
                        item.setOnAction(e -> {
                            try {
                                Open(file);
                            } catch (Exception ex) {
                                messengerService.printMessage("File import failed!");
                                messengerService.setStatusAndAddExceptionToLog("Importing data from '" + file.getName() + "' failed!", ex);
                            }
                        });
                        menuLatestFiles.getItems().add(0, item);
                        menuLatestFiles.setDisable(false);
                    });
                }
            }
        });
//        graphController
        dataService.getDaos().addListener(new ListChangeListener()
        {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                if (dataService.getDaos().size() > 0) {
                    buttonSave.setDisable(false);
                    buttonSaveAs.setDisable(false);
                } else {
                    buttonSave.setDisable(true);
                    buttonSaveAs.setDisable(true);
                }
            }
        });
    }
}
