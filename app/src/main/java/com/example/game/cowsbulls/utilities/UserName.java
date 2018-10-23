package com.example.game.cowsbulls.utilities;

import android.os.Build;

public class UserName
{
    static public String get()
    {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String name;
        
        if (model.startsWith(manufacturer)) 
        {
            name = capitalize(model);
        } 
        else 
        {
            name = capitalize(manufacturer) + " " + model;
        }
        
        if (name.length() > 9)
        {
            return name.substring(0, 9);
        }
        
        return name;
    }
    
    static private String capitalize(String s) 
    {
        if (s == null || s.length() == 0)
        {
            return "";
        }
        
        char first = s.charAt(0);
        
        if (Character.isUpperCase(first)) 
        {
            return s;
        } 
        else
        {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
