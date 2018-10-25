package com.example.game.cowsbulls.game;

public class GameTurn
{
    public static final GameTurn FIRST = new GameTurn("First");
    public static final GameTurn SECOND = new GameTurn("Second");
    
    public final String value;
    
    public static GameTurn create(String value)
    {
        if (value.equals(FIRST.value) || value.equals(SECOND.value))
        {
            return new GameTurn(value);
        }
        
        return null;
    }
    
    private GameTurn(String value)
    {
        this.value = value;
    }
    
    public GameTurn nextTurn()
    {
        return value.equals(FIRST.value) ? SECOND : FIRST;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) {return false;}
        
        if (!GameTurn.class.isAssignableFrom(obj.getClass()))
        {
            return false;
        }
        
        final GameTurn other = (GameTurn)obj;
        
        return value.equals(other.value);
    }
}
