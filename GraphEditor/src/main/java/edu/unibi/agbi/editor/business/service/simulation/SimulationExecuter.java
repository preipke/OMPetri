/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.business.service.simulation;

import edu.unibi.agbi.editor.business.exception.SimulationException;
import edu.unibi.agbi.editor.business.service.SimulationService;
import edu.unibi.agbi.petrinet.model.References;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private SimulationException exception;

    private double simStopTime;
    private int simIntervals;
    private String simIntegrator;
    private String simOptionalArcs;

    private Process simulationProcess;
    private BufferedReader simulationOutputReader;

    public SimulationExecuter(SimulationService simulationService, SimulationCompiler simulationCompiler, SimulationServer simulationServer) throws SimulationException {
        simCompilerPath = simulationService.getSimulationCompilerPath();
        simWorkingDirectory = simulationService.getSimulationWorkingDirectory();
        simExecutablePath = simulationCompiler.getSimulationReferences().getSimulationExectuablePath();
        simServerPort = simulationServer.getSimulationServerPort();
    }

    public boolean isFailed() {
        return isFailed;
    }

    public SimulationException getException() {
        return exception;
    }

    public void setSimulationStopTime(double stopTime) {
        this.simStopTime = stopTime;
    }

    public void setSimulationIntervals(int intervals) {
        this.simIntervals = intervals;
    }

    public void setSimulationIntegrator(String integrator) {
        this.simIntegrator = integrator;
    }

    public void setSimulationOptionalArgs(String optionalArgs) {
        this.simOptionalArcs = optionalArgs;
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
        
        List<String> cmdLineArgs = new ArrayList();
        cmdLineArgs.add(simExecutablePath);
        cmdLineArgs.add("-s=" + simIntegrator);
        cmdLineArgs.add(override);
        cmdLineArgs.add("-port=" + simServerPort);
//        cmdLineArgs.add("-noEventEmit");
        cmdLineArgs.add("-lv=LOG_STATS");
        
        if (simOptionalArcs != null) {
            cmdLineArgs.addAll(Arrays.asList(simOptionalArcs.split(" ")));
        }
        

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
        pb.command(cmdLineArgs);
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
            exception = new SimulationException("Failed to start the simulation process!", ex);
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
