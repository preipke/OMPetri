/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.util;

import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Function.Type;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
    
    private final Map<String,Function> subfunctions = new HashMap();
    private int subfunctionsCount = 0;
    
    public FunctionBuilder() throws IOException {
        properties = new Properties();
        properties.load(FunctionBuilder.class.getResourceAsStream(propertiesPath));
    }
    
    public String getNumberRegex() {
        return properties.getProperty("regex.function.number");
    }
    
    public String getOperatorRegex() {
        return properties.getProperty("regex.function.operator");
    }
    
    public String getOperatorExtRegex() {
        return properties.getProperty("regex.function.operator.ext");
    }
    
    public String getParameterRegex() {
        return properties.getProperty("regex.function.parameter");
    }
    
    public String getSubfunctionRegex() {
        return properties.getProperty("regex.function.subfunction");
    }
    
    public Function build(String functionString, boolean isSubfunction) throws IOException {
        
        Function function = new Function(Type.FUNCTION);
        Function functionElement;
        
        Pattern patternNumber, patternOperator, patternParamater, patternSubfunction;
        Matcher numberMatcher, operatorMatcher, parameterMatcher, subfunctionMatcher;
        boolean foundNumber, foundOperator, foundParameter, foundSubfunction;
        
        patternNumber = Pattern.compile(getNumberRegex());
        if (isSubfunction) {
            patternOperator = Pattern.compile(getOperatorExtRegex());
        } else {
            patternOperator = Pattern.compile(getOperatorRegex());
        }
        patternParamater = Pattern.compile(getParameterRegex());
        patternSubfunction = Pattern.compile(getSubfunctionRegex());
        
        if (functionString != null) {
            
            functionString = functionString.replace(" ", "");
            functionString = substitute(functionString);

            numberMatcher = patternNumber.matcher(functionString);
            operatorMatcher = patternOperator.matcher(functionString);
            parameterMatcher = patternParamater.matcher(functionString);
            subfunctionMatcher = patternSubfunction.matcher(functionString);

            foundNumber = numberMatcher.find();
            foundOperator = operatorMatcher.find();
            foundParameter = parameterMatcher.find();
            foundSubfunction = subfunctionMatcher.find();

            int index = 0;

            while (index < functionString.length()) {

                if (foundNumber && numberMatcher.start() < index) { // detects number in ref param IDs - has to be skipped

                    foundNumber = numberMatcher.find();

                } else if (foundNumber && numberMatcher.start() == index) {

                    functionElement = new Function(Type.NUMBER);
                    functionElement.setValue(numberMatcher.group());
                    function.addElement(functionElement);

                    index = numberMatcher.start() + numberMatcher.group().length();
                    foundNumber = numberMatcher.find();

                } else if (foundOperator && operatorMatcher.start() == index) {

                    functionElement = new Function(Type.OPERATOR);
                    functionElement.setValue(operatorMatcher.group());
                    function.addElement(functionElement);

                    index = operatorMatcher.start() + operatorMatcher.group().length();
                    foundOperator = operatorMatcher.find();

                } else if (foundParameter && parameterMatcher.start() == index) {

                    functionElement = new Function(Type.PARAMETER);
                    functionElement.setValue(parameterMatcher.group());
                    function.addElement(functionElement);

                    index = parameterMatcher.start() + parameterMatcher.group().length();
                    foundParameter = parameterMatcher.find();

                } else if (foundSubfunction && subfunctionMatcher.start() == index) {

                    Function subfunction = subfunctions.get(subfunctionMatcher.group());
                    function.addElement(subfunction);

                    index = subfunctionMatcher.start() + subfunctionMatcher.group().length();
                    foundSubfunction = subfunctionMatcher.find();

                } else {
                    
                    throw new IOException("Unrecognized symbol '" + functionString.toCharArray()[index] + "' at position " + index);
                }
            }
        }
        return function;
    }
    
    private String substitute(String functionString) throws IOException {
        
        String min = "min(";
        String max = "max(";
        
        String subFunctionString, subFunctionId;
        Function subFunction;
        
        while (functionString.contains(max) | functionString.contains(min)) {
            
            if (functionString.contains(min)) {
                subFunctionString = getSubFunction(functionString.substring(functionString.indexOf(min) + min.length()));
                subFunction = build(subFunctionString, true);
                subFunction.addElement(0, new Function(min, Type.OPERATOR));
                subFunctionString = min + subFunctionString;
            } else {
                subFunctionString = getSubFunction(functionString.substring(functionString.indexOf(max) + max.length()));
                subFunction = build(subFunctionString, true);
                subFunction.addElement(0, new Function(max, Type.OPERATOR));
                subFunctionString = max + subFunctionString;
            }
            
            subFunctionId = "#" + subfunctionsCount++;
            subfunctions.put(subFunctionId, subFunction);
            subFunctionString = subFunctionString.replace("(", "\\("); // avoid regex clash
            subFunctionString = subFunctionString.replace(")", "\\)");
            functionString = functionString.replaceFirst(subFunctionString, subFunctionId);
        }
        
        return functionString;
    }
    
    private String getSubFunction(String subfunction) {
        
        Pattern patternBracketOpen = Pattern.compile("\\(");
        Pattern patternBracketClose = Pattern.compile("\\)");
        Matcher openMatcher, closeMatcher;
        boolean foundOpen, foundClosed;
        int index, count;

        openMatcher = patternBracketOpen.matcher(subfunction);
        closeMatcher = patternBracketClose.matcher(subfunction);

        foundOpen = openMatcher.find();
        foundClosed = closeMatcher.find();

        index = 3;
        count = 1; // open brackets count

        while (index < subfunction.length()) {
            if (foundOpen && openMatcher.start() == index) {
                count++;
            } else if (foundClosed && closeMatcher.start() == index) {
                count--;
            }
            index++;
            if (count == 0) {
                break;
            }
        }
        return subfunction.substring(0, index);
    }
}
