/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.mode;

import edu.unibi.agbi.gnius.business.mode.impl.ArcCreationMode;
import edu.unibi.agbi.gnius.business.mode.impl.DraggingMode;
import edu.unibi.agbi.gnius.business.mode.impl.FreeMode;
import edu.unibi.agbi.gnius.business.mode.impl.NodeCreationMode;
import edu.unibi.agbi.gnius.business.mode.impl.SelectionMode;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author PR
 */
@Configuration
public class EditorModes
{
    @Bean
    public ArcCreationMode arcCreationMode() {
        return new ArcCreationMode();
    }
    
    @Bean
    public DraggingMode draggingMode() {
        return new DraggingMode();
    }
    
    @Bean
    public FreeMode freeMode() {
        return new FreeMode();
    }
    
    @Bean
    public NodeCreationMode nodeCreationMode() {
        return new NodeCreationMode();
    }
    
    @Bean
    public SelectionMode selectionMode() {
        return new SelectionMode();
    }
}
