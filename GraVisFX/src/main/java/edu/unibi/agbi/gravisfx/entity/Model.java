/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity;

import edu.unibi.agbi.gravisfx.entity.nodes.IGravisNode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class Model
{
    List<IGravisNode> nodes = new ArrayList();
    
    public List<IGravisNode> getAllNodes() {
        return nodes;
    }
}
