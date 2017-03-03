/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.simulation;

import edu.unibi.agbi.gnius.business.controller.simulation.SimulationController;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.util.OS_Validator;
import edu.unibi.agbi.petrinet.util.OpenModelicaExport;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class OpenModelicaExecuter
{
    @Autowired private DataDao petriNetDao;
    @Autowired private OpenModelicaServer openModelicaServer;
    @Autowired private SimulationController simController;

    private final static String ENV_OM_HOME = "OPENMODELICAHOME";
    
    private File dataDirectory = null;

    private LocalDateTime ldtSimulationStart;
    private LocalDateTime ldtSimulationEnd;
    
    public boolean simulate() {
        
        final Process buildProcess, simulationProcess;
        final BufferedReader simulationOutput;
        ProcessBuilder pb;
        
        File dirStorage, dirWork , fileMo, fileMos;
        String pathCompiler, pathSimulation, nameSimulation;
        Double stopTime;
        int simIntervals;
        
        dirStorage = getStorageDirectory();
        if (dirStorage == null) {
            System.out.println("Application's storage directory not accessible!");
            return false;
        }
        
        dirWork = getWorkingDirectory();
        if (dirWork == null) {
            System.out.println("Application's working directory not accessible!");
            return false;
        }
        
        pathCompiler = getCompilerPath();
        if (pathCompiler == null) {
            System.out.println("OpenModelica compiler not found!");
            return false;
        }
        
        try {
            stopTime = simController.getStopTime();
        } catch (NumberFormatException ex) {
            System.out.println("The value given for 'Stop Time' is not a number!");
            return false;
        }
        
        try {
            simIntervals = simController.getIntervals();
        } catch (NumberFormatException ex) {
            System.out.println("The value given for 'Intervals' is not a natural number!");
            return false;
        }

        /**
         * Exporting data for OpenModelica.
         */
        nameSimulation = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).replace(":" , "").substring(0, 6) + "-" + petriNetDao.getName();
        try {
            System.out.println("Exporting data...");
            fileMo = new File(dirStorage + File.separator + nameSimulation + ".mo");
            fileMos = new File(dirStorage + File.separator + nameSimulation + ".mos");
            OpenModelicaExport.exportMO(petriNetDao , fileMo);
            OpenModelicaExport.exportMOS(petriNetDao , fileMos , fileMo , dirWork);
//            fileMos = new File("C:/Users/PR/AppData/Local/Temp/GraVisFX/data/example/simulation.mos");
        } catch (IOException ex) {
            // TODO
            System.out.println("Data export failed!");
            System.out.println(ex.toString());
            return false;
        }

        /**
         * Building simulation.
         */
        pb = new ProcessBuilder(pathCompiler , fileMos.getPath());
        pb.directory(dirWork);
        try {
            System.out.println("Building simulation...");
            buildProcess = pb.start();
        } catch (IOException ex) {
            System.out.println("Exception starting compilation process!");
            System.out.println(ex.toString());
            return false;
        }

        // Terminates process after 20 minutes.
        Thread compilerWatcherThread = new Thread()
        {
            @Override
            public void run() {
                long totalTime = 1200000;
                try {
                    for (long t = 0; t < totalTime; t += 1000) {
                        if (buildProcess.isAlive()) {
                            sleep(1000);
                        }
                    }
                    buildProcess.destroy();
                    // stopped = true;
                } catch (Exception e) {
                }
            }
        };
        compilerWatcherThread.start();
        
        // Wait for build process and interrupt waiting thread.
        try {
            buildProcess.waitFor();
            compilerWatcherThread.interrupt();
        } catch (InterruptedException ex) {
            System.out.println("Exception waiting for process to finish!");
            System.out.println(ex.toString());
            return false;
        }

        /**
         * Reading build process output.
         */
        InputStream os = buildProcess.getInputStream();
        try {
            byte[] bytes = new byte[os.available()];
            os.read(bytes);
            String buildOutput = new String(bytes);
            
            System.out.println(buildOutput);

            // Evaluating for warnings
            if (buildOutput.contains("Warning: ")) {
                String[] split = buildOutput.split("Warning: ");
                System.out.println("Compilation output contains warnings!");
                for (int i = 1; i < split.length; i++) {
                    System.out.println(i + ": " + split[i]);
                }
            }
            pathSimulation = parseSimulationExecutablePath(buildOutput);
            if (pathSimulation == null) {
                System.out.println("Simulation could not be built!");
                return false;
            }
        } catch (IOException ex) {
            System.out.println("Exception reading process output!");
            System.out.println(ex.toString());
            return false;
        }
        
        try {
            openModelicaServer.StartThread();
        } catch (IOException ex) {
            System.out.println("Exception starting server.");
            System.out.println(ex.toString());
            return false;
        }
        
//        pw.getPetriNet().setPetriNetSimulation(true);
//        w.initPCPGraphs();
        
        
        /**
         * Ausführen der Simulation.
         */
        boolean noEmmit = true;

        String override = "-override=outputFormat=ia,stopTime=" + stopTime + ",stepSize=" + stopTime / simIntervals + ",tolerance=0.0001";
        
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
//                    if (SystemUtils.IS_OS_WINDOWS) {
//                        override += "\"";
//                    }
            
        pb = new ProcessBuilder();
//        noEmmit = false;
        if (noEmmit) {
            pb.command(pathSimulation , "-s=" + simController.getIntegrator() , override , "-port=11111" , "-noEventEmit" , "-lv=LOG_STATS");
        } else {
            pb.command(pathSimulation , override , "-port=11111" );
        }
        pb.directory(dirWork);
        pb.redirectOutput();

        Map<String , String> env = pb.environment();
        
        pathCompiler = pathCompiler.substring(0 , pathCompiler.lastIndexOf(File.separator));
        env.put("PATH" , env.get("PATH") + ";" + pathCompiler);
        
        pathCompiler = pathCompiler.substring(0 , pathCompiler.lastIndexOf(File.separator));
        env.put("PATH" , env.get("PATH") + ";" + pathCompiler);

        System.out.println("Path: " + pb.environment().get("PATH"));
        System.out.println("Override: " + override);

        try {
            System.out.println("Starting simulation...");
            simulationProcess = pb.start();
            simulationOutput = new BufferedReader(new InputStreamReader(simulationProcess.getInputStream()));
        } catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            return false;
        }
        
        /**
         * Simulation Output. (?)
         */
