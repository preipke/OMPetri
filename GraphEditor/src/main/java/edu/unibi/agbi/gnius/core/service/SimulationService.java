/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.business.controller.editor.element.ElementController;
import edu.unibi.agbi.gnius.business.controller.editor.model.SimulationController;
import edu.unibi.agbi.gnius.core.model.dao.ResultsDao;
import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.exception.SimulationServiceException;
import edu.unibi.agbi.gnius.core.service.simulation.SimulationCompiler;
import edu.unibi.agbi.gnius.core.service.simulation.SimulationExecuter;
import edu.unibi.agbi.gnius.core.service.simulation.SimulationServer;
import edu.unibi.agbi.petrinet.model.References;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class SimulationService
{
    private final ResultsDao resultsDao;
    private final List<Thread> threads;

    @Autowired private DataService dataService;
    @Autowired private MessengerService messengerService;
    @Autowired private SimulationController simulationControlsController;
    @Autowired private ElementController elementController;
    @Autowired private SimulationCompiler simulationCompiler;
    

    @Autowired
    public SimulationService(ResultsDao resultsDao) {
        this.resultsDao = resultsDao;
        this.threads = new ArrayList();
    }

    public Simulation InitSimulation(String authorName, String modelName, String[] variables, References variableReferences) {
        Simulation simulation = new Simulation(authorName, modelName, variables, variableReferences);
        resultsDao.add(simulation);
        return simulation;
    }

    public void StartSimulation() throws SimulationServiceException {
        
        /**
         * Monitor previous threads. 
         */
        List<Thread> threadsFinished = new ArrayList();
        for (Thread t : threads) {
            if (!t.isAlive()) {
                threadsFinished.add(t);
            }
        }
        threadsFinished.forEach(t -> {
            threads.remove(t);
        });

        messengerService.addToLog("Starting simulation...");
        Thread thread = new Thread(() -> {

            try {
                /**
                 * Execute compiler.
                 */
                References simulationReferences;
                try {
                    Platform.runLater(() -> {
                        simulationControlsController.setSimulationProgress(-1);
                    });
                    simulationReferences = simulationCompiler.compile(dataService.getActiveModel());
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
                BufferedReader simulationExecuterOutputReader;
                SimulationExecuter simulationExecuter = new SimulationExecuter(simulationReferences, simulationCompiler, simulationServer);
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
                    simulationExecuterOutputReader = simulationExecuter.getSimulationOutputReader();
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
                    Simulation simulation = InitSimulation(dataService.getActiveModel().getAuthor(),
                            dataService.getActiveModel().getName(),
                            simulationServer.getSimulationVariables(),
                            simulationReferences
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

                /**
                 * Read simulation output.
                 */
                if (simulationExecuterOutputReader != null) {
                    try {
                        System.out.println("--- Server output ---");
                        String line;
                        while ((line = simulationExecuterOutputReader.readLine()) != null) {
                            if (line.length() > 0) {
                                System.out.println(line);
                            }
                        }
                        simulationExecuterOutputReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
            } catch (SimulationServiceException ex) {
                messengerService.addToLog(ex.getMessage());
            } finally {
                messengerService.addToLog("Finished simulating!");
                Platform.runLater(() -> {
                    simulationControlsController.setSimulationProgress(1);
                    simulationControlsController.Unlock();
                });
            }
        });
        thread.start();
        threads.add(thread);
    }

    public void StopSimulation() {
        threads.stream().filter((t) -> (t.isAlive())).forEach(t -> {
            System.out.println("Interrupting thread!");
            t.interrupt();
        });
    }
}
