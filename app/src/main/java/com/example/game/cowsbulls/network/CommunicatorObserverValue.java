package com.example.game.cowsbulls.network;

public class CommunicatorObserverValue
{
    CommunicatorObserver observer;
    
    CommunicatorObserverValue(CommunicatorObserver observer)
    {
        this.observer = observer;
    }
    
    public CommunicatorObserver value()
    {
        return observer;
    }
}
