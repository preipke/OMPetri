/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.business.controller.editor.modelpanel.SimulationController;
import edu.unibi.agbi.gnius.core.exception.ResultsServiceException;
import edu.unibi.agbi.gnius.core.model.dao.ResultsDao;
import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.exception.SimulationServiceException;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.service.simulation.SimulationCompiler;
import edu.unibi.agbi.gnius.core.service.simulation.SimulationExecuter;
import edu.unibi.agbi.gnius.core.service.simulation.SimulationServer;
import edu.unibi.agbi.petrinet.model.Model;
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
    @Autowired private ResultsService resultsService;
    @Autowired private SimulationController simulationControlsController;
    @Autowired private SimulationCompiler simulationCompiler;
    

    @Autowired
    public SimulationService(ResultsDao resultsDao) {
        this.resultsDao = resultsDao;
        this.threads = new ArrayList();
    }

    public Simulation InitSimulation(DataDao dataDao, String[] variables, References variableReferences) {
        Simulation simulation = new Simulation(dataDao, variables, variableReferences);
        resultsDao.add(simulation);
        return simulation;
    }

    public void StartSimulation() {
        
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

        Platform.runLater(() -> {
            messengerService.printMessage("Starting simulation...");
        });
        Thread thread = new Thread(() -> {

            DataDao data = dataService.getActiveDao();
            
            try {
                /**
                 * Execute compiler.
                 */
                References simulationReferences;
                try {
                    Platform.runLater(() -> {
                        simulationControlsController.setSimulationProgress(-1);
                    });
                    simulationReferences = simulationCompiler.compile(data);
                } catch (SimulationServiceException ex) {
                    throw new SimulationServiceException("Simulation compilation failed!", ex);
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
                        throw new SimulationServiceException("Simulation failed while waiting for server start!", ex);
                    }
                }
                if (!simulationServer.isRunning()) {
                    if (simulationServer.isFailed()) {
                        throw new SimulationServiceException("Simulation failed while starting the server!", simulationServer.getException());
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
                            throw new SimulationServiceException("Executer got interrupted!", ex);
                        }
                    }
                    simulationExecuterOutputReader = simulationExecuter.getSimulationOutputReader();
                }
                if (simulationExecuter.isFailed()) {
                    throw new SimulationServiceException("Executing simulation failed!", simulationExecuter.getException());
                }

                /**
                 * Initialize simulation data storage.
                 */
                synchronized (simulationServer) {
                    if (simulationServer.isRunning() && simulationServer.getSimulationVariables() == null) {
                        try {
                            simulationServer.wait(); // wait for notification from server -> variables initialized or failed
                        } catch (InterruptedException ex) {
                            throw new SimulationServiceException("Storage initialization failed!", ex);
                        }
                    }
                }
                if (!simulationServer.isRunning()) {
                    if (simulationServer.isFailed()) {
                        throw new SimulationServiceException("Server failed!", simulationServer.getException());
                    } else {
                        throw new SimulationServiceException("Server is not running!");
                    }
                }

                Platform.runLater(() -> {
                    Simulation simulation = InitSimulation(
                            data,
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
                                simulationControlsController.setSimulationProgress(
                                                simulationServer.getSimulationIterationCount() / simulationControlsController.getSimulationIntervals()
                                );
                            });
                            this.wait(250);
                        }
                    }
                    simulationExecuter.join();
                    simulationServer.join();
                } catch (InterruptedException ex) {
                    throw new SimulationServiceException("The simulation has been interrupted.");
                }
                
                Platform.runLater(() -> {
                    
                    // Simulation output.
                    if (simulationExecuterOutputReader != null) {
                        try {
                            messengerService.addMessage("--- Simulation output ---");
                            String line;
                            while ((line = simulationExecuterOutputReader.readLine()) != null) {
                                if (line.length() > 0) {
                                    messengerService.addMessage(line);
                                }
                            }
                            simulationExecuterOutputReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    messengerService.printMessage("Simulating '" + data.getModelName() + "' finished!");
                    try {
                        resultsService.UpdateAutoAddedData();
                    } catch (ResultsServiceException ex) {
                        messengerService.addException("Failed to update auto added data series!", ex);
                    }
                });
            } catch (SimulationServiceException ex) {
                Platform.runLater(() -> {
                    messengerService.printMessage("Simulation for '" + data.getModelName() + "' stopped!");
                    if (ex.getCause() != null) {
                        messengerService.addException("Simulation for '" + data.getModelName() + "' stopped! ", ex.getCause());
                    } else {
                        messengerService.addException("Simulation for '" + data.getModelName() + "' stopped! ", ex);
                    }
                });
            } finally {
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
        threads.stream()
                .filter((t) -> (t.isAlive()))
                .forEach(t -> {
                    System.out.println("Interrupting simulation thread!");
                    t.interrupt();
                });
    }
}
