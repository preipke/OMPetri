/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service.simulation;

import edu.unibi.agbi.gnius.core.exception.SimulationServiceException;
import edu.unibi.agbi.gnius.util.Utility;
import edu.unibi.agbi.petrinet.model.PetriNet;
import edu.unibi.agbi.petrinet.model.References;
import edu.unibi.agbi.petrinet.util.OpenModelicaExporter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public final class SimulationCompiler
{
    @Autowired private OpenModelicaExporter openModelicaExporter;

    @Value("${directory.property}") private String directoryProperty;
    @Value("${directory.storage.subfolder}") private String workingDirectory;
    @Value("${openmodelica.home.env}") private String openModelicaHomeDir;

    private String simCompilerPath;
    private File simWorkingDirectory;

    public String getCompilerPath() {
        return simCompilerPath;
    }

    public File getSimulationWorkingDirectory() {
        return simWorkingDirectory;
    }

    /**
     * Builds a new simulation using the OpenModelica compiler.
     *
     * @param model
     * @return String representing the path to the simulation executable
     * @throws SimulationServiceException
     */
    public References compile(PetriNet model) throws SimulationServiceException {

        final Process process;
        ProcessBuilder pb;

        File dirStorage, fileMo, fileMos;
        References simulationReferences;

        /**
         * Get the required directories.
         */
        try {
            if (simCompilerPath == null) {
                simCompilerPath = getOpenModelicaCompilerPath();
            }
            if (simWorkingDirectory == null) {
                simWorkingDirectory = getWorkingDirectory();
            }
            dirStorage = createStorageDirectory(simWorkingDirectory);
        } catch (SimulationServiceException ex) {
            throw new SimulationServiceException("Failed to get the required directories! [" + ex.getMessage() + "]");
        }

        /**
         * Exort data for OpenModelica.
         */
        try {
            fileMo = new File(dirStorage + File.separator + "model.mo");
            fileMos = new File(dirStorage + File.separator + "model.mos");
            openModelicaExporter.exportMO(model, fileMo);
            simulationReferences = openModelicaExporter.exportMOS(model, fileMos, fileMo, simWorkingDirectory);
        } catch (IOException ex) {
            throw new SimulationServiceException("Failed to export the data for OpenModelica! [" + ex.getMessage() + "]");
        }

        /**
         * Build the simulation.
         */
        pb = new ProcessBuilder(simCompilerPath, fileMos.getPath());
        pb.directory(simWorkingDirectory);
        try {
            process = pb.start();
        } catch (IOException ex) {
            throw new SimulationServiceException("Failed to start the build process! [" + ex.getMessage() + "]");
        }

        /**
         * Wait for the build process to finish.
         */
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            throw new SimulationServiceException("Failed to wait for the build process! [" + ex.getMessage() + "]");
        }

        /**
         * Read the build process output and parse the path to the executable.
         */
        String output;
        output = parseInput(process.getInputStream());
        output = parseSimulationExecutablePath(output);
        simulationReferences.setSimulationExecutablePath(output);

        return simulationReferences;
    }

    /**
     * Gets the working directory. Used for storing and executing the compiled
     * sources.
     *
     * @return
     * @throws SimulationServiceException
     */
    private File getWorkingDirectory() throws SimulationServiceException {

        File dir;
        String[] subDir;

        dir = new File(System.getProperty(directoryProperty));
        if (!dir.exists() || !dir.isDirectory()) {
            throw new SimulationServiceException("Application's working directory not accessible!");
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

    /**
     * Gets the directory for storing data that is used for building the
     * simulation.
     *
     * @return
     */
    private File createStorageDirectory(File workingDirectory) throws SimulationServiceException {

        File dir;

        dir = new File(workingDirectory + File.separator + "omc");
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }

        return dir;
    }

    /**
     * Gets the path of the OMC compiler.
     *
     * @return
     * @throws SimulationServiceException
     */
    private String getOpenModelicaCompilerPath() throws SimulationServiceException {

        String pathOpenModelica, pathCompiler;
        File dirOpenModelica;

        pathOpenModelica = System.getenv(openModelicaHomeDir);

        if (pathOpenModelica == null) {
            throw new SimulationServiceException("'" + openModelicaHomeDir + "' environment variable is not set! Please install OpenModelica or set the variable correctly.");
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
                // TODO : OS not maybe supported
            }
            return pathCompiler;

        } else {
            throw new SimulationServiceException("'" + openModelicaHomeDir + "' environment variable is not set correctly! Please set the variable correctly or reinstall OpenModelica.");
        }
    }

    /**
     * Parses data generated from an input stream to a string.
     *
     * @param input
     * @return
     * @throws SimulationServiceException
     */
    private String parseInput(InputStream input) throws SimulationServiceException {

        byte[] bytes;
        String output;

        try {
            bytes = new byte[input.available()];
            input.read(bytes);
            output = new String(bytes);
        } catch (IOException ex) {
            throw new SimulationServiceException("Exception reading the simulation build process output! [" + ex.getMessage() + "]");
        }

        return output;
    }

    /**
     * Parses name of the simulation executable. Parses the String generated by
     * the compiler to output the name of the executable simulation file.
     *
     * @param output
     * @return
     */
    private String parseSimulationExecutablePath(final String output) throws SimulationServiceException {

        String path;
        
        try {
            path = output.substring(output.lastIndexOf("{"));
            path = Utility.parseSubstring(path, "\"", "\"");
        } catch (Exception ex) {
            throw new SimulationServiceException("Path to simulation executable can not be parsed. Output: \n" + output);
        }

        if (path == null) {
            throw new SimulationServiceException("Build failed. Output: \n" + output);
        }

        if (Utility.isOsWindows()) {
            path += ".exe";
        }

        return path;
    }
}
