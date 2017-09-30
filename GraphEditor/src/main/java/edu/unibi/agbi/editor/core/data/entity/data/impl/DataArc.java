/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.data.entity.data.impl;

import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.core.data.entity.data.IDataArc;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author PR
 */
public class DataArc extends Arc implements IDataArc
{
    private final DataType dataType;
    private final Set<IGraphElement> shapes;

    private String description;

    public DataArc(String id, Arc.Type arctype) {
        this(id, null, null, arctype);
    }

    public DataArc(String id, IDataNode source, Arc.Type arctype) {
        this(id, source, null, arctype);
    }

    public DataArc(String id, IDataNode source, IDataNode target, Arc.Type arctype) {
        super(id, source, target, arctype);
        super.name = id;
        this.dataType = DataType.ARC;
        this.shapes = new HashSet();
    }

    @Override
    public DataType getType() {
        return dataType;
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
    public void setDisabled(boolean value) {
        if (isDisabled() != value) {
            super.setDisabled(value);
            for (IGraphElement shape : shapes) {
                shape.setElementDisabled(value);
            }
        }
    }

    @Override
    public String getLabelText() {
        return "";
    }

    @Override
    public void setLabelText(String text) {
    }

    @Override
    public Set<IGraphElement> getShapes() {
        return shapes;
    }

    @Override
    public boolean isSticky() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setSticky(boolean value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public IDataNode getSource() {
        return (IDataNode) this.source;
    }

    @Override
    public IDataNode getTarget() {
        return (IDataNode) this.target;
    }
}
