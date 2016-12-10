/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model;

import edu.unibi.agbi.gnius.core.model.entity.DataPlace;

/**
 *
 * @author PR
 */
public class PlaceTypeChoice
{
    private final DataPlace.Type type;
    private final String typeName;

    public PlaceTypeChoice(DataPlace.Type type , String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public DataPlace.Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
