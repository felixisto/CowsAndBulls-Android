package com.example.game.cowsbulls.scenes.host;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.TextView;

import com.example.game.cowsbulls.R;
import com.example.game.cowsbulls.scenes.gamesetup.GameSetupActivity;
import com.example.game.cowsbulls.network.LocalAddress;
import com.example.game.cowsbulls.scenes.main.MainActivity;

public class HostFragment extends Fragment implements HostContract.View
{
    private HostContract.Presenter presenter;
    
    private TextView labelTip;
    
    public HostFragment()
    {

    }
    
    public static HostFragment newInstance()
    {
        return new HostFragment();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_host_screen, container, false);
        
        // Setup UI references
        labelTip = root.findViewById(R.id.labelTip);
        
        // Setup UI
        final TextView localIP = root.findViewById(R.id.labelLocalIP);
        localIP.setText("Your IP address is " + LocalAddress.get());
        
        return root;
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        presenter.start();
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
    
    // - View interface -
    
    @Override
    public void setPresenter(HostContract.Presenter presenter)
    {
        this.presenter = presenter;
    }
    
    @Override
    public void goBack()
    {
        Log.v("HostFragment", "Go back");
        
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    @Override
    public void goToGameSetupScreen()
    {
        Log.v("HostFragment", "Go to Game Setup Screen");
        Intent intent = new Intent(getContext(), GameSetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    @Override
    public void showDisconnected()
    {
        
    }
    
    @Override
    public void showOpponentQuit()
    {
        
    }
    
    @Override
    public void clientBeginConnect()
    {
        labelTip.setText("Found a client! Connecting...");
    }
    
    @Override
    public void clientTimeout()
    {
        labelTip.setText("Waiting for other player to join...");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Connection timeout. Could not connect with client.");
        builder.setCancelable(true);
        
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                
                presenter.start();
            }
        });
        
        builder.create().show();
    }
    
    @Override
    public void failedToStart(String error)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Failed to start server. Error: " + error);
        builder.setCancelable(true);
        
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                
                presenter.leave();
            }
        });
        
        builder.create().show();
    }
}
