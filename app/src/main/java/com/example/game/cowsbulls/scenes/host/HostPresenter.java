package com.example.game.cowsbulls.scenes.host;

import android.util.Log;

import com.example.game.cowsbulls.network.CommunicatorHost;
import com.example.game.cowsbulls.network.CommunicatorInitialConnection;
import com.example.game.cowsbulls.network.CommunicatorObserver;
import com.example.game.cowsbulls.shared.SharedResources;

public class HostPresenter implements HostContract.Presenter, CommunicatorObserver
{
    private HostContract.View view;
    private CommunicatorHost communicator;
    
    public HostPresenter(HostContract.View view)
    {
        this.view = view;
        
        this.view.setPresenter(this);
        
        this.communicator = new CommunicatorHost();
        
        communicator.attachObserver(this, Integer.toHexString(System.identityHashCode(this)));
        
        // Set the global shared communicator for the application
        SharedResources.getShared().setCommunicator(communicator);
    }
    
    // - Presenter interface -
    
    @Override
    public void start()
    {
        Log.v("HostPresenter", "Start");
    }
    
    @Override
    public void goToGameSetupScreen()
    {
        view.goToGameSetupScreen();
    }
    
    @Override
    public void leave()
    {
        Log.v("HostPresenter", "Stop");
    
        communicator.detachObserver(Integer.toHexString(System.identityHashCode(this)));
        
        communicator.destroy();
        
        view.goBack();
    }
    
    // - CommunicatorObserver interface -
    
    @Override
    public void lostConnectionAttemptingToReconnect()
    {
        
    }
    
    @Override
    public void reconnect()
    {
        
    }
    
    @Override
    public void opponentQuit()
    {
        
    }
    
    @Override
    public void disconnect(String error)
    {
        
    }
    
    @Override
    public void beginConnect()
    {
        
    }
    
    @Override
    public void formallyConnected(CommunicatorInitialConnection data)
    {
        
    }
    
    @Override
    public void failedToConnect()
    {
        
    }
    
    @Override
    public void timeout() 
    {
        
    }
    
    @Override
    public void opponentPickedPlaySetup(int guessWordLength, String turnToGo) {}
    
    @Override
    public void opponentPickedPlaySession() {}
    
    @Override
    public void opponentGuess(String guess) {}
    
    @Override
    public void incorrectGuessResponse(String response)  {}
    
    @Override
    public void correctGuessResponse() {}
}
