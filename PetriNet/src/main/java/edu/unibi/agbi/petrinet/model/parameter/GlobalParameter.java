/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.parameter;

import edu.unibi.agbi.petrinet.model.Parameter;

/**
 *
 * @author PR
 */
public class GlobalParameter extends Parameter
{
    public GlobalParameter(String id, String value, String unit) {
        super(Parameter.Type.GLOBAL, id, value, null);
    }
}
