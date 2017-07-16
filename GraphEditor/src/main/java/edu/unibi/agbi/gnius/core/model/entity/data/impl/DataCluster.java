/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.DataType;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.entity.IGravisElement;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author PR
 */
public class DataCluster implements IDataNode
{
    private final Set<IGraphElement> shapes;
    private final DataType dataType;
    private final Graph graph;

    private final String id;
    private String description;
    private String name;

    public DataCluster(String id) {
        this.dataType = DataType.CLUSTER;
        this.graph = new Graph();
        this.id = id;
        this.name = id;
        this.shapes = new HashSet();
    }

    public Graph getGraph() {
        return graph;
    }
    
    public void UpdateShape() {
        boolean isDisabled = isDisabled();
        for (IGraphElement shape : shapes) {
            shape.getElementHandles().forEach(handle -> handle.setDisabled(isDisabled));
        }
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
    public void setDescription(String text) {
        description = text;
    }

    @Override
    public boolean isDisabled() {
        boolean isDisabled = true;
        for (IGravisElement element : graph.getNodes()) {
            isDisabled = ((IGraphElement) element).isElementDisabled();
            if (!isDisabled) { // if at least one node is not disabled, show shape as enabled
                break;
            }
        }
        return isDisabled;
    }

    @Override
    public void setDisabled(boolean value) {
        for (IGravisElement element : graph.getNodes()) {
            ((IGraphElement) element).setElementDisabled(value);
        }
        for (IGravisElement element : graph.getConnections()) {
            ((IGraphElement) element).setElementDisabled(value);
        }
        UpdateShape();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabelText() {
        if (shapes.isEmpty()) {
            return null;
        }
        return ((IGraphNode) shapes.iterator().next()).getLabel().getText();
    }

    @Override
    public void setLabelText(String text) {
        for (IGraphElement shape : shapes) {
            ((IGraphNode) shape).getLabel().setText(text);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        this.graph.setName(name);
    }

    @Override
    public Set<IGraphElement> getShapes() {
        return shapes;
    }

    @Override
    public List<IArc> getArcsIn() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<IArc> getArcsOut() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isConstant() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setConstant(boolean value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Element.Type getElementType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Parameter> getRelatedParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSticky() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSticky(boolean value) {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }
}
