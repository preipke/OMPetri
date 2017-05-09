/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.abstr.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class DataCluster extends Node implements IDataNode {
    
    private static final String IDENT = "C";
    private static int COUNT = 0;
    
    private final List<IGraphElement> shapes;
    
    private final List<IGraphNode> nodes;
    private final List<IGraphArc> arcs;
    
    private String description = "";
    
    public DataCluster(List<IGraphNode> nodes, List<IGraphArc> arcs) {
        super(IDENT + ++COUNT);
        super.type = Element.Type.CLUSTER;
        super.name = id;
        this.nodes = nodes;
        this.arcs = arcs;
        this.shapes = new ArrayList();
    }
    
    public List<IGraphNode> getClusteredNodes() {
        return nodes;
    }
    
    public List<IGraphArc> getClusteredArcs() {
        return arcs;
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
        return ((IGraphNode)shapes.get(0)).getLabel().getText();
    }

    /**
     * Sets the label text for this data node and all related shapes in the scene.
     * @param text 
     */
    @Override
    public void setLabelText(String text) {
        for (IGraphElement shape : shapes) {
            ((IGraphNode)shape).getLabel().setText(text);
        }
    }

    @Override
    public List<IGraphElement> getGraphElements() {
        return shapes;
    }

    @Override
    public boolean isConstant() {
        throw new UnsupportedOperationException("This method is not meant to be used at any time.");
    }
}
