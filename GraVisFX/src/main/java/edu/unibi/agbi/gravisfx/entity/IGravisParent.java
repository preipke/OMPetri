/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity;

import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import java.util.List;

/**
 *
 * @author pr
 */
public interface IGravisParent {
    
    public List<GravisShapeHandle> getParentElementHandles();
    
    public List<GravisShapeHandle> getChildElementHandles();
    
    public void setId(String id);
    
    public String getId();
    
    public GravisType getType();
}
