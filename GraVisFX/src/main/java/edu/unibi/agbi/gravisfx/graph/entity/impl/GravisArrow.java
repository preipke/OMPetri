/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.impl;

import edu.unibi.agbi.gravisfx.GravisProperties;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 *
 * @author PR
 */
public class GravisArrow extends Path
{
    public GravisArrow() {
        
        super();
        
        getElements().add(new MoveTo(GravisProperties.ARROW_WIDTH / 3d , GravisProperties.ARROW_HEIGHT / 2d));
        getElements().add(new LineTo(0 , 0));
        getElements().add(new LineTo(GravisProperties.ARROW_WIDTH , GravisProperties.ARROW_HEIGHT / 2d));
        getElements().add(new LineTo(0 , GravisProperties.ARROW_HEIGHT));
        getElements().add(new LineTo(GravisProperties.ARROW_WIDTH / 3d , GravisProperties.ARROW_HEIGHT / 2d));
    }
}
