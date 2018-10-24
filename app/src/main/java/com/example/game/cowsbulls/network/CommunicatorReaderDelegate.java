package com.example.game.cowsbulls.network;

public interface CommunicatorReaderDelegate
{
    void ping();
    
    void greetingsMessageReceived(String parameter);
    void messageReceived(String command, String parameter);
}
