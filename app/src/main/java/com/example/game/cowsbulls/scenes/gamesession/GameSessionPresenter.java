package com.example.game.cowsbulls.scenes.gamesession;

import android.support.annotation.NonNull;
import android.util.Log;
import java.util.ArrayList;
import static com.google.common.base.Preconditions.checkNotNull;

import com.example.game.cowsbulls.game.GameSession;
import com.example.game.cowsbulls.game.GameTurn;
import com.example.game.cowsbulls.network.Communicator;
import com.example.game.cowsbulls.network.CommunicatorInitialConnection;
import com.example.game.cowsbulls.network.CommunicatorObserver;
import com.example.game.cowsbulls.shared.SharedResources;

public class GameSessionPresenter implements GameSessionContract.Presenter, CommunicatorObserver
{
    private GameSessionContract.View view;
    private Communicator communicator;
    private CommunicatorInitialConnection initialConnection;
    
    private final int guessWordLength;
    private GameTurn turnToGo;
    
    private String guessWordPicked;
    
    private boolean opponentHasPickedGuessWord;
    
    private boolean waitingForNextGame;
    
    public GameSessionPresenter(@NonNull GameSessionContract.View view, @NonNull Communicator communicator, @NonNull CommunicatorInitialConnection initialConnection, int guessWordLength, @NonNull String turnToGo) {
        this.view = checkNotNull(view, "View cannot be null");
    
        this.communicator = checkNotNull(communicator, "Communicator cannot be null");
    
        this.communicator.attachObserver(this, Integer.toHexString(System.identityHashCode(this)));
    
        this.initialConnection = checkNotNull(initialConnection, "InitialConnection cannot be null");
    
        this.view.setPresenter(this);
    
        this.guessWordLength = guessWordLength;
        this.turnToGo = GameTurn.create(checkNotNull(turnToGo, "Turn to go cannot be null"));
    
        if (this.turnToGo == null) {
            this.turnToGo = GameTurn.FIRST;
        }
    
        this.guessWordPicked = "";
        this.opponentHasPickedGuessWord = false;
        
        this.waitingForNextGame = false;
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
    
    protected void prepareForNewGame()
    {
        this.guessWordPicked = "";
        
        this.opponentHasPickedGuessWord = false;
        
        this.turnToGo = this.turnToGo.nextTurn();
    }
    
    // - Presenter interface -
    
    @Override
    public void start()
    {
        Log.v("GameSessionPresenter", "Start");
        
        String otherPlayerName = initialConnection != null ? initialConnection.otherPlayerName : "Unknown";
        String otherPlayerAddress = initialConnection != null ? initialConnection.otherPlayerAddress : "?";
        
        view.updateConnectionData(otherPlayerName, otherPlayerAddress);
        
        view.updateEnterXCharacterWord(guessWordLength);
    }
    
    @Override
    public void goToGameplayScreen(String guessWord)
    {
        if (waitingForNextGame)
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
    
            waitingForNextGame = true;
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
        
        communicator.detachObserver(Integer.toHexString(System.identityHashCode(this)));
        
        communicator.stop();
        
        view.leave();
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
        if (waitingForNextGame)
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
        
        view.setOpponentStatus("Opponent picked a guess word!");
        
        // If we have picked word too, play
        if (guessWordPicked.length() > 0)
        {
            goToGameplayScreen(guessWordPicked);
        }
    }
    
    @Override
    public void nextGame()
    {
        if (!waitingForNextGame)
        {
            return;
        }
        
        Log.v("GameSessionPresenter", "Opponent is calling for next game!");
        
        waitingForNextGame = false;
        
        prepareForNewGame();
        
        view.nextGame();
    }
    
    @Override
    public void opponentGuess(String guess) {}
    
    @Override
    public void incorrectGuessResponse(String response)  {}
    
    @Override
    public void correctGuessResponse() {}
    
    @Override
    public void opponentChatMessage(String message) {}
}
