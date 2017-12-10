/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.business.service.simulation;

import edu.unibi.agbi.editor.business.exception.SimulationException;
import edu.unibi.agbi.editor.business.service.SimulationService;
import edu.unibi.agbi.editor.core.data.dao.ModelDao;
import edu.unibi.agbi.editor.core.util.Utility;
import edu.unibi.agbi.petrinet.model.References;
import edu.unibi.agbi.petrinet.util.OpenModelicaExporter;
import edu.unibi.agbi.petrinet.util.ParameterFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author PR
 */
public final class SimulationCompiler extends Thread
{
    private final SimulationService simulationService;
    private final ModelDao modelDao;
    private final OpenModelicaExporter omExporter;
    private final ParameterFactory parameterFactory;
    
    private String simOptionalArcs;
    private Process buildProcess;
    private References simReferences;
    private SimulationException simException;
    
    public SimulationCompiler(SimulationService simulationService, ModelDao modelDao, OpenModelicaExporter omExporter, ParameterFactory parameterFactory) {
        this.simulationService = simulationService;
        this.modelDao = modelDao;
        this.omExporter = omExporter;
        this.parameterFactory = parameterFactory;
    }

    public void setCompilerOptionalArgs(String optionalArgs) {
        this.simOptionalArcs = optionalArgs;
    }
    
    public References getSimulationReferences() {
        return simReferences;
    }
    
    public SimulationException getException() {
        return simException;
    }

    public void terminate() {
        buildProcess.destroyForcibly();
        this.interrupt();
    }

    @Override
    public void run() {

        ProcessBuilder pb;
        File dirStorage, dirWorking, fileMo, fileMos;
        String compilerPath;
        References simulationReferences;


        /**
         * Gets the required directories. Cleans the working directory
         * from old files.
         */
        try {
            compilerPath = simulationService.getSimulationCompilerPath();
            dirWorking = simulationService.getSimulationWorkingDirectory();
            dirStorage = createStorageDirectory(dirWorking);
            
            for (File file : dirWorking.listFiles()) {
                if (!file.isDirectory()) {
                    if ((System.currentTimeMillis() - file.lastModified()) // File older than 5 minutes
                            > 300000L) {
                        System.out.println("Deleting: " + file.getName());
                        file.delete();
                    }
                }
            }
        } catch (SimulationException ex) {
            simException = new SimulationException("Failed to get the required directories! [" + ex.getMessage() + "]");
            return;
        }

        /**
         * Exort data for OpenModelica.
         */
        try {
            fileMo = new File(dirStorage + File.separator + "model.mo");
            fileMos = new File(dirStorage + File.separator + "model.mos");
            omExporter.exportMO(modelDao.getModelName(), modelDao.getModel(), fileMo, parameterFactory);
            simulationReferences = omExporter.exportMOS(modelDao.getModelName(), modelDao.getModel(), fileMos, fileMo, dirWorking);
        } catch (IOException ex) {
            simException = new SimulationException("Failed to export the data for OpenModelica! [" + ex.getMessage() + "]");
            return;
        }

        /**
         * Build the simulation.
         */
        List<String> cmdLineArgs = new ArrayList();
        cmdLineArgs.add(compilerPath);
        cmdLineArgs.add(fileMos.getPath());
        if (simOptionalArcs != null) {
            cmdLineArgs.addAll(Arrays.asList(simOptionalArcs.split(" ")));
        }
        
        pb = new ProcessBuilder();
        pb.command(cmdLineArgs);
        pb.directory(dirWorking);
//        pb.redirectOutput();
//        pb.redirectErrorStream(true); // merge stdout, stderr of process

        try {
            buildProcess = pb.start();
        } catch (IOException ex) {
            simException = new SimulationException("Failed to start the build process! [" + ex.getMessage() + "]");
            return;
        }

        /**
         * Drain process streams.
         * InputStream and ErrorStream have to be drained, as otherwise the
         * Process can get stuck.
         * https://stackoverflow.com/questions/10365402/java-process-invoked-by-processbuilder-sleeps-forever
         */
        String output;
        try {
            output = DrainStreams(buildProcess);
        } catch (IOException ex) {
            simException = new SimulationException("Build process was interrupted!");
            return;
        }

        /**
         * Wait for the build process to finish. 
         */
        try {
            buildProcess.waitFor();
        } catch (InterruptedException ex) {
            simException = new SimulationException("Build process was interrupted!");
            return;
        }
        
        /**
         * Read the build process output and 
         * parse the path to the executable.
         */
        try {
            output = parseSimulationExecutablePath(output);
        } catch (SimulationException ex) {
            simException = ex;
            return;
        }
        simulationReferences.setSimulationExecutablePath(output);

        simReferences = simulationReferences;
    }
    
    private String DrainStreams(Process process) throws IOException {
        
        InputStreamReader isr = new InputStreamReader(process.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        
        String in, output = "", error = "";
        byte[] bytes;

        while ((in = br.readLine()) != null) {
            // swallow the line, or print it out - System.out.println(lineRead);
            output += in + "\n";
            bytes = new byte[process.getErrorStream().available()];
            if (bytes.length != 0) {
                error += new String(bytes);
            }
        }

        
        System.out.println("ErrorStream:");
        System.out.println(error);
        
        System.out.println("InputStream:");
        System.out.println(output);
        
        return output;
    }

    /**
     * Gets the directory for storing data that is used for building the
     * simulation.
     *
     * @return
     */
    private File createStorageDirectory(File workingDirectory) throws SimulationException {

        File dir;

        dir = new File(workingDirectory + File.separator + "omc");
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }

        return dir;
    }

    /**
     * Parses name of the simulation executable. Parses the String generated by
     * the compiler to output the name of the executable simulation file.
     *
     * @param output
     * @return
     */
    private String parseSimulationExecutablePath(final String output) throws SimulationException {

        String path;

        try {
            path = output.substring(output.lastIndexOf("{"));
            path = Utility.parseSubstring(path, "\"", "\"");
        } catch (Exception ex) {
            throw new SimulationException("Path to simulation executable can not be parsed. Output: \n" + output);
        }

        if (path == null) {
            throw new SimulationException("Build failed. Output: \n" + output);
        }

        if (Utility.isOsWindows()) {
            path += ".exe";
        }
        
        System.out.println("Sim Executable: " + path);

        return path;
    }
}
