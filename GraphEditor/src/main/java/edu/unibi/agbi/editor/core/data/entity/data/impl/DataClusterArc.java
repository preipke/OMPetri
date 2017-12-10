/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.data.entity.data.impl;

import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.core.data.entity.data.IDataArc;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.Weight;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author PR
 */
public final class DataClusterArc implements IDataArc {
    
    private final DataType dataType;
    private final Set<IGraphElement> shapes;
    private final Map<String,IGraphArc> storedArcs;
    
    private String id;
    private String description;
//    private String name;
    
    public DataClusterArc(String id) {
        this.dataType = DataType.CLUSTERARC;
        this.id = id;
//        this.name = id;
        this.shapes = new HashSet();
        this.storedArcs = new HashMap();
    }
    
    public Map<String,IGraphArc> getStoredArcs() {
        return storedArcs;
    }

    @Override
    public DataType getType() {
        return dataType;
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
    public boolean isDisabled() {
        boolean isDisabled = true;
        for (IGraphArc arc : storedArcs.values()) {
            isDisabled = arc.getData().isDisabled();
            if (!isDisabled) { // if at least one arc is not disabled, show shape as enabled
                break;
            }
        }
        return isDisabled;
    }

    @Override
    public void setDisabled(boolean value) {
        for (IGraphArc arc : storedArcs.values()) {
            arc.getData().setDisabled(true);
        }
    }

    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getLabelText() {
        return "";
    }

    @Override
    public void setLabelText(String text) {
    }

//    @Override
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public void setName(String name) {
//        this.name = name;
//    }

    @Override
    public Set<IGraphElement> getShapes() {
        return shapes;
    }

    @Override
    public Arc.Type getArcType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setArcType(Arc.Type arcType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Element.Type getElementType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addLocalParameter(Parameter param) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Parameter getLocalParameter(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Parameter> getLocalParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Parameter> getRelatedParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSticky() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSticky(boolean value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IDataNode getSource() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IDataNode getTarget() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addWeight(Weight weight) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Weight getWeight(Colour colour) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Weight> getWeights() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getConflictResolutionValue() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setConflictResolutionValue(double conflictResValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
