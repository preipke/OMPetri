/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author PR
 */
public final class DataClusterArc extends DataArc {
    
    private final Set<IGraphElement> shapes;
    private final DataCluster dataCluster;
    
    public DataClusterArc(DataCluster dataCluster) {
        super(dataCluster, dataCluster, null);
        super.type = Element.Type.CLUSTERARC;
        this.shapes = new HashSet();
        this.dataCluster = dataCluster;
    }
    
    public DataCluster getRelatedCluster() {
        return dataCluster;
    }
    
    public List<IDataArc> getStoredDataArcs() {
        List<IDataArc> dataArcs = new ArrayList();
        for (IGraphElement elem : shapes) {
            if (elem instanceof IGraphArc) {
                dataArcs.add(((IGraphArc) elem).getDataElement());
            }
        }
        return dataArcs;
    }

    @Override
    public Set<IGraphElement> getShapes() {
        return shapes;
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("This method is not meant to be used at any time.");
    }

    @Override
    public void setDescription(String text) {
        throw new UnsupportedOperationException("This method is not meant to be used at any time.");
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
