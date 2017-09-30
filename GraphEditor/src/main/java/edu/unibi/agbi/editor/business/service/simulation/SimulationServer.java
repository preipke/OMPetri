/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.business.service.simulation;

import edu.unibi.agbi.editor.core.data.entity.result.Simulation;
import edu.unibi.agbi.editor.business.exception.ResultsException;
import edu.unibi.agbi.editor.business.exception.SimulationException;
import edu.unibi.agbi.editor.core.util.Utility;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 *
 * @author PR
 */
public class SimulationServer extends Thread {

    private final int SIZE_OF_INT; // size of modelica int;

    private int serverPort = 17015;
    private ServerSocket serverSocket;
    private Socket client;
    private DataInputStream inputStream;

    private boolean isRunning; // indicates wether the server is up and running
    private SimulationException exception;

    private String[] simulationVariables;
    private Simulation results;

    public SimulationServer() {
        if (Utility.isOsWindows()) {
            SIZE_OF_INT = 4;
        } else {
            SIZE_OF_INT = 8;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public SimulationException getException() {
        return exception;
    }

    public int getSimulationServerPort() {
        return serverPort;
    }

    public String[] getSimulationVariables() {
        return simulationVariables;
    }

    public double getSimulationTime() {
        if (results == null || results.getTimeData().isEmpty()) {
            return 0;
        }
        return (double) results.getTimeData().get(results.getTimeData().size() - 1);
    }

    public void setResultsStorage(Simulation simulation) {
        this.results = simulation;
    }

    public void terminate() {
        isRunning = false;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ex) {
                
            }
        }
    }

    @Override
    public void run() {

        isRunning = true;
        simulationVariables = null;
        results = null;

        /**
         * Open Server Socket.
         */
        serverSocket = CreateServerSocket();
        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }
        synchronized (this) {
            this.notify(); // notify that socket is ready
        }

        /**
         * Process incoming data.
         */
        try {
            try {
                client = serverSocket.accept(); // blocks until connection found
                inputStream = new DataInputStream(client.getInputStream());
            } catch (IOException ex) {
                throw new SimulationException("Failed accepting client!", ex);
            }

            try {
                synchronized (this) {
                    simulationVariables = ReadSimulationVariables(inputStream);
                }
            } catch (IOException ex) {
                throw new SimulationException("Failed reading variables!", ex);
            }

            try {
                synchronized (this) {
                    this.notify(); // notify thread that variables have been initalized (3)
                    this.wait(); // wait for initialization of results storage object (4)
                }
            } catch (InterruptedException ex) {
                throw new SimulationException("Server got interrupted while waiting for storage object!", ex);
            }

            try {
                ReadAndStoreSimulationResults(inputStream, results);
            } catch (IOException ex) {
                throw new SimulationException("Failed reading results data!", ex);
            } catch (ResultsException ex) {
                throw new SimulationException("Failed storing results data!", ex);
            }

        } catch (SimulationException ex) {
            exception = ex;
        } finally {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.out.println("Exception closing server socket! [" + ex.getMessage() + "]");
            }
            isRunning = false;
            synchronized (this) {
                this.notifyAll(); // notfiy any still waiting threads
            }
        }
    }

    /**
     * Creates the server socket. Returns null if it fails for any reasons.
     *
     * @return
     */
    private ServerSocket CreateServerSocket() {

        boolean isSocketReady;
        int countPortsChecked = 0;

        isSocketReady = false;

        while (!isSocketReady && countPortsChecked < 20) {
            try {
                serverSocket = new java.net.ServerSocket(serverPort);
                isSocketReady = true;
            } catch (IOException ex) {
                System.out.println("Socket port '" + serverPort + "' already in use. Incrementing.");
                serverPort++;
                countPortsChecked++;
            }
        }

        if (isSocketReady) {
            return serverSocket;
        } else {
            return null;
        }
    }

    private byte[] buffer;
    private ByteBuffer byteBuffer;

    private int vars, doubles, ints, bools, expected, length;
    private byte btmp;

    /**
     * Reads data from the given input stream.
     *
     * @param inputStream
     * @throws IOException
     */
    private String[] ReadSimulationVariables(DataInputStream inputStream) throws IOException {

        String[] names;

        int lengthMax = 2048;
        buffer = new byte[lengthMax];

        // read status
        inputStream.readFully(buffer, 0, 1); // server status id

        // read chunk size
        inputStream.readFully(buffer, 0, 4);

        byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        length = byteBuffer.getInt();
        if (lengthMax < length) {
            buffer = new byte[length];
        }

        // read variables and incoming types
        inputStream.readFully(buffer, 0, length);

        byteBuffer = ByteBuffer.wrap(buffer, 0, 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        doubles = byteBuffer.getInt();

        byteBuffer = ByteBuffer.wrap(buffer, 4, 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        ints = byteBuffer.getInt();

        byteBuffer = ByteBuffer.wrap(buffer, 8, 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        bools = byteBuffer.getInt();

//        byteBuffer = ByteBuffer.wrap(buffer , 12 , 4);
//        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
//        System.out.println("strings: " + byteBuffer.getInt());
        expected = doubles * 8 + ints * SIZE_OF_INT + bools;

        names = new String(buffer, 16, buffer.length - 17).split("\u0000");
        vars = names.length;

//        System.out.println(">> START: INCOMING NODE NAMES:");
//        for (String n : names) {
//            System.out.println(n);
//        }
//        System.out.println("<< END: INCOMING NODE NAMES:");
        return names;
    }

    public void ReadAndStoreSimulationResults(DataInputStream inputStream, Simulation results) throws IOException, ResultsException {

        Object[] data;
        String[] messages;
        int id, index;

        while (isRunning) {

            inputStream.readFully(buffer, 0, 5); // blocks until msg received
            id = (int) buffer[0];

            byteBuffer = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 1, buffer.length - 2)); // length
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            length = byteBuffer.getInt();

            switch (id) {
                case 4:
                    if (length > 0) {

                        inputStream.readFully(buffer, 0, length);

                        data = new Object[vars];
                        index = 0;
                        for (int r = 0; r < doubles; r++) {
                            byteBuffer = ByteBuffer.wrap(buffer, r * 8, 8);
                            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                            data[index] = byteBuffer.getDouble();
                            index++;
                        }
                        for (int i = 0; i < ints; i++) {
                            byteBuffer = ByteBuffer.wrap(buffer, doubles * 8 + i * SIZE_OF_INT, SIZE_OF_INT);
                            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                            data[index] = byteBuffer.getInt();
                            index++;
                        }
                        for (int b = 0; b < bools; b++) {
                            btmp = buffer[doubles * 8 + ints * SIZE_OF_INT + b];
                            data[index] = btmp;
                            index++;
                        }

                        byteBuffer = ByteBuffer.wrap(buffer, expected, length - expected);
                        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

                        messages = (new String(buffer, expected, length - expected)).split("\u0000");
                        for (int i = 0; i < messages.length; i++) {
                            if (messages[i].length() > 0) {
                                System.out.println(messages[i]);
                            }
                        }
                        results.addData(data);
                    }
                    break;

                case 6:
                    isRunning = false;
                    break;
            }
        }
    }
}
