package com.example.game.cowsbulls.scenes.gameplay;

import android.support.annotation.NonNull;
import android.util.Log;
import java.util.ArrayList;
import static com.google.common.base.Preconditions.checkNotNull;

import com.example.game.cowsbulls.game.GameError;
import com.example.game.cowsbulls.game.GameSession;
import com.example.game.cowsbulls.game.GuessResult;
import com.example.game.cowsbulls.network.Communicator;
import com.example.game.cowsbulls.network.CommunicatorInitialConnection;
import com.example.game.cowsbulls.network.CommunicatorObserver;
import com.example.game.cowsbulls.shared.SharedResources;

public class GameplayPresenter implements GameplayContract.Presenter, CommunicatorObserver
{
    private GameplayContract.View view;
    private Communicator communicator;
    private CommunicatorInitialConnection initialConnection;
    private GameSession gameSession;
    
    public GameplayPresenter(@NonNull GameplayContract.View view, @NonNull Communicator communicator, @NonNull CommunicatorInitialConnection initialConnection, @NonNull GameSession gameSession)
    {
        this.view = checkNotNull(view, "View cannot be null");
        
        this.communicator = checkNotNull(communicator, "Communicator cannot be null");
        
        this.communicator.attachObserver(this, Integer.toHexString(System.identityHashCode(this)));
    
        this.initialConnection = checkNotNull(initialConnection, "InitialConnection cannot be null");
    
        this.view.setPresenter(this);
        
        this.gameSession = checkNotNull(gameSession, "GameSession cannot be null");
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
        Log.v("GameplayPresenter", "Start");
        
        view.setupUI(gameSession.guessWordNumberOfCharacters, gameSession.guessWordAsString, gameSession.isMyTurn());
        
        String otherPlayerName = initialConnection != null ? initialConnection.otherPlayerName : "Unknown";
        String otherPlayerAddress = initialConnection != null ? initialConnection.otherPlayerAddress : "?";
        
        view.updateConnectionData(otherPlayerName, otherPlayerAddress);
    }
    
    @Override
    public void leave()
    {
        Log.v("GameplayPresenter", "Leave game");
        
        communicator.detachObserver(Integer.toHexString(System.identityHashCode(this)));
        
        communicator.stop();
        
        view.leave();
    }
    
    @Override
    public void goBack()
    {
        Log.v("GameplayPresenter", "Go back, play another game");
        
        communicator.sendGameNextMessage();
        
        communicator.detachObserver(Integer.toHexString(System.identityHashCode(this)));
        
        view.goBack();
    }
    
    @Override
    public void guess(String guess)
    {
        if (GameplayPresenter.guessWordIsValid(guess))
        {
            Log.v("GameplayPresenter", "Sending guess to opponent " + guess);
            
            gameSession.guessAttempt(guess);
            
            communicator.sendGuessMessage(guess);
        }
        else
        {
            view.invalidGuessAttempt("Guess word must be made of non-repeating digit characters");
        }
    }
    
    // - CommunicatorObserver interface -
    
    @Override
    public void lostConnectionAttemptingToReconnect()
    {
        Log.v("GameplayPresenter", "Lost connection attempting to reconnect");
        
        view.lostConnectionAttemptingToReconnect();
    }
    
    @Override
    public void reconnect()
    {
        Log.v("GameplayPresenter", "Reconnected");
        
        view.reconnected();
    }
    
    @Override
    public void opponentQuit()
    {
        Log.v("GameplayPresenter", "Opponent quit");
        
        SharedResources.getShared().setError("Opponent quit!");
        
        view.opponentQuit();
    }
    
    @Override
    public void disconnect(String error)
    {
        Log.v("GameplayPresenter", "Disconnected, error: " + error);
        
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
    public void opponentPickedPlaySession() {}
    
    @Override
    public void nextGame()
    {
        
    }
    
    @Override
    public void opponentGuess(String guess) 
    {
        try {
            GuessResult result = gameSession.opponentIsGuessing(guess);
            
            Log.v("GameplayPresenter", "Received a guess message from opponent " + guess);
            
            view.setCurrentTurnValue(gameSession.getGameTurn(), gameSession.isMyTurn());
            
            view.updateLog(gameSession.getLog(initialConnection.otherPlayerName));
            
            // If not correctly guessed, send a guess response back
            if (!result.hasSuccessfullyGuessed())
            {
                Log.v("GameplayPresenter", "Send guess response back to opponent " + guess);
                
                communicator.sendGuessIncorrectResponseMessage(result.messageWithGuess);
            }
            // Opponent correctly guessed, send message, show loser screen
            else
            {
                Log.v("GameplayPresenter", "Opponent correctly guessed our word! You lose! " + guess);
                
                communicator.sendGuessCorrectResponseMessage();
                
                view.defeat(guess);
            }
        }
        catch (GameError e)
        {
            
        }
    }
    
    @Override
    public void incorrectGuessResponse(String response) 
    {
        try {
            gameSession.opponentGuessResponse(response);
    
            Log.v("GameplayPresenter", "Opponent guess response " + response);
    
            view.setCurrentTurnValue(gameSession.getGameTurn(), gameSession.isMyTurn());
            
            view.updateLog(gameSession.getLog(initialConnection.otherPlayerName));
        }
        catch (GameError e)
        {
            
        }
    }
    
    @Override
    public void correctGuessResponse()
    {
        try {
            gameSession.endGame();
            
            Log.v("GameplayPresenter", "Opponent says you correctly guessed! You win!");
            
            // Show winners screen
            view.victory(gameSession.getLastGuessAttempt());
        }
        catch (GameError e)
        {
            
        }
    }
}
