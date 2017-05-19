/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.impl;

import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.abstr.Node;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Token;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author PR
 */
public class Place extends Node
{
    private Type placeType;

    private final Map<Colour, Token> token;

    public Place(String id) {
        super(id);
        this.type = Element.Type.PLACE;
        this.token = new HashMap();
        this.token.put(new Colour("DEFAULT", "Default colour"), new Token(new Colour("DEFAULT", "Default colour")));
    }

    public final void addToken(Token token) {
        this.token.put(token.getColour(), token);
    }

    public final Token getToken(Colour colour) {
        return token.get(colour);
    }

    public final Collection<Token> getTokens() {
        return token.values();
    }

    public final void setPlaceType(Type placeType) {
        this.placeType = placeType;
    }

    public final Type getPlaceType() {
        return placeType;
    }

    public enum Type
    {
        CONTINUOUS, DISCRETE;
    }
}
