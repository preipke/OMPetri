/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.abstr.Node;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author PR
 */
public class DataCluster extends Node implements IDataNode
{
    private final Set<IGraphElement> shapes;
    private final Graph graph;

    private String description = "";

    public DataCluster(String id) {
        super(id);
        super.type = Element.Type.CLUSTER;
        super.name = id;
        this.shapes = new HashSet();
        this.graph = new Graph();
    }

    public Graph getGraph() {
        return graph;
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
    public String getLabelText() {
        if (shapes.isEmpty()) {
            return null;
        }
        return ((IGraphNode) shapes.iterator().next()).getLabel().getText();
    }

    /**
     * Sets the label text for all related shapes in the scene.
     *
     * @param text
     */
    @Override
    public void setLabelText(String text) {
        for (IGraphElement shape : shapes) {
            ((IGraphNode) shape).getLabel().setText(text);
        }
    }

    @Override
    public void setName(String name) {
        super.name = name;
        this.graph.setName(name);
    }

    @Override
    public Set<IGraphElement> getShapes() {
        return shapes;
    }

    @Override
    public boolean isConstant() {
        throw new UnsupportedOperationException("This method is not meant to be used at any time.");
    }
}
