package thrones.game;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.CardGame;
import ch.aplu.jcardgame.Hand;

import java.util.stream.Collectors;

public class CardInfo extends CardGame {
    enum GoTSuit { CHARACTER, DEFENCE, ATTACK, MAGIC }
    public enum Suit {
        SPADES(CardInfo.GoTSuit.DEFENCE),
        HEARTS(CardInfo.GoTSuit.CHARACTER),
        DIAMONDS(CardInfo.GoTSuit.MAGIC),
        CLUBS(CardInfo.GoTSuit.ATTACK);
        Suit(CardInfo.GoTSuit gotsuit) {
            this.gotsuit = gotsuit;
        }
        private final CardInfo.GoTSuit gotsuit;

        public boolean isDefence(){ return gotsuit == CardInfo.GoTSuit.DEFENCE; }

        public boolean isAttack(){ return gotsuit == CardInfo.GoTSuit.ATTACK; }

        public boolean isCharacter(){ return gotsuit == CardInfo.GoTSuit.CHARACTER; }

        public boolean isMagic(){ return gotsuit == CardInfo.GoTSuit.MAGIC; }
    }

    /*
    Canonical String representations of Suit, Rank, Card, and Hand
    */
    public static String canonical(CardInfo.Suit s) { return s.toString().substring(0, 1); }

    public static String canonical(CardInfo.Rank r) {
        switch (r) {
            case ACE: case KING: case QUEEN: case JACK: case TEN:
                return r.toString().substring(0, 1);
            default:
                return String.valueOf(r.getRankValue());
        }
    }

    public static String canonical(Card c) { return canonical((CardInfo.Rank) c.getRank()) + canonical((CardInfo.Suit) c.getSuit()); }

    public static String canonical(Hand h) {
        return "[" + h.getCardList().stream().map(CardInfo::canonical).collect(Collectors.joining(",")) + "]";
    }

    public enum Rank {
        // Reverse order of rank importance (see rankGreater() below)
        // Order of cards is tied to card images
        ACE(1), KING(10), QUEEN(10), JACK(10), TEN(10), NINE(9), EIGHT(8), SEVEN(7), SIX(6), FIVE(5), FOUR(4), THREE(3), TWO(2);
        Rank(int rankValue) {
            this.rankValue = rankValue;
        }
        private final int rankValue;
        public int getRankValue() {
            return rankValue;
        }
    }
}
