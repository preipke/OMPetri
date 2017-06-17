/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.abstr.Node;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author PR
 */
public class DataCluster extends Node implements IDataNode
{
    private final Set<IGraphElement> shapes;
    private final Graph graph;

    private final Set<IGraphArc> arcs;
    private final Set<IGraphArc> arcsStored;
    private final List<IGraphNode> nodesStored;

    private String description = "";

    /**
     *
     * @param id              the unique identifier for this node
     * @param nodes           list of nodes that are inside the cluster
     * @param arcsInside      list of arcs that connects nodes inside the
     *                        cluster
     * @param arcsToOutside   list of arcs that connect to target nodes outside
     *                        the cluster
     * @param arcsFromOutside list of arcs that connect from source nodes
     *                        outside the cluster
     */
    public DataCluster(String id) {
        super(id);
        super.type = Element.Type.CLUSTER;
        super.name = id;

        this.shapes = new HashSet();
        this.graph = new Graph();

        this.arcs = new HashSet();
        this.arcsStored = new HashSet();
        this.nodesStored = new ArrayList();
    }

    public Graph getGraph() {
        return graph;
    }

    public Set<IGraphArc> getArcs() {
        return arcs;
    }

    public List<IGraphNode> getNodes() {
        return nodesStored;
    }

    public Set<IGraphArc> getStoredArcs() {
        return arcsStored;
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
