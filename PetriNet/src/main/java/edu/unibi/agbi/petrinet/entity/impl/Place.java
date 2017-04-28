/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.impl;

import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.abstr.Node;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.PetriNet;
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
    private static final String IDENT = "P";
    private static int COUNT = 0;

    private Type placeType;

    private final Map<Colour, Token> token;

    public Place() {

        super(IDENT + ++COUNT);
        type = Element.Type.PLACE;

        token = new HashMap();
        token.put(PetriNet.DEFAULT_COLOUR, new Token(PetriNet.DEFAULT_COLOUR));
    }

    public final void setToken(Token token) {
        this.token.put(token.getColour(), token);
    }

    public final Token getToken(Colour colour) {
        return token.get(colour);
    }

    public final Collection<Token> getToken() {
        return token.values();
    }

    public final Map<Colour, Token> getTokenMap() {
        return token;
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
