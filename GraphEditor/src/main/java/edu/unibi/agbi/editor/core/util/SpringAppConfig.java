/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.util;

import edu.unibi.agbi.petrinet.util.FunctionFactory;
import edu.unibi.agbi.petrinet.util.OpenModelicaExporter;
import edu.unibi.agbi.petrinet.util.ParameterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 *
 * @author PR
 */
@Configuration
public class SpringAppConfig {
    
    @Bean
    public ResourceBundle resourceBundle() {
        return ResourceBundle.getBundle("application");
    }
    
    @Bean
    public FunctionFactory functionFactory() throws IOException {
        return new FunctionFactory();
    }
    
    @Bean
    public ParameterFactory parameterFactory() throws IOException {
        return new ParameterFactory();
    }
    
    @Bean
    public OpenModelicaExporter openModelicaExporter() throws IOException {
        return new OpenModelicaExporter();
    }
}
