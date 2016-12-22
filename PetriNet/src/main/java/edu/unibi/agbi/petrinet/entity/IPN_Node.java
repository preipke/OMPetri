/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import edu.unibi.agbi.petrinet.entity.abstr.Arc;
import java.util.List;

/**
 *
 * @author PR
 */
public interface IPN_Node extends IPN_Element
{
    public List<Arc> getArcsOut();
    public List<Arc> getArcsIn();
}
