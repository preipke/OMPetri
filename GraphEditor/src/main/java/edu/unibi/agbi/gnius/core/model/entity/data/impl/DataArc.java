/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class DataArc extends Arc implements IDataArc
{
    private final List<IGraphArc> shapes;
    
    public DataArc() {
        super();
        shapes = new ArrayList();
    }

    @Override
    public List<IGraphArc> getShapes() {
        return shapes;
    }
}
