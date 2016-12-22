/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.petrinet.entity.abstr.Arc;
import edu.unibi.agbi.petrinet.exception.IllegalAssignmentException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class DataArc extends Arc implements IDataArc
{
    private final List<IGraphElement> shapes;
    
    public DataArc(IDataNode source, IDataNode target) throws IllegalAssignmentException {
        super(source, target);
        shapes = new ArrayList();
    }

    @Override
    public List<IGraphElement> getShapes() {
        return shapes;
    }
}
