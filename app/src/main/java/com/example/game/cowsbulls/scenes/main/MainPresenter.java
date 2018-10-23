package com.example.game.cowsbulls.scenes.main;

import android.util.Log;

public class MainPresenter implements MainContract.Presenter
{
    private MainContract.View view;
    
    public MainPresenter(MainContract.View view)
    {
        this.view = view;
        
        this.view.setPresenter(this);
    }
    
    @Override
    public void start()
    {
        Log.v("MainPresenter", "Start");
    }

    @Override
    public void goToHostScreen()
    {
        view.goToHostScreen();
    }

    @Override
    public void goToJoinScreen()
    {
        view.goToJoinScreen();
    }
}
