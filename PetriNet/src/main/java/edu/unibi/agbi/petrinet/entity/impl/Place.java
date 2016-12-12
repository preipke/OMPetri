/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.impl;

import edu.unibi.agbi.petrinet.entity.PN_Node;
import edu.unibi.agbi.petrinet.entity.PN_Element;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Token;
import java.util.Collection;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author PR
 */
public abstract class Place extends PN_Node
{
    private Type placeType;
    
    private final Map<Colour,Token> token;
    
    public Place() {
        
        type = PN_Element.Type.PLACE;
        
        token = new HashMap();
        token.put(null , new Token(null));
    }
    
    public void addToken(Token token) {
        this.token.put(token.getColour() , token);
    }
    
    public Collection<Token> getToken() {
        return token.values();
    }
    
    public Map<Colour, Token> getTokenMap() {
        return token;
    }
    
    public Type getPlaceType() {
        return placeType;
    }
    
    public enum Type {
        CONTINUOUS, DEFAULT, DISCRETE;
    }
}
