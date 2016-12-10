/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model;

import edu.unibi.agbi.gnius.core.model.entity.DataEdge;

/**
 *
 * @author PR
 */
public class EdgeTypeChoice
{
    private final DataEdge.Type type;
    private final String typeName;

    public EdgeTypeChoice(DataEdge.Type type , String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public DataEdge.Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
