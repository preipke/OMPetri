/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.dao;

import edu.unibi.agbi.gnius.service.model.EdgeType;
import edu.unibi.agbi.gnius.service.model.NodeType;
import edu.unibi.agbi.gnius.service.exception.EdgeCreationException;
import edu.unibi.agbi.gnius.service.exception.NodeCreationException;
import edu.unibi.agbi.petrinet.model.PNNode;
import edu.unibi.agbi.petrinet.model.PetriNet;
import edu.unibi.agbi.petrinet.model.entity.Arc;
import edu.unibi.agbi.petrinet.model.entity.Place;
import edu.unibi.agbi.petrinet.model.entity.Transition;
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
    private static final Map<String,PNNode> pnNodesInternalIds = new HashMap();
    private static final Map<String,ArrayList<PNNode>> pnNodesUserIds = new HashMap();
    
    public void add(PNNode node, NodeType.Type type) throws NodeCreationException {
        switch(type) {
            case PLACE:
                add((Place) node);
                break;
            case TRANSITION:
                add((Transition) node);
                break;
            default:
                throw new NodeCreationException("No suitable node type selected!");
        }
    }
    
    public void add(PNNode node, EdgeType.Type type) throws EdgeCreationException {
        switch(type) {
            case ARC:
                add((Arc) node);
                break;
            case EDGE:
                add((Arc) node);
                break;
            default:
                throw new EdgeCreationException("No suitable node type selected!");
        }
    }
}
