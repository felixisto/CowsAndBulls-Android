package com.example.game.cowsbulls.scenes.gameplay;

import com.example.game.cowsbulls.BasePresenter;
import com.example.game.cowsbulls.BaseView;

public interface GameplayContract
{
    interface Presenter extends BasePresenter
    {
        void leave();
        
        void goBack();
        
        void guess(String guess);
    }
    
    interface View extends BaseView<Presenter>
    {
        void victory(String opponentGuessWord);
        void defeat(String myGuessWord);
        void setupUI(int guessCharacters, String myGuessWord, boolean firstToGo);
        void setCurrentTurnValue(int turn, boolean myTurn);
        void updateLog(String string);
        void updateConnectionData(String playerName, String playerAddress);
        void invalidGuessAttempt(String error);
        
        void lostConnectionAttemptingToReconnect();
        void reconnected();
        void leave();
        void opponentQuit();
        void disconnected();
    }
}
