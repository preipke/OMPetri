/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.mode.impl;

import edu.unibi.agbi.gnius.business.mode.exception.EditorModeLockException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author PR
 */
abstract class Mode
{
    private static boolean isModeLocked = false;
    private static List<Mode> modes = new ArrayList();
    
    private final BooleanProperty isEnabled;
    private final String modeName;
    
    public Mode(String name) {
        isEnabled = new SimpleBooleanProperty(false);
        modeName = name;
    }
    
    public boolean isEnabled() {
        return isEnabled.get();
    }
    
    public synchronized void setEnabled() throws EditorModeLockException {
        if (!isEnabled.get()) {
            if (isModeLocked) {
                throw new EditorModeLockException("Mode is locked!");
            }
            isModeLocked = true;
            isEnabled.set(isModeLocked);
        }
    }
    
    public synchronized void setDisabled() {
        if (isEnabled.get()) {
            isEnabled.set(false);
            isModeLocked = false;
        }
    }
    
    public String getName() {
        return modeName;
    }
}
