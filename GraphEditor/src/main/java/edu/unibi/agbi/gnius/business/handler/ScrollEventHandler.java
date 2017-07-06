/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.handler;

import edu.unibi.agbi.gnius.business.controller.editor.graph.ZoomController;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import javafx.scene.input.ScrollEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class ScrollEventHandler
{
    @Autowired private ZoomController zoomController;
    
    @Value("${zoom.scale.base}") private double scaleBase;
    @Value("${zoom.scale.factor}") private double scaleFactor;
    @Value("${zoom.scale.max}") private double scaleMax;
    @Value("${zoom.scale.min}") private double scaleMin;
    
    public void registerTo(DataDao dao) {
        
        dao.getGraphPane().setOnScroll(( ScrollEvent event ) -> {
            
            double scale_t0, scale_t1;
            
            scale_t0 = scaleBase * Math.pow(scaleFactor , dao.getScalePower());
            
            if (event.getDeltaY() > 0) {
                scale_t1 = scaleBase * Math.pow(scaleFactor, dao.getScalePower() + 1);
                if (scale_t1 > scaleMax) {
                    return;
                }
                dao.setScalePower(dao.getScalePower() + 1);
            } else {
                scale_t1 = scaleBase * Math.pow(scaleFactor, dao.getScalePower() - 1);
                if (scale_t1 < scaleMin) {
                    return;
                }
                dao.setScalePower(dao.getScalePower() - 1);
            }
            
            dao.getGraphPane().getGraph().getScale().setX(scale_t1);
            dao.getGraphPane().getGraph().getScale().setY(scale_t1);
            
            zoomController.ApplyZoomOffset(
                    dao.getGraphPane(), 
                    event.getX(), 
                    event.getY(), 
                    scale_t1 / scale_t0
            );
        });
    }
}
