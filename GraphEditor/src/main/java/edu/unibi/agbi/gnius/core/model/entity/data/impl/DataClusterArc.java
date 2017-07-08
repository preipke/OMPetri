/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author PR
 */
public final class DataClusterArc extends DataArc {
    
    private final Set<IGraphElement> shapes;
    private final Map<String,IGraphArc> storedArcs;
    
    private String description;
    
    public DataClusterArc(String id, IDataNode source, IDataNode target) {
        super(id, source, target, null);
        super.type = Element.Type.CLUSTERARC;
        super.name = id;
        this.shapes = new HashSet();
        this.storedArcs = new HashMap();
    }
    
    public Map<String,IGraphArc> getStoredArcs() {
        return storedArcs;
    }

    @Override
    public Set<IGraphElement> getShapes() {
        return shapes;
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
        throw new UnsupportedOperationException("This method is not meant to be used at any time.");
    }

    @Override
    public void setLabelText(String text) {
        throw new UnsupportedOperationException("This method is not meant to be used at any time.");
    }
}
