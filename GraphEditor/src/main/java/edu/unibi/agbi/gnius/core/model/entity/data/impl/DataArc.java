/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author PR
 */
public class DataArc extends Arc implements IDataArc
{
    private final Set<IGraphElement> shapes;
    
    private String description = "";
    
    public DataArc(String id, IDataNode source, Arc.Type arctype) {
        this(id, source, null, arctype);
    }
    
    public DataArc(String id, IDataNode source, IDataNode target, Arc.Type arctype) {
        super(id, source, target, arctype);
        super.name = id;
        this.shapes = new HashSet();
    }

    @Override
    public Set<IGraphElement> getShapes() {
        return shapes;
    }

    @Override
    public IDataNode getSource() {
        return (IDataNode) this.source;
    }

    @Override
    public IDataNode getTarget() {
        return (IDataNode) this.target;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getLabelText() {
        throw new UnsupportedOperationException("This method is not meant to be used at any time.");
    }

    @Override
    public void setLabelText(String text) {
        throw new UnsupportedOperationException("This method is not meant to be used at any time.");
    }
}
