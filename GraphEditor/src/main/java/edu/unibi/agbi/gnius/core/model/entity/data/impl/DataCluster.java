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
    private final Graph layer;

//    private final IGraphCluster parentCluster;
//    private final List<IGraphCluster> childCluster;
    private final List<IGraphNode> nodesInside;

    private final Set<IGraphArc> arcsInside;
    private final Set<IGraphArc> arcsToOutside;

    private String description = "";

    /**
     *
     * @param id          the unique identifier for this node
     * @param layer       graph layer that presents the elements inside this cluster
     * @param nodesInside list of nodes that are inside the cluster
     * @param arcsInside  list of arcs that connect the nodes inside the cluster
     * @param arcsOutside list of arcs that connect to nodes outside the cluster
     */
    public DataCluster(String id, Graph layer, List<IGraphNode> nodesInside, Set<IGraphArc> arcsInside, Set<IGraphArc> arcsOutside) {
        super(id);
        super.type = Element.Type.CLUSTER;
        super.name = id;

        this.shapes = new HashSet();
        this.layer = layer;

        this.nodesInside = nodesInside;
        this.arcsInside = arcsInside;
        this.arcsToOutside = arcsOutside;
    }
    
    public Graph getLayer() {
        return layer;
    }

    public List<IGraphNode> getClusteredNodesInside() {
        return nodesInside;
    }

    public Set<IGraphArc> getClusteredArcsInside() {
        return arcsInside;
    }

    public Set<IGraphArc> getClusteredArcsToOutside() {
        return arcsToOutside;
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
    public Set<IGraphElement> getShapes() {
        return shapes;
    }

    @Override
    public boolean isConstant() {
        throw new UnsupportedOperationException("This method is not meant to be used at any time.");
    }
}
