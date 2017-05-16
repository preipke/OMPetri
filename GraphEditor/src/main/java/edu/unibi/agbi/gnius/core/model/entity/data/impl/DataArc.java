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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class DataArc extends Arc implements IDataArc
{
    private final List<IGraphElement> shapes;
    
    private String description = "";
    
    public DataArc(IDataNode source, IDataNode target, Arc.Type arctype) {
        super(source, target, arctype);
        this.shapes = new ArrayList();
    }

    @Override
    public List<IGraphElement> getShapes() {
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

    @Override
    public String toString() {
        return "(" + id + ") " + name;
    }
}
