/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.simulation;

import edu.unibi.agbi.gnius.core.dao.PetriNetDao;
import edu.unibi.agbi.gnius.core.service.ResultsService;
import edu.unibi.agbi.gnius.util.OS_Validator;
import edu.unibi.agbi.petrinet.entity.IPN_Arc;
import edu.unibi.agbi.petrinet.entity.IPN_Node;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class OpenModelicaServer
{
    @Autowired private ResultsService resultsService;
    @Autowired private PetriNetDao petriNetDao;
    
    private final int SIZE_OF_INT; // size of modelica int;
    private final int SERVER_PORT = 11111;
    
    private boolean isTerminated = false;
    private boolean isRunning = true;
    
    private ServerSocket serverSocket;
    
    private ArrayList<String> nodeNames;
    private HashMap<String , Integer> nodeIndicesForNames;

    public OpenModelicaServer() {
        if (OS_Validator.isOsWindows()) {
            SIZE_OF_INT = 4;
        } else {
            SIZE_OF_INT = 8;
        }
    }
    
    /**
     * Starts the server thread. Waits for the thread to start before returning.
     * @throws IOException 
     */
    public void StartThread() throws IOException {
        
        final Thread serverThread;
        
        if (serverSocket != null) {
            System.out.println("Server is still running! Please wait and try again later...");
            return;
        }
        
        serverThread = new Thread(() -> {
            
            Socket client;
            DataInputStream clientInput;
            
            try {
                isTerminated = false;
                serverSocket = new java.net.ServerSocket(SERVER_PORT);
                synchronized (this) {
                    this.notify();
                }
                while (true) {
                    client = serverSocket.accept();
                    System.out.println("Client connected!");
                    clientInput = new DataInputStream(client.getInputStream());
                    ReadData(clientInput);
                }
            } catch (IOException ex) {
                if (isTerminated) {
                    System.out.println("Server terminated!");
                } else {
                    System.out.println("Exception while processing client request!");
                    System.out.println(ex.toString());
                }
            } finally {
                serverSocket = null;
            }
        });
        serverThread.start();
        
        
        synchronized (this) {
            try {
                System.out.println("Waiting for server to start...");
                this.wait();
                System.out.println("Server started!");
            } catch (InterruptedException ex) {
                System.out.println("Exception while waiting for server start!");
                System.out.println(ex);
            }
        }
    }
    
    /**
     * Stops the server thread. Closes the server socket.
     */
    public void StopThread() {
        System.out.println("Stopping server...");
        try {
            serverSocket.close();
        } catch (IOException ex) {
            // TODO
            System.out.println(ex.toString());
        } finally {
            isRunning = false;
        }
    }
    
    /**
     * Reads data from the given input stream.
     * @param inputStream
     * @throws IOException 
     */
    private void ReadData(DataInputStream inputStream) throws IOException {
        
        ArrayList<Object> values;
        String[] sValues;
        String names;
        int reals, ints, bools, expected, length, id;
        byte btmp;
        
        int lengthMax = 2048;
        byte[] buffer = new byte[lengthMax];
        
//        inputStream.readFully(buffer , 0 , 1); // blockiert bis Nachricht empfangen
        inputStream.readFully(buffer , 0 , 4);

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        length = byteBuffer.getInt();
        if (lengthMax < length) {
            buffer = new byte[length];
        }

        inputStream.readFully(buffer , 0 , length); // blocks until msg received
        
        byteBuffer = ByteBuffer.wrap(buffer , 0 , 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        reals = byteBuffer.getInt();
        byteBuffer = ByteBuffer.wrap(buffer , 4 , 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        ints = byteBuffer.getInt();

        byteBuffer = ByteBuffer.wrap(buffer , 8 , 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        bools = byteBuffer.getInt();

        expected = reals * 8 + ints * SIZE_OF_INT + bools;

        byteBuffer = ByteBuffer.wrap(buffer , 12 , 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        names = new String(buffer , 16 , buffer.length - 17);
        nodeNames = new ArrayList(Arrays.asList(names.split("\u0000")));
        
        System.out.println(">> START: INCOMING NODE NAMES:");
        for (String name : nodeNames) {
            System.out.println(name);
        }
        System.out.println("<< END: INCOMING NODE NAMES:");
        
        nodeIndicesForNames = new HashMap(); // to avoid calls of names.indexOf(identifier)
        for (int i = 0; i < nodeNames.size(); i++) {
            nodeIndicesForNames.put(nodeNames.get(i) , i);
        }

        try {
            while (isRunning) {
                
                values = new ArrayList();

                inputStream.readFully(buffer , 0 , 5);
                id = (int)buffer[0];

                byteBuffer = ByteBuffer.wrap(Arrays.copyOfRange(buffer , 1 , buffer.length - 2));
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                length = byteBuffer.getInt();

                switch (id) {
                    case 4:
                        if (length > 0) {
                            inputStream.readFully(buffer , 0 , length);

                            for (int r = 0; r < reals; r++) {
                                byteBuffer = ByteBuffer.wrap(buffer , r * 8 , 8);
                                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                                values.add(byteBuffer.getDouble());
                            }
                            for (int i = 0; i < ints; i++) {
                                byteBuffer = ByteBuffer.wrap(buffer , reals * 8 + i * SIZE_OF_INT , SIZE_OF_INT);
                                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                                values.add(byteBuffer.getInt());
                            }
                            for (int b = 0; b < bools; b++) {
                                btmp = buffer[reals * 8 + ints * SIZE_OF_INT + b];
                                values.add((double)btmp);
                            }
                            
                            byteBuffer = ByteBuffer.wrap(buffer , expected , length - expected);
                            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

                            sValues = (new String(buffer , expected , length - expected)).split("\u0000");
                            for (int i = 0; i < sValues.length; i++) {
                                values.add(sValues[i]);
                            }
                            setData(values);
                        }
                        break;

                    case 6:
                        // TODO
                        System.out.println("Server shutting down...");
                        StopThread();
                        break;
                }
                
                try {
                    Thread.sleep(10); // sleep thread to save resources
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted while sleeping!");
                    System.out.println(e);
                }
            }
        } catch (SocketException e) {
            // TODO
        } finally {
            StopThread();
        }
    }

    private void setData(ArrayList<Object> values) {
        
        Collection<IPN_Node> places = petriNetDao.getPlaces();
        Collection<IPN_Node> transitions = petriNetDao.getTransitions();
        Collection<IPN_Arc> arcs = petriNetDao.getArcs();
        
        double value;
        
        for (IPN_Arc arc : arcs) {
            
            // token flow
            value = (Double)values.get(arc.getExportIndex());
            resultsService.addResult(arc.getId(), value);

            // gesamt
//            this.simResult.addValue(e , SimulationResultController.SIM_SUM_OF_TOKEN , value);
        }
        
        for (IPN_Node place : places) {
            
            // token
            value = (Double) values.get(place.getExportIndex());
            resultsService.addResult(place.getId(), value);
        }
        
        for (IPN_Node transition : transitions) {
            
            // speed
            value = (Double) values.get(transition.getExportIndex()); // "'" + bna.getName() + "'.actualSpeed"
            resultsService.addResult(transition.getId(), value);
            
            // condition
            value = (Double) values.get(transition.getExportIndex()); // "'" + bna.getName() + "'.fire"
            resultsService.addResult(transition.getId(), value);
        }
        
        String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);

//        System.out.println(time + ": " + values.get(name2index.get("time")));
//        value = (Double)values.get(name2index.get("time"));
//        this.simResult.addTime(value);
    }
    
    public boolean isRunning() {
        return isRunning;
    }
}
