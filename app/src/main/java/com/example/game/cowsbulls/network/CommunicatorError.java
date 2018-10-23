package com.example.game.cowsbulls.network;

public class CommunicatorError extends Exception
{
    public static final CommunicatorError INVALID_IP_ADDRESS = new CommunicatorError("INVALID IP ADDRESS");
    
    public CommunicatorError(String description)
    {
        super(description);
    }
}
