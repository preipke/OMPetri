/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.business.service;

import edu.unibi.agbi.editor.presentation.controller.editor.graph.SimulationController;
import edu.unibi.agbi.editor.core.data.dao.ModelDao;
import edu.unibi.agbi.editor.core.data.entity.result.Simulation;
import edu.unibi.agbi.editor.business.exception.ResultsException;
import edu.unibi.agbi.editor.business.exception.SimulationException;
import edu.unibi.agbi.editor.business.service.simulation.SimulationCompiler;
import edu.unibi.agbi.editor.business.service.simulation.SimulationExecuter;
import edu.unibi.agbi.editor.business.service.simulation.SimulationServer;
import edu.unibi.agbi.editor.core.util.Utility;
import edu.unibi.agbi.petrinet.util.OpenModelicaExporter;
import edu.unibi.agbi.petrinet.util.References;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
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
    @Autowired private OpenModelicaExporter omExporter;
    @Autowired private ModelService dataService;
    @Autowired private MessengerService messengerService;
    @Autowired private ResultService resultsService;
    @Autowired private SimulationController simulationController;
    
    @Value("${directory.property}") private String directoryProperty;
    @Value("${directory.storage.subfolder}") private String workingDirectory;
    @Value("${openmodelica.home.env}") private String openModelicaHomeDir;
    
    private final List<SimulationThread> threads = new ArrayList();
    
    private String compilerPath;
    private File dirWorking;

    public Simulation InitResultsStorage(ModelDao dataDao, String[] variables, References variableReferences) throws ResultsException {
        Simulation results = new Simulation(dataDao, variables, variableReferences);
        resultsService.add(results);
        return results;
    }

    public void StartSimulation() {
        
        /**
         * Monitor previous threads. 
         */
        List<SimulationThread> threadsFinished = new ArrayList();
        for (SimulationThread t : threads) {
            if (!t.isAlive()) {
                threadsFinished.add(t);
            }
        }
        threadsFinished.forEach(t -> {
            threads.remove(t);
        });

        Platform.runLater(() -> messengerService.printMessage("Starting simulation..."));
        SimulationThread thread = new SimulationThread();
        thread.start();
        threads.add(thread);
    }

    public void StopSimulation() {
        threads.stream()
                .filter((t) -> (t.isAlive()))
                .forEach(t -> {
                    System.out.println("Interrupting simulation thread!");
                    t.interrupt();
                });
    }
    
    /**
     * Gets the working directory. Used for storing and executing the compiled
     * sources.
     *
     * @return
     * @throws SimulationException
     */
    public File getSimulationWorkingDirectory() throws SimulationException {
        if (dirWorking == null) {
            dirWorking = this.getWorkingDirectory();
        }
        return dirWorking;
    }
    
    /**
     * Gets the path of the OMC compiler.
     *
     * @return
     * @throws SimulationException
     */
    public String getSimulationCompilerPath() throws SimulationException {
        if (compilerPath == null) {
            compilerPath = this.getCompilerPath();
        }
        return compilerPath;
    }
    
    private class SimulationThread extends Thread {
        
        private SimulationCompiler simulationCompiler;
        private SimulationServer simulationServer;
        private SimulationExecuter simulationExecuter;

        /**
         * Interrupts all related processes.
         */
        @Override
        public void interrupt() {
            if (simulationCompiler != null) {
                simulationCompiler.terminate();
            }
            if (simulationServer != null) {
                simulationServer.terminate();
            }
            if (simulationExecuter != null) {
                simulationExecuter.interrupt();
            }
            super.interrupt();
            System.out.println("Interrupted simulation!");
        }
        
        @Override
        public void run() {

            BufferedReader simulationExecuterOutputReader;
            ModelDao modelDao = dataService.getDao();
            double stopTime = simulationController.getSimulationStopTime();
            Throwable thr = null;
            
            try {
                /**
                 * Execute compiler.
                 */
                Platform.runLater(() -> {
                    simulationController.setSimulationProgress(-1);
                    messengerService.addMessage("Simulation: Initializing...");
                    messengerService.addMessage("Simulation: Building...");
                });
                simulationCompiler = new SimulationCompiler(SimulationService.this, modelDao, omExporter);
                simulationCompiler.setCompilerOptionalArgs(simulationController.getCompilerArgs());
                simulationCompiler.start();
                synchronized (simulationCompiler) {
                    try {
                        simulationCompiler.wait(); // wait for notification from compiler -> finished or failed
                    } catch (InterruptedException ex) {
                        throw new SimulationException("Simulation failed while waiting for compiler thread!", ex);
                    }
                }
                if (simulationCompiler.getException() != null) {
                    throw new SimulationException("Simulation build process failed!", simulationCompiler.getException());
                }
                
                

                /**
                 * Start server.
                 */
                Platform.runLater(() -> messengerService.addMessage("Simulation: Creating communication server socket..."));
                simulationServer = new SimulationServer();
                simulationServer.start();
                synchronized (simulationServer) {
                    try {
                        simulationServer.wait(); // wait for notification from server -> started or failed
                    } catch (InterruptedException ex) {
                        throw new SimulationException("Simulation failed while waiting for communication server!", ex);
                    }
                }
                if (!simulationServer.isRunning()) {
                    throw new SimulationException("Simulation communication server cannot be started!");
                }

                /**
                 * Start simulation.
                 */
                Platform.runLater(() -> messengerService.addMessage("Simulation: Executing..."));
                simulationExecuter = new SimulationExecuter(SimulationService.this, simulationCompiler, simulationServer);
                try {
                    simulationExecuter.setSimulationStopTime(simulationController.getSimulationStopTime());
                    simulationExecuter.setSimulationIntervals(simulationController.getSimulationIntervals());
                    simulationExecuter.setSimulationIntegrator(simulationController.getSimulationIntegrator());
                    simulationExecuter.setSimulationOptionalArgs(simulationController.getSimulationArgs());
                } catch (Exception ex) {
                    throw new SimulationException("Invalid simulation options!", ex);
                }
                simulationExecuter.start();
                synchronized (simulationExecuter) {
                    if (simulationExecuter.getSimulationOutputReader() == null) {
                        try {
                            simulationExecuter.wait(); // wait for notification from executer -> process started or failed
                        } catch (InterruptedException ex) {
                            throw new SimulationException("Executer got interrupted!", ex);
                        }
                    }
                    simulationExecuterOutputReader = simulationExecuter.getSimulationOutputReader();
                }
                if (simulationExecuter.isFailed()) {
                    throw new SimulationException("Executing simulation failed!", simulationExecuter.getException());
                }

                /**
                 * Initialize results data storage.
                 */
                synchronized (simulationServer) {
                    if (simulationServer.isRunning() && simulationServer.getSimulationVariables() == null) {
                        try {
                            simulationServer.wait(); // wait for notification from server that variables have been read (3)
                        } catch (InterruptedException ex) {
                            throw new SimulationException("Storage initialization failed!", ex);
                        }
                    }
                }
                if (!simulationServer.isRunning()) {
                    throw new SimulationException("Server was stopped!", simulationServer.getException());
                }

                Platform.runLater(() -> {
                    messengerService.addMessage("Simulation: Initializing results storage...");
                    Simulation results = null;
                    try {
                        results = InitResultsStorage(
                                modelDao,
                                simulationServer.getSimulationVariables(),
                                simulationCompiler.getSimulationReferences()
                        );
                        simulationServer.setResultsStorage(results);
                    } catch (ResultsException ex) {
                        if (results == null) {
                            simulationServer.terminate();
                            simulationExecuter.interrupt();
                        }
                    } finally {
                        synchronized (simulationServer) {
                            simulationServer.notify(); // notify server that storage has been initalized (4)
                        }
                    }
                });

                try {
                    synchronized (this) {
                        if (simulationServer.isRunning()) {
                            Platform.runLater(() -> messengerService.addMessage("Simulation: Reading and storing results..."));
                        }
                        while (simulationServer.isRunning()) {
                            double progress = simulationServer.getSimulationTime() / stopTime;
                            Platform.runLater(() -> simulationController.setSimulationProgress(progress));
                            this.wait(125);
                        }
                    }
                    simulationExecuter.join();
                    simulationExecuter = null;
                    simulationServer.join();
                    simulationServer = null;
                } catch (InterruptedException ex) {
                    throw new SimulationException("The simulation has been interrupted.");
                } finally {
                    Platform.runLater(() -> { // Simulation output.
                        if (simulationExecuterOutputReader != null) {
                            try {
                                messengerService.addMessage("Simulation: Output [START]");
                                String line;
                                while ((line = simulationExecuterOutputReader.readLine()) != null) {
                                    if (line.length() > 0) {
                                        messengerService.addMessage(line);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                messengerService.addMessage("Simulation: Output [END]");
                                try {
                                    simulationExecuterOutputReader.close();
                                } catch (IOException ex) {

                                }
                            }
                        }
                        try {
                            resultsService.UpdateAutoAddedData();
                        } catch (ResultsException ex) {
                            messengerService.addException("Failed to update auto added data series!", ex);
                        }
                    });
                }

            } catch (SimulationException ex) {
                
                thr = ex;
                Platform.runLater(() -> {
                    if (ex.getCause() != null) {
                        messengerService.addException("Simulation for '" + modelDao.getModelName() + "' stopped! ", ex.getCause());
                    } else {
                        messengerService.addException("Simulation for '" + modelDao.getModelName() + "' stopped! ", ex);
                    }
                });
                
            } finally {
                
                if (simulationExecuter != null) {
                    simulationExecuter.interrupt();
                }
                if (simulationServer != null) {
                    simulationServer.terminate();
                }
                
                final Throwable thrLambda = thr;
                Platform.runLater(() -> {
                    simulationController.setSimulationProgress(1);
                    if (thrLambda == null) {
                        messengerService.printMessage("Simulation: Finished!");
                        simulationController.setSimulationStatus("Finished!");
                    } else {
                        messengerService.printMessage("Simulation: Interrupted!");
                        simulationController.setSimulationStatus("Interrupted!");
                    }
                    simulationController.Unlock();
                });
            }
        }
    }

    private String getCompilerPath() throws SimulationException {

        String pathOpenModelica, pathCompiler;
        File dirOpenModelica;

        pathOpenModelica = System.getenv(openModelicaHomeDir);

        if (pathOpenModelica == null) {
            throw new SimulationException("'" + openModelicaHomeDir + "' environment variable is not set! Please install OpenModelica or set the variable correctly.");
        }

        dirOpenModelica = new File(pathOpenModelica);
        if (dirOpenModelica.exists() && dirOpenModelica.isDirectory()) {

            pathCompiler = pathOpenModelica + File.separator + "bin" + File.separator + "omc";
            if (Utility.isOsWindows()) {
                pathCompiler = pathCompiler + ".exe";
            } else if (Utility.isOsUnix()) {
                // TODO : OS is Linux
            } else if (Utility.isOsMac()) {
                // TODO : OS is Mac
            } else {
                // TODO : OS unknown
            }
            return pathCompiler;

        } else {
            throw new SimulationException("'" + openModelicaHomeDir + "' environment variable is not set correctly! Please set the variable correctly or reinstall OpenModelica.");
        }
    }

    public File getWorkingDirectory() throws SimulationException {

        File dir;
        String[] subDir;

        dir = new File(System.getProperty(directoryProperty));
        if (!dir.exists() || !dir.isDirectory()) {
            throw new SimulationException("Application's working directory not accessible!");
        }

        subDir = workingDirectory.split("/");

        for (String folder : subDir) {
            dir = new File(dir + File.separator + folder);
            if (!dir.exists() || !dir.isDirectory()) {
                dir.mkdir();
            }
        }

        return dir;
    }
}
