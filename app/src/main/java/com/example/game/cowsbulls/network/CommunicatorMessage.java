package com.example.game.cowsbulls.network;

import android.util.Log;

import java.util.Arrays;

public class CommunicatorMessage
{
    static final int MESSAGE_LENGTH = 50;
    static final int MESSAGE_COMMAND_LENGTH = 5;
    static final int MESSAGE_PARAMETER_LENGTH = MESSAGE_LENGTH - MESSAGE_COMMAND_LENGTH;
    static final char FILLER_CHARACTER = '\t';
    
    final int commandLength;
    final int parameterLength;
    private StringBuilder data;
    
    public static CommunicatorMessage createReadMessage()
    {
        return new CommunicatorMessage(MESSAGE_COMMAND_LENGTH, MESSAGE_PARAMETER_LENGTH);
    }
    
    public static CommunicatorMessage createWriteMessage(String command)
    {
        if (command.length() != MESSAGE_COMMAND_LENGTH)
        {
            return null;
        }
        
        CommunicatorMessage m = new CommunicatorMessage(MESSAGE_PARAMETER_LENGTH, command, "");
        
        m.fillMessage();
        
        return m;
    }
    
    public static CommunicatorMessage createWriteMessage(String command, String parameter)
    {
        if (command.length() != MESSAGE_COMMAND_LENGTH)
        {
            return null;
        }
        
        CommunicatorMessage m = new CommunicatorMessage(MESSAGE_PARAMETER_LENGTH, command, parameter);
        
        m.fillMessage();
        
        return m;
    }
    
    public static CommunicatorMessage createWriteMessage(String command, String parameter1, String parameter2)
    {
        if (command.length() != MESSAGE_COMMAND_LENGTH)
        {
            return null;
        }
        
        CommunicatorMessage m = new CommunicatorMessage(MESSAGE_PARAMETER_LENGTH, command, parameter1 + " " + parameter2);
        
        m.fillMessage();
        
        return m;
    }
    
    private CommunicatorMessage(int commandLength, int parameterLength)
    {
        this.commandLength = commandLength > 0 ? commandLength : 1;
        this.parameterLength = parameterLength > 0 ? parameterLength : 1;
        
        this.data = new StringBuilder();
    }
    
    private CommunicatorMessage(int parameterLength, String command, String parameter)
    {
        if (parameterLength <= 0)
        {
            parameterLength = 0;
            parameter = "";
        }
        
        this.commandLength = command.length();
        this.parameterLength = parameterLength > 0 ? parameterLength : 1;
        
        this.data = new StringBuilder();
        
        append(command);
        append(parameter);
    }
    
    public boolean isFullyWritten()
    {
        return data.toString().getBytes().length >= MESSAGE_LENGTH;
    }
    
    public String getCommand()
    {
        return data.substring(0, commandLength);
    }
    
    private String getParameterWithFillerChars()
    {
        byte[] dataAsByteArray = data.toString().getBytes();
        byte[] byteArray = new byte[dataAsByteArray.length];
        int byteArrayCount = 0;
    
        for (int i = 0; i < dataAsByteArray.length; i++)
        {
            if (i < commandLength)
            {
                continue;
            }
        
            byteArray[byteArrayCount] = dataAsByteArray[i];
            byteArrayCount++;
        
            if (i == MESSAGE_LENGTH)
            {
                break;
            }
        }
        
        return new String(Arrays.copyOf(byteArray, byteArrayCount));
    }
    
    public String getParameter()
    {
        return getParameterWithFillerChars().replaceAll(String.valueOf(FILLER_CHARACTER), "");
    }
    
    public String getData()
    {
        return data.toString();
    }
    
    public int getDataBytesCount()
    {
        return data.toString().getBytes().length;
    }
    
    public void clearFirstFilledMessage()
    {
        if (getDataBytesCount() > MESSAGE_LENGTH)
        {
            String command = getCommand();
            String parameter = getParameterWithFillerChars();
            
            int beginIndex = command.length() + parameter.length();
            
            String newDataValue = data.toString().substring(beginIndex);
            
            data = new StringBuilder();
            data.append(newDataValue);
        }
        else
        {
            data = new StringBuilder();
        }
    }
    
    public void append(String string)
    {
        data.append(string);
    }
    
    public void fillMessage()
    {
        while (!isFullyWritten())
        {
            data.append(FILLER_CHARACTER);
        }
    }
}
