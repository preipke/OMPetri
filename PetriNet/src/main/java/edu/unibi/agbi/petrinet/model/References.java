/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.unibi.agbi.petrinet.entity.IElement;

/**
 * Container for references between IElement data objects and filter variables
 * used throughout OpenModelica's simulation. 
 * @author PR
 */
public class References {
    
    private final Map<String,IElement> filterElementReferences;
    private final Map<IElement,List<String>> elementFilterReferences;
    
    public References() {
        filterElementReferences = new HashMap();
        elementFilterReferences = new HashMap();
    }
    
    /**
     * Adds and maps the given IElement to the given String.
     * @param filter String representing a filter variable
     * @param element IElement data object
     * @throws IOException 
     */
    public void addFilterReference(String filter, IElement element) throws IOException {
        if (filterElementReferences.containsKey(filter)) {
            throw new IOException("Filter -> Element reference already exists! Don't overwrite!");
        }
        filterElementReferences.put(filter, element);
    }
    
    /**
     * Adds and maps the given String to the given IElement.
     * @param element IElement data object
     * @param filter String representing a filter variable
     */
    public void addElementReference(IElement element, String filter) {
        if (!elementFilterReferences.containsKey(element)) {
            elementFilterReferences.put(element, new ArrayList());
        }
        elementFilterReferences.get(element).add(filter);
    }
    
    public Map<String,IElement> getFilterElementReferences() {
        return filterElementReferences;
    }
    
    public Map<IElement,List<String>> getElementFilterReferences() {
        return elementFilterReferences;
    }
}
