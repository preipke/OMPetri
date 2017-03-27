/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.parent.connection;

import edu.unibi.agbi.gravisfx.graph.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.entity.util.ElementHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisEdge extends Line implements IGravisConnection
{
    private final List<ElementHandle> elementHandles = new ArrayList();
    
    private final IGravisNode source;
    private final IGravisNode target;
    
    /**
     * 
     * @param source must not be null, final
     * @param target can be null and set later
     */
    public GravisEdge(IGravisNode source, IGravisNode target) {
        
        super();
        
        elementHandles.add(new ElementHandle(this));
        
        this.source = source;
        this.target = target;
        
        startXProperty().bind(source.translateXProperty().add(source.getOffsetX()));
        startYProperty().bind(source.translateYProperty().add(source.getOffsetY()));
        
        if (target != null) {
            endXProperty().bind(target.translateXProperty().add(target.getOffsetX()));
            endYProperty().bind(target.translateYProperty().add(target.getOffsetY()));
        } else {
            endXProperty().set(startXProperty().get());
            endYProperty().set(startYProperty().get());
        }
    }
    
    @Override
    public Object getBean() {
        return GravisEdge.this;
    }

    @Override
    public List<ElementHandle> getElementHandles() {
        return elementHandles;
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
    public IGravisNode getSource() {
        return source;
    }
    
    @Override
    public IGravisNode getTarget() {
        return target;
    }
}
