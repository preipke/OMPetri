/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

/**
 *
 * @author PR
 */
public interface IPN_Element
{
    public String getId();
    public void setExportIndex(int index);
    public int getExportIndex();
    public PN_Element.Type getElementType();
    public boolean isEnabled();
}
