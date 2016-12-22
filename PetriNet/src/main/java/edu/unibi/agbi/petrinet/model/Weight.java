/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

/**
 *
 * @author PR
 */
public class Weight
{
    private final Colour colour;
    private String weight = "1";
    
    public Weight(Colour colour) {
        this.colour = colour;
    }

    public Colour getColour() {
        return colour;
    }

    public String getValue() {
        return weight;
    }

    public void setValue(String weight) {
        this.weight = weight;
    }
}
