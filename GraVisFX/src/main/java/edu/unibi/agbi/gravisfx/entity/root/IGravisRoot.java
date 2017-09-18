/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.root;

import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import java.util.List;
import edu.unibi.agbi.gravisfx.entity.IGravisItem;

/**
 *
 * @author pr
 */
public interface IGravisRoot extends IGravisItem {
    
    public List<GravisShapeHandle> getRootHandles();
    
    public List<GravisShapeHandle> getChildHandles();
    
    public void setId(String id);
    
    public String getId();
    
    public GravisType getType();
}
