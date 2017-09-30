/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import java.util.List;
import main.TestFXBase;
import org.junit.Test;

/**
 *
 * @author PR
 */
public class ImportExportTests extends TestFXBase
{
    int placeCount = 5;
    int transitionCount = 3;
    
    @Test
    public void XmlExportImport() {
        

        List<IGraphNode> places = CreatePlaces(placeCount);
        List<IGraphNode> transitions = CreateTransitions(transitionCount);
        
//        ConnectNodes(places, transitions);
    }
}
