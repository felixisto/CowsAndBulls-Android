package com.example.game.cowsbulls.scenes.gamesetup;

import com.example.game.cowsbulls.R;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.example.game.cowsbulls.network.Communicator;
import com.example.game.cowsbulls.shared.SharedResources;

public class GameSetupActivity extends AppCompatActivity
{
    private GameSetupPresenter presenter;
    
    private Communicator communicator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.communicator = SharedResources.getShared().getCommunicator();
        
        setContentView(R.layout.activity_gamesetup_screen);
        
        GameSetupFragment fragment = (GameSetupFragment)getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        
        if (fragment == null)
        {
            fragment = GameSetupFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.contentFrame, fragment);
            transaction.commit();
        }
        
        this.presenter = new GameSetupPresenter(fragment, communicator, SharedResources.getShared().getCommunicatorInitialConnection());
        
        getSupportActionBar().setTitle("Game Setup");
    }
    
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        
        this.presenter.leave();
    }
}
