/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service.simulation;

import edu.unibi.agbi.gnius.core.service.exception.SimulationServiceException;
import edu.unibi.agbi.petrinet.model.References;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 *
 * @author PR
 */
public class SimulationExecuter extends Thread
{
    private final String simCompilerPath;
    private final String simExecutablePath;
    private final File simWorkingDirectory;
    private final int simServerPort;

    private boolean isFailed;
    private SimulationServiceException exception;

    private double simStopTime;
    private int simIntervals;
    private String simIntegrator;

    private Process simulationProcess;
    private BufferedReader simulationOutputReader;

    public SimulationExecuter(References simulationReferences, SimulationCompiler simulationCompiler, SimulationServer simulationServer) {
        simExecutablePath = simulationReferences.getSimulationExectuablePath();
        simCompilerPath = simulationCompiler.getCompilerPath();
        simWorkingDirectory = simulationCompiler.getSimulationWorkingDirectory();
        simServerPort = simulationServer.getSimulationServerPort();
    }

    public boolean isFailed() {
        return isFailed;
    }

    public SimulationServiceException getException() {
        return exception;
    }

    public void setSimulationStopTime(double simStopTime) {
        this.simStopTime = simStopTime;
    }

    public void setSimulationIntervals(int simIntervals) {
        this.simIntervals = simIntervals;
    }

    public void setSimulationIntegrator(String simIntegrator) {
        this.simIntegrator = simIntegrator;
    }

    public BufferedReader getSimulationOutputReader() {
        return simulationOutputReader;
    }

    @Override
    public void run() {

        ProcessBuilder pb;

        isFailed = false;
        simulationOutputReader = null;
        
        if (simStopTime == 0 || simIntervals == 0) {
            return;
        }

        String override = "-override=outputFormat=ia,stopTime=" + simStopTime + ",stepSize=" + simStopTime / simIntervals + ",tolerance=0.0001";

//                    if (flags.isParameterChanged()) {
//
//                        Iterator<Parameter> it = pw.getChangedParameters().keySet().iterator();
//                        GraphElementAbstract gea;
//                        Parameter param;
//
//                        while (it.hasNext()) {
//                            param = it.next();
//                            gea = pw.getChangedParameters().get(param);
//                            BiologicalNodeAbstract bna;
//                            if (gea instanceof BiologicalNodeAbstract) {
//                                bna = (BiologicalNodeAbstract)gea;
//                                override += ",'_" + bna.getName() + "_" + param.getName() + "'=" + param.getValue();
//                            }
//                        }
//                    }
//                    if (flags.isInitialValueChanged()) {
//                        Iterator<Place> it = pw.getChangedInitialValues().keySet().iterator();
//                        Place p;
//                        Double d;
//                        while (it.hasNext()) {
//                            p = it.next();
//                            d = pw.getChangedInitialValues().get(p);
//                            override += ",'" + p.getName() + "'.startMarks=" + d;
//                        }
//                    }
//                    if (flags.isBoundariesChanged()) {
//                        Iterator<Place> it = pw.getChangedBoundaries().keySet().iterator();
//                        Place p;
//                        Boundary b;
//                        while (it.hasNext()) {
//                            p = it.next();
//                            b = pw.getChangedBoundaries().get(p);
//                            if (b.isLowerBoundarySet()) {
//                                override += ",'" + p.getName() + "'.minMarks=" + b.getLowerBoundary();
//                            }
//                            if (b.isUpperBoundarySet()) {
//                                override += ",'" + p.getName() + "'.maxMarks=" + b.getUpperBoundary();
//                            }
//                        }
//                    }
        pb = new ProcessBuilder();
        pb.command(simExecutablePath, "-s=" + simIntegrator, override, "-port=" + simServerPort, "-lv=LOG_STATS");
//        pb.command(simExecutablePath, "-s=" + simIntegrator, override, "-port=" + simServerPort, "-noEventEmit", "-lv=LOG_STATS");
        pb.directory(simWorkingDirectory);
        pb.redirectOutput();

        Map<String, String> env = pb.environment();
        env.put("PATH", env.get("PATH") + ";" + simCompilerPath.substring(0, simCompilerPath.lastIndexOf(File.separator)));

//        System.out.println("Path: " + pb.environment().get("PATH"));
//        System.out.println("Override: " + override);
        try {
//            System.out.println("Starting simulation...");
            simulationProcess = pb.start();
            simulationOutputReader = new BufferedReader(new InputStreamReader(simulationProcess.getInputStream()));
        } catch (IOException ex) {
            isFailed = true;
            exception = new SimulationServiceException("Failed to start the simulation process!", ex);
        } finally {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }
    
    @Override
    public void interrupt() {
        if (simulationProcess != null) {
            simulationProcess.destroyForcibly();
        }
        super.interrupt();
    }
}
