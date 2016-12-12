/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.dao;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.service.exception.NodeCreationException;

import edu.unibi.agbi.petrinet.model.PetriNet;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.entity.PN_Element;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import edu.unibi.agbi.petrinet.entity.impl.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

/**
 * 
 * @author PR
 */
@Repository
public class PetriNetDao extends PetriNet
{
    private static final Map<String,PN_Element> pnNodesInternalIds = new HashMap();
    private static final Map<String,ArrayList<PN_Element>> pnNodesUserIds = new HashMap();
    
    public void add(IDataNode node) throws NodeCreationException {
        
        PN_Element.Type type = node.getElementType();
        
        switch(type) {
            case PLACE:
                add((Place) node);
                break;
            case TRANSITION:
                add((Transition) node);
                break;
            case ARC:
                add((Arc) node);
                break;
            default:
                throw new NodeCreationException("No suitable node type selected!");
        }
    }
}
