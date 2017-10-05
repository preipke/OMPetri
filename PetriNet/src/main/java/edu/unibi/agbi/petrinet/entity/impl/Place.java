/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.impl;

import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.abstr.Node;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Parameter;
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
    private ConflictResolutionType conflictResType;

    private final Map<Colour, Token> token;

    public Place(String id, Type placeType, ConflictResolutionType conflictResType) {
        super(id, Element.Type.PLACE);
        this.placeType = placeType;
        this.conflictResType = conflictResType;
        this.token = new HashMap();
        this.parametersLocal = null;
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
    
    public final void setConflictResolutionType(ConflictResolutionType conflictResType) {
        this.conflictResType = conflictResType;
    }
    
    public final ConflictResolutionType getConflictResolutionType() {
        return conflictResType;
    }

    public final void setPlaceType(Type placeType) {
        this.placeType = placeType;
    }

    public final Type getPlaceType() {
        return placeType;
    }
    
    @Override
    public void addLocalParameter(Parameter param) {
        throw new UnsupportedOperationException("A place is not supposed to have any local parameters!");
    }
    
    @Override
    public Parameter getLocalParameter(String id) {
        throw new UnsupportedOperationException("A place is not supposed to have any local parameters!");
    }
    
    @Override
    public Collection<Parameter> getLocalParameters() {
        throw new UnsupportedOperationException("A place is not supposed to have any local parameters!");
    }

    public enum Type
    {
        CONTINUOUS, DISCRETE;
    }
    
    public enum ConflictResolutionType
    {
        PRIORITY, PROBABILITY;
    }
}
