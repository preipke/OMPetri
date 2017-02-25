/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class ResultsService
{
    private HashMap<String,ArrayList<Double>> results;
    
    public void addResult(String id, Double value) {
        if (results.get(id) == null) {
            results.put(id, new ArrayList());
        }
        results.get(id).add(value);
    }
}
