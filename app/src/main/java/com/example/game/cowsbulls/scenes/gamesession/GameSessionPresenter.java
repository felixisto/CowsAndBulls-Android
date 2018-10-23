package com.example.game.cowsbulls.scenes.gamesession;

import android.util.Log;

import com.example.game.cowsbulls.game.GameSession;
import com.example.game.cowsbulls.game.GameTurn;
import com.example.game.cowsbulls.network.Communicator;
import com.example.game.cowsbulls.network.CommunicatorInitialConnection;
import com.example.game.cowsbulls.network.CommunicatorObserver;
import com.example.game.cowsbulls.shared.SharedResources;

import java.util.ArrayList;

public class GameSessionPresenter implements GameSessionContract.Presenter, CommunicatorObserver
{
    private GameSessionContract.View view;
    private Communicator communicator;
    private CommunicatorInitialConnection initialConnection;
    
    private boolean isActive;
    
    private final int guessWordLength;
    private GameTurn turnToGo;
    
    private String guessWordPicked;
    private int turnValue;
    
    private boolean opponentHasPickedGuessWord;
    private int opponentPickedTurn;
    
    public GameSessionPresenter(GameSessionContract.View view, Communicator communicator, CommunicatorInitialConnection initialConnection, int guessWordLength, String turnToGo)
    {
        this.view = view;
        
        this.communicator = communicator;
    
        if (this.communicator == null)
        {
            this.communicator = SharedResources.getShared().getCommunicator();
        }
        
        this.communicator.attachObserver(this, Integer.toHexString(System.identityHashCode(this)));
        
        this.initialConnection = initialConnection;
    
        if (this.initialConnection == null)
        {
            this.initialConnection = SharedResources.getShared().getCommunicatorInitialConnection();
        }
    
        this.view.setPresenter(this);
        
        this.isActive = false;
        
        this.guessWordLength = guessWordLength;
        this.turnToGo = GameTurn.create(turnToGo);
        
        if (this.turnToGo == null)
        {
            this.turnToGo = GameTurn.FIRST;
        }
        
        this.guessWordPicked = "";
        this.turnValue = 0;
        this.opponentHasPickedGuessWord = false;
        this.opponentPickedTurn = 0;
    }
    
    public static boolean guessWordIsValid(String guessWord)
    {
        ArrayList<Integer> symbols = new ArrayList<>();
    
        for (int e = 0; e < guessWord.length(); e++)
        {
            char c = guessWord.charAt(e);
            
            Integer integerValue = new Integer(c);
            
            if (symbols.contains(integerValue))
            {
                return false;
            }
            else
            {
                symbols.add(integerValue);
            }
        }
        
        return true;
    }
    
    // - Presenter interface -
    
    @Override
    public void start()
    {
        Log.v("GameSessionPresenter", "Start");
        
        isActive = true;
        
        String otherPlayerName = initialConnection != null ? initialConnection.otherPlayerName : "Unknown";
        String otherPlayerAddress = initialConnection != null ? initialConnection.otherPlayerAddress : "?";
        
        view.updateConnectionData(otherPlayerName, otherPlayerAddress);
        
        view.updateEnterXCharacterWord(guessWordLength);
    }
    
    @Override
    public void goToGameplayScreen(String guessWord)
    {
        if (!isActive)
        {
            return;
        }
        
        if (guessWord.length() != guessWordLength)
        {
            return;
        }
        
        // Guess word must not have repeating symbols
        if (!GameSessionPresenter.guessWordIsValid(guessWord))
        {
            view.invalidGuessWord("Guess word must be made of non-repeating digit characters");
            return;
        }
        
        guessWordPicked = guessWord;
    
        // If opponent has also picked word, then lets play
        if (opponentHasPickedGuessWord)
        {
            Log.v("GameSessionPresenter", "PickWordPresenter play with guess word " + guessWord);
            
            GameSession gameSession = new GameSession(turnToGo.equals(GameTurn.FIRST), guessWord);
            SharedResources.getShared().setGameSession(gameSession);
            
            view.goToGameplayScreen();
        }
        else
        {
            Log.v("GameSessionPresenter", "PickWordPresenter picked guess word " + guessWord);
        }
        
        communicator.sendAlertPickedGuessWordMessage();
    }
    
    @Override
    public void leave()
    {
        Log.v("GameSessionPresenter", "Leave game");
        
        isActive = false;
        
        communicator.detachObserver(Integer.toHexString(System.identityHashCode(this)));
        
        communicator.stop();
        
        view.leave();
    }
    
    @Override
    public void prepareForNewGame()
    {
        this.isActive = false;
        
        this.guessWordPicked = "";
        this.turnValue = 0;
        
        this.opponentHasPickedGuessWord = false;
        this.opponentPickedTurn = 0;
        
        this.turnToGo = this.turnToGo.nextTurn();
    }
    
    // - CommunicatorObserver interface -
    
    @Override
    public void lostConnectionAttemptingToReconnect()
    {
        Log.v("GameSessionPresenter", "Lost connection attempting to reconnect");
        
        view.lostConnectionAttemptingToReconnect();
    }
    
    @Override
    public void reconnect()
    {
        Log.v("GameSessionPresenter", "Reconnected");
        
        view.reconnected();
    }
    
    @Override
    public void opponentQuit()
    {
        Log.v("GameSessionPresenter", "Opponent quit");
        
        SharedResources.getShared().setError("Opponent quit!");
        
        view.opponentQuit();
    }
    
    @Override
    public void disconnect(String error)
    {
        Log.v("GameSessionPresenter", "Disconnected, error: " + error);
        
        SharedResources.getShared().setError(error);
        
        view.disconnected();
    }
    
    @Override
    public void beginConnect() {}
    
    @Override
    public void formallyConnected(CommunicatorInitialConnection data) {}
    
    @Override
    public void failedToConnect() {}
    
    @Override
    public void timeout() {}
    
    @Override
    public void opponentPickedPlaySetup(int guessWordLength, String turnToGo) {}
    
    @Override
    public void opponentPickedPlaySession() 
    {
        if (!isActive)
        {
            return;
        }
        
        // Skip if opponent has picked guess word
        if (opponentHasPickedGuessWord)
        {
            return;
        }
        
        Log.v("GameSessionPresenter", "Opponent picked guess word!");
        
        opponentHasPickedGuessWord = true;
        opponentPickedTurn = turnValue;
        
        view.setOpponentStatus("Opponent picked a guess word!");
        
        // If we have picked word too, play
        if (guessWordPicked.length() > 0)
        {
            goToGameplayScreen(guessWordPicked);
        }
    }
    
    @Override
    public void opponentGuess(String guess) {}
    
    @Override
    public void incorrectGuessResponse(String response)  {}
    
    @Override
    public void correctGuessResponse() {}
}
