/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.data.dao;

import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
@Repository
public class SelectionDao
{
    private List<IGraphElement> selectedElements;
    private List<IGraphElement> highlightedElements;
    
    public SelectionDao() {
        selectedElements = new ArrayList();
        highlightedElements = new ArrayList();
    }
    
    public synchronized void addSelection(IGraphElement element) {
        selectedElements.add(element);
    }
    
    public synchronized void addHighlight(IGraphElement element) {
        highlightedElements.add(element);
    }
    
    public synchronized List<IGraphElement> getSelectedElements() {
        return selectedElements;
    }
    
    public synchronized List<IGraphElement> getHightlightedElements() {
        return highlightedElements;
    }
    
    public synchronized IGraphElement[] getSelectedElementsArray() {
        IGraphElement[] elements = new IGraphElement[selectedElements.size()];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = selectedElements.get(i);
        }
        return elements;
    }
    
    public synchronized IGraphElement[] getHighlightedElementsArray() {
        IGraphElement[] elements = new IGraphElement[highlightedElements.size()];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = highlightedElements.get(i);
        }
        return elements;
    }
    
    public synchronized boolean removeSelection(IGraphElement element) {
        return selectedElements.remove(element);
    }
    
    public synchronized boolean removeHighlight(IGraphElement element) {
        return highlightedElements.remove(element);
    }
    
    public synchronized void clear() {
        selectedElements = new ArrayList();
        highlightedElements = new ArrayList();
    }
}
