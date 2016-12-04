/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.graph.model.SelectionModel;
import edu.unibi.agbi.gravisfx.graph.model.DataModel;
import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.layer.EdgeLayer;
import edu.unibi.agbi.gravisfx.graph.layer.LabelLayer;
import edu.unibi.agbi.gravisfx.graph.layer.NodeLayer;
import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;

/**
 *
 * @author PR
 */
public final class Graph
{
    private final DataModel dataModel;
    private final SelectionModel selectionModel;
    
    private final TopLayer topLayer;
    
    private final LabelLayer labelLayer;
    private final NodeLayer nodeLayer;
    private final EdgeLayer edgeLayer;
    
    public Graph() {
        
        dataModel = new DataModel();
        selectionModel = new SelectionModel();
        
        topLayer = new TopLayer();
        
        labelLayer = topLayer.getLabelLayer();
        nodeLayer = topLayer.getNodeLayer();
        edgeLayer = topLayer.getEdgeLayer();
    }
    
    public DataModel getDataModel() {
        return dataModel;
    }
    
    public SelectionModel getSelectionModel() {
        return selectionModel;
    }
    
    public TopLayer getTopLayer() {
        return topLayer;
    }
    
    public void add(IGravisNode node) {
        if (dataModel.add(node)) {
            nodeLayer.getChildren().add(node.getShape());
        }
    }
    
    public void add(IGravisEdge edge) {
        if (dataModel.add(edge)){
            edgeLayer.getChildren().add(edge.getShape());
        }
    }
    
    public boolean containsNode(IGravisNode node) {
        return dataModel.contains(node);
    }
    
    public boolean containsEdge(IGravisEdge edge) {
        return dataModel.contains(edge);
    }
    
    public IGravisNode[] getNodes() {
        return dataModel.getNodes();
    }
    
    public IGravisEdge[] getEdges() {
        return dataModel.getEdges();
    }
    
    public void remove(IGravisNode node) {
        dataModel.remove(node);
        nodeLayer.getChildren().remove(node.getShape());
    }
    
    public void remove(IGravisEdge edge) {
        dataModel.remove(edge);
        edgeLayer.getChildren().remove(edge.getShape());
    }
}
