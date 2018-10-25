package com.example.game.cowsbulls.scenes.gameplay;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.game.cowsbulls.R;
import com.example.game.cowsbulls.network.Communicator;
import com.example.game.cowsbulls.shared.SharedResources;

public class GameplayActivity extends AppCompatActivity
{
    private GameplayPresenter presenter;
    
    private Communicator communicator;
    
    private GameplayFragment fragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.communicator = SharedResources.getShared().getCommunicator();
        
        setContentView(R.layout.activity_gameplay_screen);
        
        fragment = (GameplayFragment)getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        
        if (fragment == null)
        {
            fragment = GameplayFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.contentFrame, fragment);
            transaction.commit();
        }
        
        this.presenter = new GameplayPresenter(fragment, communicator, SharedResources.getShared().getCommunicatorInitialConnection(), SharedResources.getShared().getGameSession());
        
        getSupportActionBar().setTitle("Cows & Bulls");
    }
    
    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
        builder.setMessage("Are you sure you want to leave the game?");
        builder.setCancelable(true);
        
        builder.setNeutralButton("Leave", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                
                GameplayActivity.super.onBackPressed();
                
                presenter.leave();
            }
        });
        
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        
        builder.create().show();
    }
}
