package com.example.game.cowsbulls.scenes.gamesession;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.example.game.cowsbulls.R;
import com.example.game.cowsbulls.scenes.gameplay.GameplayActivity;
import com.example.game.cowsbulls.scenes.main.MainActivity;
import com.chaos.view.PinView;

import org.w3c.dom.Text;

public class GameSessionFragment extends Fragment implements GameSessionContract.View
{
    private GameSessionContract.Presenter presenter;
    
    private PinView pinentry;
    private TextView labelInfo;
    private TextView labelTip;
    private TextView labelOpponentStatus;
    
    public GameSessionFragment()
    {
        
    }
    
    public static GameSessionFragment newInstance()
    {
        return new GameSessionFragment();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gamesession_screen, container, false);
        
        // Setup UI references
        pinentry = root.findViewById(R.id.pinentry);
        labelInfo = root.findViewById(R.id.labelInfo);
        labelTip = root.findViewById(R.id.labelTip);
        labelOpponentStatus = root.findViewById(R.id.labelOpponentStatus);
        
        // Setup UI
        pinentry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) 
            {
                if (s.length() == pinentry.getItemCount())
                {
                    // Hide keyboard, lose focus
                    pinentry.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(GameSessionActivity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    
                    // Alert presenter
                    presenter.goToGameplayScreen(s.toString());
                }
            }
    
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
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
    public void setPresenter(GameSessionContract.Presenter presenter)
    {
        this.presenter = presenter;
    }
    
    @Override
    public void goBack()
    {
        Log.v("GameSessionFragment", "Go back");
    }
    
    @Override
    public void goToGameplayScreen()
    {
        Log.v("GameSessionFragment", "Go to Gameplay Screen");
        
        Intent intent = new Intent(getContext(), GameplayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        
        pinentry.setText("");
        labelOpponentStatus.setText("Waiting for opponent to leave outcome screen...");
        pinentry.setEnabled(false);
    }
    
    @Override
    public void invalidGuessWord(String error)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(error);
        builder.setCancelable(true);
        
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
    
                pinentry.setText("");
            }
        });
        
        builder.create().show();
    }
    
    @Override
    public void setOpponentStatus(String status)
    {
        final TextView labelOpponentStatus = getView().findViewById(R.id.labelOpponentStatus);
    
        labelOpponentStatus.setText(status);
    }
    
    @Override
    public void updateEnterXCharacterWord(int length)
    {
        labelTip.setText("Enter " + String.valueOf(length) + " digit guess word");
        
        pinentry.setItemCount(length);
        
    }
    
    @Override
    public void updateConnectionData(String playerName, String playerAddress)
    {
        labelInfo.setText("Opponent: " + playerName + " (" + playerAddress + ")");
    }
    
    @Override
    public void nextGame()
    {
        pinentry.setEnabled(true);
        labelOpponentStatus.setText("Opponent is picking guess word...");
    }
    
    @Override
    public void lostConnectionAttemptingToReconnect()
    {
        Snackbar snackbar = Snackbar.make(getView(), "Lost connection, attempting to reconnect...", Snackbar.LENGTH_LONG);
        
        snackbar.show();
    }
    
    @Override
    public void reconnected()
    {
        Snackbar snackbar = Snackbar.make(getView(), "Reconnected!", Snackbar.LENGTH_LONG);
        
        snackbar.show();
    }
    
    @Override
    public void leave()
    {
        Log.v("GameSessionFragment", "Leave");
        
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    @Override
    public void opponentQuit()
    {
        
    }
    
    @Override
    public void disconnected()
    {
        
    }
}
