package com.example.game.cowsbulls.scenes.gamesession;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.game.cowsbulls.R;
import com.example.game.cowsbulls.network.Communicator;
import com.example.game.cowsbulls.shared.SharedResources;

public class GameSessionActivity extends AppCompatActivity
{
    private GameSessionPresenter presenter;
    
    private Communicator communicator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.communicator = SharedResources.getShared().getCommunicator();
        
        setContentView(R.layout.activity_gamesession_screen);
        
        GameSessionFragment fragment = (GameSessionFragment)getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        
        if (fragment == null)
        {
            fragment = GameSessionFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.contentFrame, fragment);
            transaction.commit();
        }
        
        final int guessWordLength = getIntent().getIntExtra("guessWordLength", 0);
        final String turnToGo = getIntent().getStringExtra("turnToGo");
        
        this.presenter = new GameSessionPresenter(fragment, communicator, SharedResources.getShared().getCommunicatorInitialConnection(), guessWordLength, turnToGo);
        
        getSupportActionBar().setTitle("Pick Guess Word");
    }
    
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        
        this.presenter.leave();
    }
}
