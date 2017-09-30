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
    private Function weight;

    public Weight(Colour colour) {
        this.colour = colour;
        this.weight = new Function(Function.Type.FUNCTION);
        this.weight.addElement(new Function("1", Function.Type.NUMBER));
    }

    public Colour getColour() {
        return colour;
    }

    public Function getFunction() {
        return weight;
    }

    public void setFunction(Function weight) {
        this.weight = weight;
    }
}
