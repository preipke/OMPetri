/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.business.controller.ElementController;
import edu.unibi.agbi.gnius.business.controller.SimulationController;
import edu.unibi.agbi.gnius.core.model.dao.ResultsDao;
import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.core.exception.SimulationServiceException;
import edu.unibi.agbi.gnius.core.service.simulation.SimulationCompiler;
import edu.unibi.agbi.gnius.core.service.simulation.SimulationExecuter;
import edu.unibi.agbi.gnius.core.service.simulation.SimulationServer;
import edu.unibi.agbi.petrinet.model.References;
import java.io.BufferedReader;
import java.io.IOException;
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

    @Autowired private DataGraphService dataService;
    @Autowired private MessengerService messengerService;
    @Autowired private SimulationController simulationControlsController;
    @Autowired private ElementController elementController;
    @Autowired private SimulationCompiler simulationCompiler;

    @Autowired
    public SimulationService(ResultsDao resultsDao) {
        this.resultsDao = resultsDao;
    }

    public Simulation InitSimulation(String authorName, String modelName, String[] variables, References variableReferences) {
        Simulation simulation = new Simulation(authorName, modelName, variables, variableReferences);
        resultsDao.add(simulation);
        return simulation;
    }

    public void StartSimulation() throws SimulationServiceException {

        try {
            elementController.StoreElementDetails();
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
                References simulationReferences;
                try {
                    Platform.runLater(() -> {
                        simulationControlsController.setSimulationProgress(-1);
                    });
                    simulationReferences = simulationCompiler.compile(dataService.getDataDao());
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
                    Simulation simulation = InitSimulation(dataService.getDataDao().getAuthor(),
                            dataService.getDataDao().getName(),
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
                 * Read simulation output. (?)
                 */
                Thread t3 = new Thread()
                {
                    public void run() {
                        String line;
                        if (simulationExecuterOutputReader != null) {
//                            while (server.isRunning()) {
//                                try {
//                                    line = simulationExecuterOutputReader.readLine();
//                                    if (line != null && line.length() > 0) {
//                                        System.out.println(line);
//                                    }
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                                try {
//                                    sleep(100);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
                            try {
                                System.out.println("--- Server output ---");
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
                    }
                };
                t3.start();
                
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
    }

    public void StopSimulation() {
        // if !isSimulating -> return
    }
}