//        try {
        Thread t3 = new Thread()
        {
            public void run() {
                // System.out.println("running");
                String line;
                if (simulationOutput != null) {
                    while (openModelicaServer.isRunning()) {
                        try {
                            line = simulationOutput.readLine();
                            if (line != null && line.length() > 0) {
                                System.out.println(line);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        System.out.println("Server stopped");
                        while ((line = simulationOutput.readLine()) != null && line.length() > 0) {
                            System.out.println(line);
                        }
                        simulationOutput.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // w.updatePCPView();
            }
        };
        t3.start();
        
//        simExePresent = false;
//        System.out.println(simName);
//        if (new File(simName).exists()) {
//            System.out.println("sim exists");
//            simExePresent = true;
//        }
//
//        if (simExePresent) {
//            t2.start();
//            t3.start();
//            t1.start();
//        } else {
//            // System.out.println("something wet wrong");
//            JOptionPane.showMessageDialog(MainWindowSingleton.getInstance() , "Something wet wrong! Simulation could not be built!");
//            this.stopAction();
//        }
//
//        zstNachher = System.currentTimeMillis();
//        System.out.println("Zeit benoetigt: " + ((zstNachher - zstVorher) / 1000) + " sec");
//        System.out.println("Zeit benoetigt: " + ((zstNachher - zstVorher)) + " millisec");
//
//    }
//    catch (Exception e
//
//    
//        ) {
//                e.printStackTrace();
//        JOptionPane.showMessageDialog(MainWindowSingleton.getInstance() , "Something went wrong. The model couldn't be simulated!" ,
//                                      "Error occured..." , JOptionPane.ERROR_MESSAGE);
//        w.unBlurrUI();
//        this.menue.stopped();
//        this.process.destroy();
//        return;
//    }
//
//    w.unBlurrUI ();
        
        /**
         * Zeichnen der Graphen.
         */
//                Thread t2 = new Thread()
//                {
//                    public void run() {
//                        pw.getGraph().getVisualizationViewer().requestFocus();
//                        w.redrawGraphs();
//                        // CHRIS sometimes nullpointer
//                        List<Double> v = pw.getPetriNet().getSimResController().get().getTime().getAll();
//                        // System.out.println("running");
//                        while (s.isRunning()) {
//                            w.redrawGraphs();
//
//                            // double time =
//                            if (v.size() > 0) {
//                                menue.setTime((v.get(v.size() - 1)).toString());
//                            }
//                            try {
//                                sleep(100);
//                            } catch (InterruptedException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                        }
//                        menue.stopped();
//                        System.out.println("end of simulation");
//                        w.updatePCPView();
//                        w.redrawGraphs();
//
//                        w.repaint();
//                        if (v.size() > 0) {
//                            menue.setTime((v.get(v.size() - 1)).toString());
//                        }
//                        // w.updatePCPView();
//                    }
//                };
        return true;
    }
    
    public void terminate() {
        openModelicaServer.StopThread();
    }
    
    /**
     * Gets the directory for storing files related to the simulation.
     * @return 
     */
    private File getStorageDirectory() {
        
        File dir;
        
        dir = new File(System.getProperty("java.io.tmpdir"));
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }
        
        dir = new File(dir + File.separator + "GraVisFX");
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        
        dir = new File(dir + File.separator + "data");
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        
        dir = new File(dir + File.separator + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        
        return dir;
    }
    
    /**
     * Gets the working directory. Used for storing compiled sources.
     * @return 
     */
    private File getWorkingDirectory() {
        
        if (dataDirectory == null) {

            File dir;

            dir = new File(System.getProperty("java.io.tmpdir"));
            if (!dir.exists() || !dir.isDirectory()) {
                return null;
            }

            dir = new File(dir + File.separator + "GraVisFX");
            if (!dir.exists() || !dir.isDirectory()) {
                dir.mkdir();
            }

            dir = new File(dir + File.separator + "data");
            if (!dir.exists() || !dir.isDirectory()) {
                dir.mkdir();
            }
            
            dataDirectory = dir;
        }
        
        return dataDirectory;
    }
    
    /**
     * Gets the path of the OMC compiler.
     * @return 
     */
    private String getCompilerPath() {
        
        String pathOpenModelica, pathCompiler;
        File dirOpenModelica;

        pathOpenModelica = System.getenv(ENV_OM_HOME);

        if (pathOpenModelica != null) {

            dirOpenModelica = new File(pathOpenModelica);
            if (dirOpenModelica.exists() && dirOpenModelica.isDirectory()) {

                pathCompiler = pathOpenModelica + "bin" + File.separator + "omc";
                if (OS_Validator.isOsWindows()) {
                    pathCompiler = pathCompiler + ".exe";
                } else if (OS_Validator.isOsUnix()) {
                    // TODO : OS is Linux
                } else if (OS_Validator.isOsMac()) {
                    // TODO : OS is Mac
                } else {
                    // TODO : OS not supported
                }
                return pathCompiler;

            } else {
                // ENV is present, but directory does not exist: check if om is installed and ENV variable is set properly 
            }
        } else {
            // ENV is not present: check if om is installed and ENV variable is set properly
        }
        return null;
    }
    
    /**
     * Parses name of the simulation executable. Parses the String generated
     * by the compiler to output the name of the executable simulation file.
     * @param output
     * @return 
     */
    private String parseSimulationExecutablePath(String output) {
        
        output = output.substring(output.lastIndexOf("{"));
        output = parseSubstring(output, "\"", "\"");
        
        if (output == null) {
            return null;
        }
        
        if (OS_Validator.isOsWindows()) {
            output += ".exe";
        }
        
        return output;
    }
        
    /**
     * Parses a substring. Parses a subject String from a given starting String 
     * to a given ending String. Returns NULL if the String cannot be parsed.
     * @param subject
     * @param start
     * @param end
     * @return 
     */
    public static String parseSubstring(String subject, String start, String end){
        
        if (subject == null)
            return null;
        
        int s = subject.indexOf(start) + start.length();
        int e = subject.indexOf(end, s);
        
        if (s >= e)
            return null;
        
        return subject.substring(s, e);
    }
}
