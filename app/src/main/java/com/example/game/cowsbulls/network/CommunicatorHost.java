package com.example.game.cowsbulls.network;

import android.util.Log;

import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CommunicatorHost implements Communicator, CommunicatorReaderDelegate, CommunicatorWriterDelegate
{
    private HashMap<String, CommunicatorObserverValue> observers;
    private CommunicatorReader reader;
    private CommunicatorWriter writer;
    private Socket socket;
    
    private boolean isConnectedToClient;
    
    private Date lastPingFromClient;
    private boolean lastPingRetryingToConnect;
    
    public CommunicatorHost()
    {
        this.observers = new HashMap<String, CommunicatorObserverValue>();
        this.isConnectedToClient = false;
        this.lastPingFromClient = null;
        this.lastPingRetryingToConnect = false;
    }
    
    public void reset()
    {
        if (reader != null)
        {
            reader.stop();
        }
        
        if (writer != null)
        {
            writer.stop();
        }
        
        socket = null;
        reader = null;
        writer = null;
        
        isConnectedToClient = false;
        
        lastPingFromClient = null;
        lastPingRetryingToConnect = false;
    }
    
    public void destroy()
    {
        reset();
    
        observers.clear();
    }
    
    public void start()
    {
        
    }
    
    // - Communicator interface -
    
    @Override
    public void attachObserver(CommunicatorObserver observer, final String key)
    {
        observers.put(key, new CommunicatorObserverValue(observer));
    }
    
    @Override
    public void detachObserver(String key)
    {
        observers.remove(key);
    }
    
    @Override
    public void stop()
    {
        sendQuitMessage();
        
        reset();
    }
    
    @Override
    public void sendQuitMessage()
    {
        Log.v("CommunicatorHost", "Sending quit message to client");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.QUIT);
        writer.send(message.getData());
    }
    
    @Override
    public void sendPlaySetupMessage(int guessLength, String turnToGo)
    {
        Log.v("CommunicatorHost", "Sending play setup message to client");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.PLAYSETUP, String.valueOf(guessLength), turnToGo);
        writer.send(message.getData());
    }
    
    @Override
    public void sendAlertPickedGuessWordMessage()
    {
        Log.v("CommunicatorHost", "Sending alert picked guess word message to client");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.READYTOPLAY);
        writer.send(message.getData());
    }
    
    @Override
    public void sendGuessMessage(String guess)
    {
        Log.v("CommunicatorHost", "Sending guess message to client");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.GAMEGUESS, guess);
        writer.send(message.getData());
    }
    
    @Override
    public void sendGuessIncorrectResponseMessage(String response)
    {
        Log.v("CommunicatorHost", "Sending guess response message to client");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.GAMEGUESSRESPONSE, response);
        writer.send(message.getData());
    }
    
    @Override
    public void sendGuessCorrectResponseMessage()
    {
        Log.v("CommunicatorHost", "Sending guess correct message to client");
        
        CommunicatorMessage message = CommunicatorMessage.createWriteMessage(CommunicatorCommands.GAMECORRECTGUESS);
        writer.send(message.getData());
    }
    
    // - CommunicatorReaderDelegate interface -
    
    @Override
    public void ping()
    {
        
    }
    
    @Override
    public void greetingsMessageReceived(String parameter)
    {
        
    }
    
    @Override
    public void messageReceived(String command, String parameter)
    {
        
    }
    
    // - CommunicatorWriterDelegate interface -
    
    @Override
    public void pingRefresh()
    {
        if (!isConnectedToClient)
        {
            return;
        }
        
        
    }
}
