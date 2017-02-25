/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.petrinet.entity.abstr.Place;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public class DataPlace extends Place implements IDataNode
{
    private final List<IGraphElement> shapes;
    
    private String name;
    private String description;
    
    public DataPlace(Place.Type type) {
        setPlaceType(type);
        name = super.id;
        description = "";
        shapes = new ArrayList();
    }

    @Override
    public List<IGraphElement> getShapes() {
        return shapes;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
