/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.parent.node;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisSubElement;
import edu.unibi.agbi.gravisfx.graph.entity.child.GravisLabel;
import edu.unibi.agbi.gravisfx.graph.entity.util.ElementHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisCircle extends Circle implements IGravisNode
{
    private final List<ElementHandle> elementHandles = new ArrayList();
    
    private final List<IGravisNode> children = new ArrayList();
    private final List<IGravisNode> parents = new ArrayList();
    private final List<IGravisConnection> edges = new ArrayList();
    
    private final GravisLabel label;
    
    private boolean isChildShapesEnabled = true;
    
    public GravisCircle() {
        
        super();
        
        elementHandles.add(new ElementHandle(this));
        
        setRadius(GravisProperties.CIRCLE_RADIUS);
        
        label = new GravisLabel(this);
        label.xProperty().bind(translateXProperty().add(GravisProperties.LABEL_OFFSET_X));
        label.yProperty().bind(translateYProperty().add(GravisProperties.LABEL_OFFSET_Y));
    }
    
    @Override
    public Object getBean() {
        return GravisCircle.this;
    }
    
    @Override
    public Shape getShape() {
        return this;
    }
    
    @Override
    public List<Shape> getShapes() {
        List<Shape> shapes = new ArrayList();
        shapes.add(this);
        return shapes;
    }

    @Override
    public final List<ElementHandle> getElementHandles() {
        return elementHandles;
    }

    @Override
    public final double getOffsetX() {
        return 0; // position is fixed to shape center
    }

    @Override
    public final double getOffsetY() {
        return 0; // position is fixed to shape center
    }
    
    @Override
    public final void addParentNode(IGravisNode parent) {
        parents.add(parent);
    }
    
    @Override
    public final void addChildNode(IGravisNode child) {
        children.add(child);
    }
    
    @Override
    public final void addConnection(IGravisConnection edge) {
        edges.add(edge);
    }
    
    @Override
    public final List<IGravisNode> getParents() {
        return parents;
    }
    
    @Override
    public final List<IGravisNode> getChildren() {
        return children;
    }
    
    @Override
    public final List<IGravisConnection> getConnections() {
        return edges;
    }
    
    @Override
    public final boolean removeChild(IGravisNode node) {
        return children.remove(node);
    }
    
    @Override
    public final boolean removeParent(IGravisNode node) {
        return parents.remove(node);
    }
    
    @Override
    public final boolean removeConnection(IGravisConnection edge) {
        return edges.remove(edge);
    }
    
    @Override
    public final boolean isChildShapesEnabled() {
        return isChildShapesEnabled;
    }
    
    @Override
    public final void setChildShapesEnabled(boolean value) {
        isChildShapesEnabled = value;
    }
    
    @Override
    public List<IGravisSubElement> getChildElements() {
        return new ArrayList();
    }
    
    @Override
    public final GravisLabel getLabel() {
        return label;
    }
}
