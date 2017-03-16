/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.simulation;

import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.service.SimulationService;
import edu.unibi.agbi.gnius.core.service.exception.SimulationServiceException;
import edu.unibi.agbi.gnius.util.Utility;
import edu.unibi.agbi.petrinet.model.References;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Map;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class OpenModelicaServer
{
    @Autowired private SimulationService simulationService;
    
    private final int SIZE_OF_INT; // size of modelica int;
    
    private boolean isTerminated = false;
    private boolean isRunning = false;
    
    private ServerSocket serverSocket;
    private DataInputStream inputStream;

    public OpenModelicaServer() {
        if (Utility.isOsWindows()) {
            SIZE_OF_INT = 4;
        } else {
            SIZE_OF_INT = 8;
        }
    }
    
    /**
     * Starts the server thread. Waits for the thread to start before returning.
     * @param port
     * @param references
     * @return 
     * @throws edu.unibi.agbi.gnius.core.service.exception.SimulationServiceException 
     */
    public Thread StartThread(int port, final References references) throws SimulationServiceException {
        
        final Boolean serverSync = true;
        Thread serverThread;
        
        /**
         * Start server thread.
         */
        serverThread = new Thread(() -> {
            
            try {
                isTerminated = false;
                isRunning = true;
                serverSocket = new java.net.ServerSocket(port);
                
                synchronized (serverSync) {
                    serverSync.notify();
                }
                
                while (true) {
                    
                    if (serverSocket.isClosed()) {
                        break;
                    }
                    
                    final Socket client;
                    final String[] variables;
                    
                    System.out.println("Waiting for client...");
                    client = serverSocket.accept();
                    System.out.println("Client connected!");
                    inputStream = new DataInputStream(client.getInputStream());
                    
                    variables = ReadSimulationVariables();
                    
                    synchronized(client) {

                        Platform.runLater(() -> {
                            synchronized(client) {
                                simulationService.InitSimulation(variables , references); // has to be executed on main thread, changelistener attached
                                System.out.println("Simulation storage initialized!..");
                                client.notify();
                            }
                        });
                        
                        try {
                            System.out.println("Waiting for initialization of simulation storage...");
                            client.wait();
                        } catch (InterruptedException ex) {
                            try {
                                throw new SimulationServiceException("Simulation server got interrupted! " + ex);
                            } catch (SimulationServiceException iex) {
                                throw new IOException(iex);
                            }
                        }
                    }
                    
                    StoreSimulationResults(simulationService.getLatestSimulation());
                    
                    System.out.println("Client disconnect!");
                }
            } catch (IOException ex) {
                if (isTerminated) {
                    System.out.println("Server terminated!");
                } else {
                    System.out.println("Exception while processing client request!");
                    System.out.println(ex.toString());
                }
            }
        });
        serverThread.start();
        
        /**
         * Wait for server thread before returning.
         */
        synchronized (serverSync) {
            try {
                System.out.println("Waiting for server to start...");
                serverSync.wait();
                System.out.println("Server started!");
            } catch (InterruptedException ex) {
                System.out.println("Exception while waiting for server start!");
                System.out.println(ex);
            }
        }
        
        return serverThread;
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
    
    private byte[] buffer;
    private ByteBuffer byteBuffer;
    
    private int vars, doubles, ints, bools, expected, length;
    private byte btmp;
    
    /**
     * Reads data from the given input stream.
     * @param inputStream
     * @throws IOException 
     */
    private String[] ReadSimulationVariables() throws IOException {
        
        String[] names;
        
        int lengthMax = 2048;
        buffer = new byte[lengthMax];
        
        // read status
        inputStream.readFully(buffer, 0, 1);
//        System.out.println("Server ID: " + (int) buffer[0]);

        // read chunk size
        inputStream.readFully(buffer , 0 , 4);
        
        byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        length = byteBuffer.getInt();
        if (lengthMax < length) {
            buffer = new byte[length];
        }

        // read variables and incoming types
        inputStream.readFully(buffer , 0 , length);
        
        byteBuffer = ByteBuffer.wrap(buffer , 0 , 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        doubles = byteBuffer.getInt();
        
        byteBuffer = ByteBuffer.wrap(buffer , 4 , 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        ints = byteBuffer.getInt();

        byteBuffer = ByteBuffer.wrap(buffer , 8 , 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        bools = byteBuffer.getInt();

//        byteBuffer = ByteBuffer.wrap(buffer , 12 , 4);
//        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
//        System.out.println("strings: " + byteBuffer.getInt());

        expected = doubles * 8 + ints * SIZE_OF_INT + bools;
        
        names = new String(buffer , 16 , buffer.length - 17).split("\u0000");
        vars = names.length;
        
        System.out.println(">> START: INCOMING NODE NAMES:");
        for (String n : names) {
            System.out.println(n);
        }
        System.out.println("<< END: INCOMING NODE NAMES:");
        
        return names;
    }
    
    public void StoreSimulationResults(Simulation simulation) {

        Object[] data;
        String[] messages;
        int id, index;
        
        try {
//            int count = 0;
            while (isRunning) {

//                System.out.println("#" + count++ + ": " + inputStream.available());
                inputStream.readFully(buffer , 0 , 5); // blocks until msg received
                id = (int)buffer[0];

                byteBuffer = ByteBuffer.wrap(Arrays.copyOfRange(buffer , 1 , buffer.length - 2)); // length
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                length = byteBuffer.getInt();
                
//                System.out.println("#" + count++ + ": ID = " + id + " | " + length);

                switch (id) {
                    case 4:
                        if (length > 0) {
                            
                            inputStream.readFully(buffer , 0 , length);
                            
                            data = new Object[vars];
                            index = 0;
                            for (int r = 0; r < doubles; r++) {
                                byteBuffer = ByteBuffer.wrap(buffer , r * 8 , 8);
                                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                                data[index] = byteBuffer.getDouble();
//                                if (r == 0) {
//                                    System.out.println("time = " + data[index]);
//                                }
                                index++;
                            }
                            for (int i = 0; i < ints; i++) {
                                byteBuffer = ByteBuffer.wrap(buffer , doubles * 8 + i * SIZE_OF_INT , SIZE_OF_INT);
                                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                                data[index] = byteBuffer.getInt();
                                index++;
                            }
                            for (int b = 0; b < bools; b++) {
                                btmp = buffer[doubles * 8 + ints * SIZE_OF_INT + b];
                                data[index] = btmp;
                                index++;
                            }
                            
                            byteBuffer = ByteBuffer.wrap(buffer , expected , length - expected);
                            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

                            messages = (new String(buffer , expected , length - expected)).split("\u0000");
                            for (int i = 0; i < messages.length; i++) {
                                if (messages[i].length() > 0) {
                                    System.out.println(messages[i]);
                                }
                            }
                            
                            simulation.addResult(data);
                        }
                        break;

                    case 6:
                        System.out.println("Simulation finished!");
                        isTerminated = true;
                        isRunning = false;
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while reading data!");
//            System.out.println(e);
            e.printStackTrace();
        } finally {
            StopThread();
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }
}
