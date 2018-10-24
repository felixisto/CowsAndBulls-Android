package com.example.game.cowsbulls.shared;

import com.example.game.cowsbulls.game.GameSession;
import com.example.game.cowsbulls.network.Communicator;
import com.example.game.cowsbulls.network.CommunicatorInitialConnection;

public class SharedResources
{
    static private SharedResources singleton;
    
    static public SharedResources getShared()
    {
        if (singleton == null)
        {
            singleton = new SharedResources();
        }
        
        return singleton;
    }
    
    private SharedResources()
    {
        
    }
    
    private String error = "";
    
    public void setError(String error)
    {
        this.error = error;
        
        if (error == null)
        {
            this.error = "";
        }
    }
    
    public String getError() {return error;}
    
    public String getErrorAndClear()
    {
        String temp = error;
        
        error = "";
        
        return temp;
    }
    
    private Communicator communicator;
    
    public void setCommunicator(Communicator communicator)
    {
        this.communicator = communicator;
    }
    
    public Communicator getCommunicator() {return communicator;}
    
    private CommunicatorInitialConnection communicatorInitialConnection;
    
    public void setCommunicatorInitialConnection(CommunicatorInitialConnection data)
    {
        this.communicatorInitialConnection = data;
    }
    
    public CommunicatorInitialConnection getCommunicatorInitialConnection() {return communicatorInitialConnection;}
    
    private GameSession gameSession;
    
    public void setGameSession(GameSession gameSession)
    {
        this.gameSession = gameSession;
    }
    
    public GameSession getGameSession() {return gameSession;}
}
