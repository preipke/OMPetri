/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.util;

import edu.unibi.agbi.petrinet.util.FunctionBuilder;
import edu.unibi.agbi.petrinet.util.OpenModelicaExporter;
import edu.unibi.agbi.petrinet.util.ParameterFactory;
import java.io.IOException;
import java.util.ResourceBundle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author PR
 */
@Configuration
public class SpringAppConfig {
    
    @Bean
    public ParameterFactory parameterFactory() {
        return new ParameterFactory();
    }
    
    @Bean
    public ResourceBundle resourceBundle() {
        return ResourceBundle.getBundle("application");
    }
    
    @Bean
    public FunctionBuilder functionBuilder() throws IOException {
        return new FunctionBuilder();
    }
    
    @Bean
    public OpenModelicaExporter openModelicaExporter() throws IOException {
        return new OpenModelicaExporter();
    }
}
