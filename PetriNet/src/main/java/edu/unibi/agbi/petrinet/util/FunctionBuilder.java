/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.util;

import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.FunctionElement;
import edu.unibi.agbi.petrinet.model.FunctionElement.Type;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author PR
 */
public class FunctionBuilder
{
    private final String propertiesPath = "/function.properties";
    private final Properties properties;
    
    public FunctionBuilder() throws IOException {
        properties = new Properties();
        properties.load(FunctionBuilder.class.getResourceAsStream(propertiesPath));
    }
    
    public Function build(String functionString) throws IOException {
        
        Function function = new Function();
        FunctionElement element;
        
        Pattern patternNumber = Pattern.compile(properties.getProperty("regex.function.number"));
        Pattern patternOperator = Pattern.compile(properties.getProperty("regex.function.operator"));
        Pattern patternParamater = Pattern.compile(properties.getProperty("regex.function.parameter"));
        
        Matcher numberMatcher = patternNumber.matcher(functionString);
        Matcher operatorMatcher = patternOperator.matcher(functionString);
        Matcher parameterMatcher = patternParamater.matcher(functionString);
        
        boolean foundNumber = numberMatcher.find();
        boolean foundOperator = operatorMatcher.find();
        boolean foundParameter = parameterMatcher.find();
        
        int index = 0;
        
        while (index < functionString.length()) {
            
            if (foundNumber && numberMatcher.start() == index) {
                
                element = new FunctionElement(numberMatcher.group(), Type.NUMBER);
                function.getElements().add(element);
                
                index = numberMatcher.start() + numberMatcher.group().length();
                foundNumber = numberMatcher.find();
                
            } else if (foundOperator && operatorMatcher.start() == index) {
                
                element = new FunctionElement(operatorMatcher.group(), Type.OPERATOR);
                function.getElements().add(element);
                
                index = operatorMatcher.start() + operatorMatcher.group().length();
                foundOperator = operatorMatcher.find();
                
            } else if (foundParameter && parameterMatcher.start() == index ) {
                
                element = new FunctionElement(parameterMatcher.group(), Type.PARAMETER);
                function.getElements().add(element);
                function.getParameterIds().add(element.get());
                
                index = parameterMatcher.start() + parameterMatcher.group().length();
                foundParameter = parameterMatcher.find();
                
            } else {
                throw new IOException("Unrecognized symbol at position " + index + "! [" + functionString.toCharArray()[index] + "]");
            }
        }
        return function;
    }
}
