/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.DataType;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
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
    private final DataType dataType;
    private final Set<IGraphElement> shapes;

    private String description;

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
        super.setDisabled(value);

        IGraphArc arc;
        for (IGraphElement shape : shapes) {

            arc = (IGraphArc) shape;
            arc.getElementHandles().forEach(handle -> handle.setDisabled(value));

            if (value) { // experimentary - automatically adjusts related nodes styles based on active connections

                boolean active;

                if (!arc.getSource().getData().isDisabled()) { // if node data is not disabled, 

                    active = arc.getSource().getConnections().stream() // for all related connections
                            .anyMatch(conn -> !((IGraphArc) conn).getData().isDisabled()); // check if any is not disabled

                    if (!active) { // if no active connection is to be found, indicate that node is disabled
                        arc.getSource().setElementDisabled(value);
//                        arc.getSource().getElementHandles().forEach(handle -> handle.setDisabled(value));
                    }

                }

                if (!arc.getTarget().getData().isDisabled()) {

                    active = arc.getTarget().getConnections().stream() // for all related connections
                            .anyMatch(conn -> !((IGraphArc) conn).getData().isDisabled()); // check if any is not disabled

                    if (!active) { // if no active connection is to be found, indicate that node is disabled
                        arc.getTarget().setElementDisabled(value);
//                        arc.getTarget().getElementHandles().forEach(handle -> handle.setDisabled(value));
                    }

                }

            } else {

                if (!arc.getSource().getData().isDisabled()) { // if node data is not disabled, indicate that node is enabled as connected arc is enabled
                    arc.getSource().setElementDisabled(value);
//                    arc.getSource().getElementHandles().forEach(handle -> handle.setDisabled(value));
                }
                if (!arc.getTarget().getData().isDisabled()) { // same here
                    arc.getTarget().setElementDisabled(value);
//                    arc.getTarget().getElementHandles().forEach(handle -> handle.setDisabled(value));
                }
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
