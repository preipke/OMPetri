/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphCluster;
import edu.unibi.agbi.gravisfx.entity.GravisType;
import edu.unibi.agbi.gravisfx.entity.parent.node.GravisRectangle;
import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import edu.unibi.agbi.gravisfx.graph.Graph;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class GraphCluster extends GravisRectangle implements IGraphCluster {
    
    private final DataCluster dataCluster;
    
    public GraphCluster(String id, DataCluster dataCluster) {
        super(id, GravisType.CLUSTER);
        this.dataCluster = dataCluster;
        this.dataCluster.getShapes().add(this);
        
        setWidth(getHeight());
        setArcWidth(getArcHeight());
        
        super.getCircle().getStyleClass().add("cluster-place");
        super.getCircle().setRadius(super.getCircle().getRadius() / 8 * 7);
        super.getCircle().translateXProperty().bind(translateXProperty().add(getWidth() / 5 * 2));
        super.getCircle().translateYProperty().bind(translateYProperty().add(getHeight() / 5 * 2));
        
        super.getRectangle().getStyleClass().add("cluster-transition");
        super.getRectangle().setWidth(super.getRectangle().getWidth() / 8 * 7);
        super.getRectangle().setHeight(super.getRectangle().getHeight() / 8 * 7);
        super.getRectangle().translateXProperty().bind(translateXProperty().add(getWidth() / 3 * 2 + 1));
        super.getRectangle().translateYProperty().bind(translateYProperty().add(getHeight() / 4 * 1));
    }
    
    @Override
    public DataCluster getData() {
        return dataCluster;
    }

    @Override
    public Graph getGraph() {
        return dataCluster.getGraph();
    }
    
//    @Override
//    public List<GravisShapeHandle> getChildElementHandles() {
//        return new ArrayList();
//    }
}
