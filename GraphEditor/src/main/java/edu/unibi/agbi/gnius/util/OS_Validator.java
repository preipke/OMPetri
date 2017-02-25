/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.util;

/**
 *
 * @author PR
 */
public final class OS_Validator
{
    private final static String OS = System.getProperty("os.name").toLowerCase();

    public static String getOS() {
        return OS;
    }

    public static boolean isOsWindows() {
        return (OS.contains("win"));
    }

    public static boolean isOsMac() {
        return (OS.contains("mac"));
    }

    public static boolean isOsUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }

    public static boolean isOsSolaris() {
        return (OS.contains("sunos"));
    }

}
