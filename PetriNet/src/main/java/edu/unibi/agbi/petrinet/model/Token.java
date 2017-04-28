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
public class Token
{
    private final Colour colour;

    private double valueMin = 0d;
    private double valueMax = Double.MAX_VALUE;
    private double valueStart = 0d;

    public Token(Colour colour) {
        this.colour = colour;
    }

    public Colour getColour() {
        return colour;
    }

    public double getValueMin() {
        return valueMin;
    }

    public void setValueMin(double valueMin) {
        this.valueMin = valueMin;
    }

    public double getValueMax() {
        return valueMax;
    }

    public void setValueMax(double valueMax) {
        this.valueMax = valueMax;
    }

    public double getValueStart() {
        return valueStart;
    }

    public void setValueStart(double valueStart) {
        this.valueStart = valueStart;
    }
}
