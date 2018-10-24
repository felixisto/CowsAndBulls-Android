package com.example.game.cowsbulls.scenes.join;

import android.util.Log;

import com.example.game.cowsbulls.game.GameConstants;
import com.example.game.cowsbulls.game.GameTurn;
import com.example.game.cowsbulls.network.CommunicatorClient;
import com.example.game.cowsbulls.network.CommunicatorInitialConnection;
import com.example.game.cowsbulls.network.CommunicatorObserver;
import com.example.game.cowsbulls.shared.SharedResources;

public class JoinPresenter implements JoinContract.Presenter, CommunicatorObserver
{
    private JoinContract.View view;
    private CommunicatorClient communicator;
    
    public JoinPresenter(JoinContract.View view)
    {
        this.view = view;
        
        this.view.setPresenter(this);
    }
    
    // - Presenter interface -
    
    @Override
    public void start()
    {
        Log.v("JoinPresenter", "Start");
    }
    
    @Override
    public void tryConnectingToHost(String host)
    {
        Log.v("JoinPresenter", "Trying to connect to host '" + host + "'");
        
        communicator = new CommunicatorClient();
        communicator.start(host);
        communicator.attachObserver(this, Integer.toHexString(System.identityHashCode(this)));
        
        // Set the global shared communicator for the application
        SharedResources.getShared().setCommunicator(communicator);
    }
    
    @Override
    public void leave()
    {
        Log.v("JoinPresenter", "Stop");
    
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
        Log.v("JoinPresenter", "Connected with host! Attempting to start formal connection!");
        
        view.beginConnect();
    }
    
    @Override
    public void formallyConnected(CommunicatorInitialConnection data)
    {
        Log.v("JoinPresenter", "Connected formally with host! Going to game setup screen");
        
        // Set the global shared communicator initial data for the application
        SharedResources.getShared().setCommunicatorInitialConnection(data);
        
        // Go to next scene
        view.goToGameSetupScreen();
    }
    
    @Override
    public void failedToConnect()
    {
        Log.v("JoinPresenter", "Failed to connect!");
        
        view.connectionFailure("Could not find player");
    }
    
    @Override
    public void timeout()
    {
        view.timeout();
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
