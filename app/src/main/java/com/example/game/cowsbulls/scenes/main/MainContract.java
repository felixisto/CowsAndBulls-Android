package com.example.game.cowsbulls.scenes.main;

import com.example.game.cowsbulls.BaseView;
import com.example.game.cowsbulls.BasePresenter;

public interface MainContract 
{
    interface Presenter extends BasePresenter
    {
        void goToHostScreen();
        void goToJoinScreen();
    }
    
    interface View extends BaseView<Presenter> 
    {
        void showDisconnected();
        void showOpponentQuit();
        
        void goToHostScreen();
        void goToJoinScreen();
    }
}
