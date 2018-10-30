package com.example.game.cowsbulls.network;

public class CommunicatorMessage
{
    static final int MESSAGE_LENGTH = 50;
    static final int MESSAGE_COMMAND_LENGTH = 5;
    static final int MESSAGE_PARAMETER_LENGTH = MESSAGE_LENGTH-MESSAGE_COMMAND_LENGTH;
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
        
        this.data.append(command);
        
        if (parameter.length() > parameterLength)
        {
            parameter = parameter.substring(0, parameterLength);
        }
        
        this.data.append(parameter);
    }
    
    public boolean isFullyWritten()
    {
        return data.length() == MESSAGE_LENGTH;
    }
    
    public String getCommand()
    {
        return data.substring(0, commandLength);
    }
    
    public String getParameter()
    {
        String parameter = data.substring(commandLength);
        
        return parameter.replaceAll(String.valueOf(FILLER_CHARACTER), "");
    }
    
    public String getData()
    {
        return data.toString();
    }
    
    public void clear()
    {
        data = new StringBuilder();
    }
    
    public void append(String string)
    {
        for (int e = 0; e < string.length(); e++)
        {
            data.append(string.charAt(e));
            
            if (isFullyWritten())
            {
                return;
            }
        }
    }
    
    public void fillMessage()
    {
        while (!isFullyWritten())
        {
            data.append(FILLER_CHARACTER);
        }
    }
}
