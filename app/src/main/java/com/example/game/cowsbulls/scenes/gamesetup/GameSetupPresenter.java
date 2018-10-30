package com.example.game.cowsbulls.scenes.gamesetup;

import android.support.annotation.NonNull;
import android.util.Log;
import static com.google.common.base.Preconditions.checkNotNull;

import com.example.game.cowsbulls.game.GameConstants;
import com.example.game.cowsbulls.game.GameTurn;
import com.example.game.cowsbulls.network.Communicator;
import com.example.game.cowsbulls.network.CommunicatorInitialConnection;
import com.example.game.cowsbulls.network.CommunicatorObserver;
import com.example.game.cowsbulls.shared.SharedResources;

public class GameSetupPresenter implements GameSetupContract.Presenter, CommunicatorObserver
{
    private GameSetupContract.View view;
    private Communicator communicator;
    private CommunicatorInitialConnection initialConnection;
    
    private int selectedGuessWordCharacterCount;
    private GameTurn selectedTurnToGo;
    private boolean sentRequestToOpponent;
    private int opponentPickedGuessWordCharacterCount;
    private GameTurn opponentPickedTurnToGo;
    private boolean opponentSentRequest;
    
    public GameSetupPresenter(@NonNull GameSetupContract.View view, @NonNull Communicator communicator, @NonNull CommunicatorInitialConnection initialConnection)
    {
        this.view = checkNotNull(view, "View cannot be null");
        
        this.communicator = checkNotNull(communicator, "Communicator cannot be null");
        
        this.communicator.attachObserver(this, Integer.toHexString(System.identityHashCode(this)));
        
        this.initialConnection = checkNotNull(initialConnection, "InitialConnection cannot be null");
        
        this.view.setPresenter(this);
        
        this.selectedGuessWordCharacterCount = GameConstants.GUESS_WORD_CHARACTER_COUNT_MIN;
        this.selectedTurnToGo = GameTurn.FIRST;
        this.sentRequestToOpponent = false;
        this.opponentPickedGuessWordCharacterCount = 0;
        this.opponentPickedTurnToGo = GameTurn.FIRST;
        this.opponentSentRequest = false;
    }
    
    private boolean setupParametersAgreeWithOpponents()
    {
        return selectedGuessWordCharacterCount != 0 && selectedGuessWordCharacterCount == opponentPickedGuessWordCharacterCount && !selectedTurnToGo.equals(opponentPickedTurnToGo);
    }
    
    // - Presenter interface -
    
    @Override
    public void start()
    {
        Log.v("GameSetupPresenter", "Start");
        
        String otherPlayerName = initialConnection != null ? initialConnection.otherPlayerName : "Unknown";
        String otherPlayerAddress = initialConnection != null ? initialConnection.otherPlayerAddress : "?";
    
        view.updateConnectionData(otherPlayerName, otherPlayerAddress);
    }
    
    @Override
    public void goToGameSessionScreen()
    {
        view.goToGameSessionScreen(selectedGuessWordCharacterCount, selectedTurnToGo.value);
    }
    
    @Override
    public void leave()
    {
        Log.v("GameSetupPresenter", "Leave game");
    
        communicator.detachObserver(Integer.toHexString(System.identityHashCode(this)));
        
        communicator.stop();
        
        view.leave();
    }
    
    @Override
    public void didSelectGuessWordCharacterCount(int number)
    {
        if (number >= GameConstants.GUESS_WORD_CHARACTER_COUNT_MIN)
        {
            Log.v("GameSetupPresenter", "Selected guess word character count " + String.valueOf(number));
    
            selectedGuessWordCharacterCount = number;
        }
    }
    
    @Override
    public void didSelectTurnToGo(String turnToGo)
    {
        GameTurn turn = GameTurn.create(turnToGo);
        
        if (turn != null)
        {
            Log.v("GameSetupPresenter", "Selected turn to go " + turnToGo);
    
            selectedTurnToGo = turn;
        }
    }
    
