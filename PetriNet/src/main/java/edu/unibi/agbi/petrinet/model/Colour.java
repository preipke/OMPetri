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
public class Colour
{
    private String id;
    private String description;

    public String getId() {
        return id;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object != null) {
            if (object instanceof Colour) {
                if (this.id.matches(((Colour)object).getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
