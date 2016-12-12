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
    private Colour colour;
    private double weight = 1d;
    
    public Weight(Colour colour) {
        this.colour = colour;
    }

    public Colour getColour() {
        return colour;
    }

    public void setColour(Colour colour) {
        this.colour = colour;
    }

    public double getValue() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
