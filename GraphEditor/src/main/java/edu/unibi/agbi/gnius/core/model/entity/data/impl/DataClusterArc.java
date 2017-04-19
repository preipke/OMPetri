/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public final class DataClusterArc extends DataArc {
    
    private final List<IGraphElement> shapes;
    private final DataCluster dataCluster;
    
    public DataClusterArc(DataCluster dataCluster) {
        super(dataCluster, dataCluster, null);
        super.type = Element.Type.CLUSTERARC;
        this.shapes = new ArrayList();
        this.dataCluster = dataCluster;
    }
    
    public DataCluster getDataCluster() {
        return dataCluster;
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

    @Override
    public List<IGraphElement> getGraphElements() {
        return shapes;
    }
}
