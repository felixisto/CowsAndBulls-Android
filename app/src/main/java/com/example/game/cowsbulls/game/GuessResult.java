package com.example.game.cowsbulls.game;

import java.util.ArrayList;

public class GuessResult
{
    public final int guessWordLength;
    public final String message;
    public final String messageWithGuess;
    public final ArrayList<GuessCharacterResult> characterGuesses;
    
    protected GuessResult(int guessWordLength, String guess, ArrayList<GuessCharacterResult> characterGuesses)
    {
        this.guessWordLength = guessWordLength;
        this.message = GuessCharacterResult.arrayToString(this.guessWordLength, characterGuesses);
        this.messageWithGuess = "guessed " + guess + ", that's " + message + "!";
        this.characterGuesses = characterGuesses;
    }
    
    public boolean hasSuccessfullyGuessed()
    {
        int bulls = 0;
    
        for (int e = 0; e < characterGuesses.size(); e++)
        {
            if (characterGuesses.get(e).isBull())
            {
                bulls += 1;
            }
        }
        
        return bulls == guessWordLength;
    }
}

class GuessCharacter
{
    protected final char character;
    protected final int position;
    
    protected GuessCharacter(char character, int position)
    {
        this.character = character;
        this.position = position;
    }
    
    static ArrayList<GuessCharacter> stringToGuessCharacters(String string)
    {
        ArrayList<GuessCharacter> guessCharacters = new ArrayList<>();
        
        for (int e = 0; e < string.length(); e++)
        {
            guessCharacters.add(new GuessCharacter(string.charAt(e), e));
        }
        
        return guessCharacters;
    }
    
    static String guessCharactersToString(ArrayList<GuessCharacter> guessCharacters)
    {
        String string = "";
        
        for (int e = 0; e < guessCharacters.size(); e++)
        {
            string = string.concat(String.valueOf(guessCharacters.get(e).character));
        }
        
        return string;
    }
    
    static GuessResult guessResult(final ArrayList<GuessCharacter> guessWordConstant, final ArrayList<GuessCharacter> guessConstant) 
    {
        ArrayList<GuessCharacterResult> characterGuesses = new ArrayList<GuessCharacterResult>();
        
        ArrayList<GuessCharacter> guessWord = new ArrayList<GuessCharacter>(guessWordConstant);
        ArrayList<GuessCharacter> guess = new ArrayList<GuessCharacter>(guessConstant);
        
        while (guessWord.size() > 0)
        {
            GuessCharacter guessWordChar = guessWord.get(0);
            
            GuessCharacterResult bestComparisonResultForThisChar = new GuessCharacterResult(false, false);
            int bestComparisonResultIndex = -1;
            
            for (int i = 0; i < guess.size(); i++) 
            {
                GuessCharacter guessChar = guess.get(i);
    
                GuessCharacterResult comparisonResult = new GuessCharacterResult(guessWordChar, guessChar);
    
                if (comparisonResult.isCow() || comparisonResult.isBull())
                {
                    if (bestComparisonResultIndex == -1)
                    {
                        bestComparisonResultForThisChar = comparisonResult;
                        bestComparisonResultIndex = i;
                    }
                    else
                    {
                        if (bestComparisonResultForThisChar.isCow() && comparisonResult.isBull())
                        {
                            bestComparisonResultForThisChar = comparisonResult;
                            bestComparisonResultIndex = i;
                        }
                    }
                }
            }
            
            characterGuesses.add(bestComparisonResultForThisChar);
            
            if (bestComparisonResultIndex != -1)
            {
                guess.remove(bestComparisonResultIndex);
            }
            
            guessWord.remove(0);
        }
        
        return new GuessResult(guessWordConstant.size(), guessCharactersToString(guessConstant), characterGuesses);
    }
}

class GuessCharacterResult
{
    private final boolean guessedValue;
    private final boolean guessedPosition;
    
    protected GuessCharacterResult(boolean guessedValue, boolean guessedPosition)
    {
        this.guessedValue = guessedValue;
        this.guessedPosition = guessedPosition;
    }
    
    protected GuessCharacterResult(GuessCharacter a, GuessCharacter b)
    {
        this.guessedValue = a.character == b.character;
        this.guessedPosition = a.position == b.position;
    }
    
    public boolean isCow()
    {
        return guessedValue && !guessedPosition;
    }
    
    public boolean isBull()
    {
        return guessedValue && guessedPosition;
    }
    
    public static String arrayToString(int guessWordLength, ArrayList<GuessCharacterResult> array)
    {
        int bulls = 0;
        int cows = 0;
        
        for (int e = 0; e < array.size(); e++)
        {
            if (array.get(e).isCow())
            {
                cows++;
            }
            
            if (array.get(e).isBull())
            {
                bulls++;
            }
        }
        
        if (bulls == guessWordLength)
        {
            return "Correct guess!";
        }
        
        if (bulls == 0 && cows != 0)
        {
            return String.valueOf(cows) + " cows";
        }
        
        if (cows == 0 && bulls != 0)
        {
            return String.valueOf(bulls) + " bulls";
        }
        
        if (cows == 0 && bulls == 0)
        {
            return "nothing";
        }
        
        return String.valueOf(cows) + " cows, " + String.valueOf(bulls) + " bulls";
    }
}
