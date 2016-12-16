/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class DataPlace extends Place implements IDataNode
{
    private final List<IGraphElement> shapes;
    
    public DataPlace() {
        super();
        shapes = new ArrayList();
    }

    @Override
    public List<IGraphElement> getShapes() {
        return shapes;
    }
}
