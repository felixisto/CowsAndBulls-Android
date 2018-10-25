package com.example.game.cowsbulls.scenes.main;

import android.support.design.widget.Snackbar;
import android.util.Log;
import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Button;

import com.example.game.cowsbulls.R;
import com.example.game.cowsbulls.scenes.host.HostActivity;
import com.example.game.cowsbulls.scenes.join.JoinActivity;
import com.example.game.cowsbulls.shared.SharedResources;

public class MainFragment extends Fragment implements MainContract.View
{
    private MainContract.Presenter presenter;
    
    public MainFragment() 
    {
        
    }
    
    public static MainFragment newInstance() 
    {
        return new MainFragment();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main_screen, container, false);
        
        // Setup UI
        final Button host = root.findViewById(R.id.buttonHostGame);
        
        host.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                presenter.goToHostScreen();
            }
        });
        
        final Button join = root.findViewById(R.id.buttonJoinGame);
        
        join.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                presenter.goToJoinScreen();
            }
        });
        
        return root;
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        presenter.start();
        
        String error = SharedResources.getShared().getErrorAndClear();
        
        if (error != null && error.length() > 0)
        {
            Snackbar snackbar = Snackbar.make(getView(), error, Snackbar.LENGTH_LONG);
            
            snackbar.show();
        }
    }
    
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }
    
    @Override
    public void onDetach() 
    {
        super.onDetach();
    }
    
    @Override
    public void setPresenter(MainContract.Presenter presenter)
    {
        this.presenter = presenter;
    }
    
    @Override
    public void goBack() {}
    
    @Override
    public void showDisconnected()
    {
        
    }
    
    @Override
    public void showOpponentQuit()
    {
        
    }
    
    @Override
    public void goToHostScreen()
    {
        Log.v("MainFragment", "Go to Host Screen");
        Intent intent = new Intent(getContext(), HostActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    @Override
    public void goToJoinScreen()
    {
        Log.v("MainFragment", "Go to Join Screen");
        Intent intent = new Intent(getContext(), JoinActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