    @Override
    public void pickCurrentPlaySetupAndSendToOpponent()
    {
        Log.v("GameSetupPresenter", "Sending play setup to opponent with guess word length " + String.valueOf(selectedGuessWordCharacterCount) + " and " + selectedTurnToGo.value + " turn value");
        
        // If the opponent has already sent their play setup, compare and we may agree go to the next screen right here
        if (opponentSentRequest)
        {
            if (setupParametersAgreeWithOpponents())
            {
                sentRequestToOpponent = true;
                
                // Send message to opponent
                communicator.sendPlaySetupMessage(selectedGuessWordCharacterCount, selectedTurnToGo.value);
                
                // Match
                view.playSetupMatch();
            }
            else
            {
                Log.v("GameSetupPresenter", "We disagree with the opponent game setup values! Try again");
                
                // Mismatch
                view.playSetupMismatch();
            }
            
            return;
        }
        
        // Else, send the character count to opponent
        sentRequestToOpponent = true;
        
        communicator.sendPlaySetupMessage(selectedGuessWordCharacterCount, selectedTurnToGo.value);
    }
    
    @Override
    public void playSetupMismatchesOpponentPlayerSetup()
    {
        // Skip all of this, if no requests have been sent by either side
        if (!sentRequestToOpponent && !opponentSentRequest)
        {
            return;
        }
        
        Log.v("GameSetupPresenter", "We disagree with the opponent game setup values! Try again");
        
        if (!sentRequestToOpponent)
        {
            communicator.sendPlaySetupMessage(selectedGuessWordCharacterCount, selectedTurnToGo.value);
        }
        
        sentRequestToOpponent = false;
        opponentSentRequest = false;
    }
    
    @Override
    public void playSetupMatchesOpponentPlayerSetup()
    {
        // Skip all of this, if either mine or the opponent guess word length is zero
        if (!(selectedGuessWordCharacterCount == opponentPickedGuessWordCharacterCount && selectedGuessWordCharacterCount != 0))
        {
            return;
        }
        
        Log.v("GameSetupPresenter", "We have agreed with the opponent on the game setup values! Guess words will be " + String.valueOf(selectedGuessWordCharacterCount) + " characters long and we are " + selectedTurnToGo + " to go!");
        
        view.goToGameSessionScreen(selectedGuessWordCharacterCount, selectedTurnToGo.value);
    }
    
    // - CommunicatorObserver interface -
    
    @Override
    public void lostConnectionAttemptingToReconnect()
    {
        Log.v("GameSetupPresenter", "Lost connection attempting to reconnect");
        
        view.lostConnectionAttemptingToReconnect();
    }
    
    @Override
    public void reconnect()
    {
        Log.v("GameSetupPresenter", "Reconnected");
        
        view.reconnected();
    }
    
    @Override
    public void opponentQuit()
    {
        Log.v("GameSetupPresenter", "Opponent quit");
    
        SharedResources.getShared().setError("Opponent quit!");
        
        view.opponentQuit();
    }
    
    @Override
    public void disconnect(String error)
    {
        Log.v("GameSetupPresenter", "Disconnected, error: " + error);
        
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
    public void opponentPickedPlaySetup(int guessWordLength, String turnToGo)
    {
        // If guess word length is zero, setup is mismatch
        if (guessWordLength == 0)
        {
            return;
        }
        
        // We need a valid Turn value
        GameTurn opponentTurnToGo = GameTurn.create(turnToGo);
        
        if (opponentTurnToGo == null)
        {
            return;
        }
    
        Log.v("GameSetupPresenter", "Opponent selected guess word length " + String.valueOf(guessWordLength));
        
        opponentSentRequest = true;
        opponentPickedGuessWordCharacterCount = guessWordLength;
        opponentPickedTurnToGo = opponentTurnToGo;
        
        view.updateOpponentPlaySetup(guessWordLength, turnToGo);
    
        // If we have already sent our play setup to the opponent, try to see if the parameters match here
        // We may go to the next screen here
        if (sentRequestToOpponent)
        {
            if (setupParametersAgreeWithOpponents())
            {
                view.playSetupMatch();
            }
            else
            {
                view.playSetupMismatch();
            }
        }
    }
    
    @Override
    public void opponentPickedPlaySession() {}
    
    @Override
    public void nextGame()
    {
        
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
