/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.controller.data;

import edu.unibi.agbi.petrinet.model.PNNode;
import edu.unibi.agbi.gnius.exception.controller.ControllerNotNullException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author PR
 */
public class DataController
{
    private Map<String,PNNode> petriNodeMap;
    
    private static DataController controller;
    
    public static DataController get() {
        return controller;
    }
    
    public DataController() throws ControllerNotNullException {
        if (controller != null) {
            throw new ControllerNotNullException("DataController has already been initialized!");
        }
        controller = this;
        
        petriNodeMap = new HashMap();
    }
    
    
    
    /**
     * Ideas for functionality.
     * 
     * unused shapes: remove all shapes not linked to node in petri net
     * node table overview: center view to on graph
     * 
     * copy node(s)
     * clone node(s)
     * delete node(s)
     */
}
