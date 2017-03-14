/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import edu.unibi.agbi.petrinet.entity.IPN_Element;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PR
 */
public class References {
    
    private final Map<String,IPN_Element> filterElementReferences;
    private final Map<IPN_Element,List<String>> elementFilterReferences;
    
    public References() {
        filterElementReferences = new HashMap();
        elementFilterReferences = new HashMap();
    }
    
    public void addFilterReference(String filter, IPN_Element element) throws IOException {
        if (filterElementReferences.containsKey(filter)) {
            throw new IOException("Filter -> Element reference already exists! Don't overwrite!");
        }
        filterElementReferences.put(filter, element);
    }
    
    public void addElementReference(IPN_Element element, String filter) {
        if (!elementFilterReferences.containsKey(element)) {
            elementFilterReferences.put(element, new ArrayList());
        }
        elementFilterReferences.get(element).add(filter);
    }
    
    public Map<String,IPN_Element> getFilterElementReferences() {
        return filterElementReferences;
    }
    
    public Map<IPN_Element,List<String>> getElementFilterReferences() {
        return elementFilterReferences;
    }
}
