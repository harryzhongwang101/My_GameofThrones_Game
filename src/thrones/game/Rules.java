package thrones.game;

import ch.aplu.jcardgame.*;

import java.util.Optional;

public class Rules{

    public static boolean isMoveValid(Optional<Card> selected, Hand piles){
        CardInfo.Suit suit = (CardInfo.Suit)selected.get().getSuit();

        if (suit.isMagic() && (piles.get(piles.getNumberOfCards()-1).getSuit() == suit.HEARTS)) {
            return false;
        }
        return true;
    }

    public static void isMoveLegal(Optional<Card> selected, Hand piles) throws BrokeRuleException {

        if (!isMoveValid(selected, piles)) {
            throw new BrokeRuleException("Move Not Valid");
        }
    }
}
