package com.example.game.cowsbulls.scenes.gamesetup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.game.cowsbulls.R;
import com.example.game.cowsbulls.game.GameConstants;
import com.example.game.cowsbulls.game.GameTurn;
import com.example.game.cowsbulls.network.Communicator;
import com.example.game.cowsbulls.network.CommunicatorInitialConnection;
import com.example.game.cowsbulls.scenes.gamesession.GameSessionActivity;
import com.example.game.cowsbulls.scenes.main.MainActivity;
import com.example.game.cowsbulls.shared.SharedResources;

import java.util.ArrayList;
import java.util.List;

public class GameSetupFragment extends Fragment implements GameSetupContract.View
{
    private GameSetupContract.Presenter presenter;
    
    private GameSetupPickerAdapter numberOfCharactersAdapter;
    private GameSetupPickerAdapter turnsAdapter;
    
    public GameSetupFragment()
    {

    }

    public static GameSetupFragment newInstance()
    {
        return new GameSetupFragment();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gamesetup_screen, container, false);
        
        // Setup UI
        final Spinner pickerNumberOfCharacters = root.findViewById(R.id.pickerNumberOfCharacters);
        
        ArrayList<String> numberOfCharacters = new ArrayList<>();
        
        for (int e = GameConstants.GUESS_WORD_CHARACTER_COUNT_MIN; e <= GameConstants.GUESS_WORD_CHARACTER_COUNT_MAX; e++) 
        {
            numberOfCharacters.add(String.valueOf(e));
        }
        
        this.numberOfCharactersAdapter = new GameSetupPickerAdapter(getContext(), numberOfCharacters);
        pickerNumberOfCharacters.setAdapter(this.numberOfCharactersAdapter);
    
        pickerNumberOfCharacters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                presenter.didSelectGuessWordCharacterCount(Integer.parseInt(parent.getItemAtPosition(position).toString()));
            }
        
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        final Spinner pickerTurnToGo = root.findViewById(R.id.pickerTurnToGo);
        
        ArrayList<String> turnsToGo = new ArrayList<>();
        
        turnsToGo.add(GameTurn.FIRST.value);
        turnsToGo.add(GameTurn.SECOND.value);
        
        this.turnsAdapter = new GameSetupPickerAdapter(getContext(), turnsToGo);
        pickerTurnToGo.setAdapter(this.turnsAdapter);
    
        pickerTurnToGo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) 
            {
                presenter.didSelectTurnToGo(parent.getItemAtPosition(position).toString());
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        
        final Button buttonAgree = root.findViewById(R.id.buttonAgree);
    
        buttonAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                presenter.pickCurrentPlaySetupAndSendToOpponent();
                
                buttonAgree.setEnabled(false);
            }
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
    public void setPresenter(GameSetupContract.Presenter presenter)
    {
        this.presenter = presenter;
    }
    
    @Override
    public void goBack()
    {
        Log.v("GameSetupFragment", "Go back");
    }
    
    @Override
    public void goToGameSessionScreen(int guessWordLength, String turnToGo)
    {
        Log.v("GameSetupFragment", "Go to Game Session Screen");
        
        Intent intent = new Intent(getContext(), GameSessionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("guessWordLength", guessWordLength);
        intent.putExtra("turnToGo", turnToGo);
        startActivity(intent);
    }
    
    @Override
    public void updateOpponentPlaySetup(int guessWordLength, String turnToGo)
    {
        final TextView labelOpponentStatus = getView().findViewById(R.id.labelOpponentStatus);
        
        labelOpponentStatus.setText("Opponent wants " + String.valueOf(guessWordLength) + " digit guess words and wants " + turnToGo + " turn");
    }
    
    @Override
    public void playSetupMatch()
    {
        presenter.playSetupMatchesOpponentPlayerSetup();
    }
    
    @Override
    public void playSetupMismatch()
    {
        presenter.playSetupMismatchesOpponentPlayerSetup();
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Your picked values must not contradict the opponent's values.");
        builder.setCancelable(true);
        
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                
                if (getView() != null)
                {
                    final Button buttonAgree = getView().findViewById(R.id.buttonAgree);
                    
                    if (buttonAgree != null)
                    {
                        buttonAgree.setEnabled(true);
                    }
                }
            }
        });
        
        builder.create().show();
    }
    
    @Override
    public void updateConnectionData(String playerName, String playerAddress)
    {
        final TextView labelTip = getView().findViewById(R.id.labelInfo);
        
        labelTip.setText("Opponent: " + playerName + " (" + playerAddress + ")");
    }
    
    @Override
    public void lostConnectionAttemptingToReconnect()
    {
        if (getView() == null) {return;}
        
        Snackbar snackbar = Snackbar.make(getView(), "Lost connection, attempting to reconnect...", Snackbar.LENGTH_LONG);
        
        snackbar.show();
    }
    
    @Override
    public void reconnected()
    {
        if (getView() == null) {return;}
        
        Snackbar snackbar = Snackbar.make(getView(), "Reconnected!", Snackbar.LENGTH_LONG);
        
        snackbar.show();
    }
    
    @Override
    public void leave()
    {
        Log.v("GameSetupFragment", "Leave");
        
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    @Override
    public void opponentQuit()
    {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    @Override
    public void disconnected()
    {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}

class GameSetupPickerAdapter extends ArrayAdapter<String>
{
    private Context context;
    private List<String> data = new ArrayList<>();
    
    public GameSetupPickerAdapter(@NonNull Context context, ArrayList<String> data) 
    {
        super(context, R.layout.generic_spinner_text_item , data);
        this.context = context;
        this.data = data;
    }
    
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItem = convertView;
        
        if (listItem == null)
        {
            listItem = LayoutInflater.from(context).inflate(R.layout.generic_spinner_text_item, parent, false);
        }
        
        String currentString = data.get(position);
    
        TextView title =  (TextView)listItem.findViewById(R.id.title);
    
        title.setText(currentString);
    
        title.setGravity(Gravity.CENTER);
        
        return listItem;
    }
}
