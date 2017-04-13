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
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class DataCluster implements IDataNode {
    
    private final static String CLUSTER_IDENT = "C";
    private static int clusterCount = 1;
    
    private final List<IGraphElement> shapes;
    
    private final String id;
    private String name;
    private String description = "";
    
    private final List<IGraphNode> nodes;
    private final List<IGraphArc> arcs;
    
    public DataCluster(List<IGraphNode> nodes, List<IGraphArc> arcs) {
        
        id = CLUSTER_IDENT + "_" + clusterCount++;
        name = id;
        
        this.nodes = nodes;
        this.arcs = arcs;
        
        shapes = new ArrayList();
    }
    
    public List<IGraphNode> getClusteredNodes() {
        return nodes;
    }
    
    public List<IGraphArc> getClusteredArcs() {
        return arcs;
    }

    @Override
    public void setDescription(String text) {
        description = text;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLabelText() {
        return ((IGraphNode)shapes.get(0)).getLabel().getText();
    }

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
    public Element.Type getElementType() {
        return Element.Type.CLUSTER;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<String> getFilter() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Parameter> getParameter() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEnabled() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setEnabled(boolean value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<IArc> getArcsOut() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<IArc> getArcsIn() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isConstant() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
