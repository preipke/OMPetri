/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.dao;

import edu.unibi.agbi.petrinet.model.PetriNet;

/**
 *
 * @author PR
 */
public final class DataDao extends PetriNet
{
    public DataDao(int nextPlaceId, int nextTransitionId) {
        super(nextPlaceId, nextTransitionId);
        this.setName("Untitled");
        this.setAuthor(System.getProperty("user.name"));
        this.setDescription("New data model.");
    }
}
