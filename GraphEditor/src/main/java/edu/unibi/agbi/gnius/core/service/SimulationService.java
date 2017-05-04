/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.business.controller.ElementController;
import edu.unibi.agbi.gnius.business.controller.SimulationController;
import edu.unibi.agbi.gnius.core.model.dao.SimulationResultsDao;
import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.core.exception.SimulationServiceException;
import edu.unibi.agbi.gnius.core.service.simulation.SimulationCompiler;
import edu.unibi.agbi.gnius.core.service.simulation.SimulationExecuter;
import edu.unibi.agbi.gnius.core.service.simulation.SimulationServer;
import edu.unibi.agbi.gnius.util.Utility;
import edu.unibi.agbi.petrinet.model.References;
import java.io.File;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class SimulationService
{
    private final SimulationResultsDao simulationDao;

    @Value("${application.working.dir.property}") private String varWorkingDirProperty;
    @Value("${application.working.dir.subfolder}") private String varWorkingDirSubfolder;
    @Value("${openmodelica.home.env}") private String varOpenModelicaHomeDir;
    
    @Autowired private DataGraphService dataGraphService;
    @Autowired private SimulationService simulationService;
    @Autowired private MessengerService messengerService;
    
    @Autowired private SimulationController simulationControlsController;
    @Autowired private ElementController elementDetailsController;
    
    @Autowired 
    public SimulationService(SimulationResultsDao simulationDao) {
        this.simulationDao = simulationDao;
    }
    
    public ObservableList<Simulation> getSimulations() {
        return simulationDao.getSimulations();
    }
    
    public Simulation InitSimulation(String authorName, String modelName, String[] variables, References variableReferences) {
        Simulation simulation = new Simulation(authorName, modelName, variables, variableReferences);
        simulationDao.add(simulation);
        return simulation;
    }
    
    public void StartSimulation() throws SimulationServiceException {
        
        try {
            elementDetailsController.StoreElementProperties();
        } catch (DataGraphServiceException ex) {
            throw new SimulationServiceException(ex);
        }

//        if (server.isRunning()) {
//            throw new SimulationServiceException("Server is still running!");
//        }
        messengerService.addToLog("Starting simulation...");

        Thread thread = new Thread(() -> {
            try {
                /**
                 * Execute compiler.
                 */
                SimulationCompiler simulationCompiler = new SimulationCompiler(this, dataGraphService.getDataDao());
                try {
                    Platform.runLater(() -> {
                        simulationControlsController.setSimulationProgress(-1);
                    });
                    simulationCompiler.compile();
                } catch (SimulationServiceException ex) {
                    throw new SimulationServiceException("Simulation failed during compilation! [" + ex.getMessage() + "]");
                }

                /**
                 * Start server.
                 */
                SimulationServer simulationServer = new SimulationServer();
                simulationServer.start();
                synchronized (simulationServer) {
                    try {
                        simulationServer.wait(); // wait for notification from server -> started or failed
                    } catch (InterruptedException ex) {
                        throw new SimulationServiceException("Simulation failed while waiting for server start! [" + ex.getMessage() + "]");
                    }
                }
                if (!simulationServer.isRunning()) {
                    if (simulationServer.isFailed()) {
                        throw new SimulationServiceException("Simulation failed while starting the server! [" + simulationServer.getErrorMessage() + "]");
                    } else {
                        throw new SimulationServiceException("Simulation server is not running!");
                    }
                }

                /**
                 * Start simulation.
                 */
                SimulationExecuter simulationExecuter = new SimulationExecuter(simulationCompiler, simulationServer);
                simulationExecuter.setSimulationStopTime(simulationControlsController.getSimulationStopTime());
                simulationExecuter.setSimulationIntervals(simulationControlsController.getSimulationIntervals());
                simulationExecuter.setSimulationIntegrator(simulationControlsController.getSimulationIntegrator());
                simulationExecuter.start();
                synchronized (simulationExecuter) {
                    if (simulationExecuter.getSimulationOutputReader() == null) {
                        try {
                            simulationExecuter.wait(); // wait for notification from executer -> process started or failed
                        } catch (InterruptedException ex) {
                            throw new SimulationServiceException("Simulation failed while waiting for simulation process start! [" + ex.getMessage() + "]");
                        }
                    }
                }
                if (simulationExecuter.isFailed()) {
                    throw new SimulationServiceException("Simulation failed while starting the simulation process! [" + simulationExecuter.getErrorMessage() + "]");
                }

                /**
                 * Initialize simulation data storage.
                 */
                synchronized (simulationServer) {
                    if (simulationServer.isRunning() && simulationServer.getSimulationVariables() == null) {
                        try {
                            simulationServer.wait(); // wait for notification from server -> variables initialized or failed
                        } catch (InterruptedException ex) {
                            throw new SimulationServiceException("Simulation failed while waiting for data storage initialization! [" + ex.getMessage() + "]");
                        }
                    }
                }
                if (!simulationServer.isRunning()) {
                    if (simulationServer.isFailed()) {
                        throw new SimulationServiceException("Simulation failed while initializing variables! [" + simulationServer.getErrorMessage() + "]");
                    } else {
                        throw new SimulationServiceException("Simulation server is not running!");
                    }
                }

                Platform.runLater(() -> {
                    Simulation simulation = simulationService.InitSimulation(
                            dataGraphService.getDataDao().getAuthor(), 
                            dataGraphService.getDataDao().getName(), 
                            simulationServer.getSimulationVariables(), 
                            simulationCompiler.getSimulationVariableReferences()
                    );
                    simulationServer.setSimulation(simulation);
                    synchronized (simulationServer) {
                        simulationServer.notify(); // notify server that storage has been initalized
                    }
                });

                try {
                    synchronized (this) {
                        while (simulationServer.isRunning()) {
                            Platform.runLater(() -> {
                                simulationControlsController.setSimulationProgress(simulationServer.getSimulationIterationCount() / simulationControlsController.getSimulationIntervals());
                            });
                            this.wait(250);
                        }
                    }
                    simulationExecuter.join();
                    simulationServer.join();
                } catch (InterruptedException ex) {
                    
                }
            } catch (SimulationServiceException ex) {
                messengerService.addToLog(ex.getMessage());
            } finally {
                messengerService.addToLog("Finished simulating!");
                Platform.runLater(() -> {
                    simulationControlsController.setSimulationProgress(1);
                    simulationControlsController.StopSimulation();
                });
            }

//            /**
//             * Read simulation output. (?)
//             */
//            Thread t3 = new Thread() {
//                public void run() {
//                    String line;
//                    if (simulationOutput != null) {
//                        while (server.isRunning()) {
//                            try {
//                                line = simulationOutput.readLine();
//                                if (line != null && line.length() > 0) {
//                                    System.out.println(line);
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            try {
//                                sleep(100);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        try {
//                            System.out.println("Server stopped");
//                            while ((line = simulationOutput.readLine()) != null && line.length() > 0) {
//                                System.out.println(line);
//                            }
//                            simulationOutput.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            };
//            t3.start();
        });
        thread.start();
    }
    
    public void StopSimulation() {
        // if !isSimulating -> return
    }

    /**
     * Gets the path of the OMC compiler.
     * @return
     * @throws SimulationServiceException
     */
    public String getOpenModelicaCompilerPath() throws SimulationServiceException {

        String pathOpenModelica, pathCompiler;
        File dirOpenModelica;

        pathOpenModelica = System.getenv(varOpenModelicaHomeDir);

        if (pathOpenModelica == null) {
            throw new SimulationServiceException("'" + varOpenModelicaHomeDir + "' environment variable is not set! Please install OpenModelica or set the variable correctly.");
        }

        dirOpenModelica = new File(pathOpenModelica);
        if (dirOpenModelica.exists() && dirOpenModelica.isDirectory()) {

            pathCompiler = pathOpenModelica + "bin" + File.separator + "omc";
            if (Utility.isOsWindows()) {
                pathCompiler = pathCompiler + ".exe";
            } else if (Utility.isOsUnix()) {
                // TODO : OS is Linux
            } else if (Utility.isOsMac()) {
                // TODO : OS is Mac
            } else {
                // TODO : OS not maybe supported
            }
            return pathCompiler;

        } else {
            throw new SimulationServiceException("'" + varOpenModelicaHomeDir + "' environment variable is not set correctly! Please set the variable correctly or reinstall OpenModelica.");
        }
    }

    /**
     * Gets the working directory. Used for storing and executing the compiled
     * sources.
     *
     * @return
     * @throws SimulationServiceException
     */
    public File getWorkingDirectory() throws SimulationServiceException {

        File dir;
        String[] subDir;

        dir = new File(System.getProperty(varWorkingDirProperty));
        if (!dir.exists() || !dir.isDirectory()) {
            throw new SimulationServiceException("Application's working directory not accessible!");
        }

        subDir = varWorkingDirSubfolder.split("/");

        for (String folder : subDir) {
            dir = new File(dir + File.separator + folder);
            if (!dir.exists() || !dir.isDirectory()) {
                dir.mkdir();
            }
        }

        return dir;
    }
}
