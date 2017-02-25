/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.dao;

import edu.unibi.agbi.petrinet.model.PetriNet;

import org.springframework.stereotype.Repository;

/**
 * 
 * @author PR
 */
@Repository
public class PetriNetDao extends PetriNet
{
    public PetriNetDao() {
        super();
        this.setName("Untitled");
        this.setAuthor(System.getProperty("user.name"));
        this.setDescription("New petri net.");
    }
}
