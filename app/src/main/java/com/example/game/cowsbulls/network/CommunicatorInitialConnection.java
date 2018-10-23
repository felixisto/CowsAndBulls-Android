package com.example.game.cowsbulls.network;

import java.util.Date;

public class CommunicatorInitialConnection 
{
    public final Date dateConnected;
    public final String otherPlayerAddress;
    public final String otherPlayerName;
    
    public CommunicatorInitialConnection(Date dateConnected, String otherPlayerAddress, String otherPlayerName)
    {
        this.dateConnected = dateConnected;
        this.otherPlayerAddress = otherPlayerAddress;
        this.otherPlayerName = otherPlayerName;
    }
}
