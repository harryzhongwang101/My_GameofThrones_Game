package thrones.game.PlayerClasses;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import thrones.game.*;

import java.util.*;

abstract public class Player implements Observer, PlayCard{
    protected Hand currentHand;
    protected int playerIndex;
    protected Optional<Card> selected;
    protected int score = 0;
    protected final int NON_SELECTION_VALUE = -2;

    public Player(Hand hand, int playerIndex){
        currentHand = hand;
        this.playerIndex = playerIndex;
    }

    abstract public void playACard(Board board, boolean isCharacter);

    public void addScore(int score){
        this.score += score;
    }

    @Override
    public void update(int rank) {
        return;
    }

    public Hand getCurrentHand() {
        return currentHand;
    }

    public int getScore() {
        return score;
    }
}
