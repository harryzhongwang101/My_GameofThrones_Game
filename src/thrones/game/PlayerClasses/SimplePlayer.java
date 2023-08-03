package thrones.game.PlayerClasses;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.*;
import thrones.game.PlayerClasses.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class SimplePlayer extends Player {


    private Random random;

    public SimplePlayer(Hand hand, int playerIndex, Random random) {
        super(hand, playerIndex);
        this.random = random;
    }

    @Override
    public void playACard(Board board, boolean isCharacter) {

        int pileIndex = NON_SELECTION_VALUE;
        boolean cardNotValid = false;
        List<Card> shortListCards = new ArrayList<>();

        for (int i = 0; i < currentHand.getCardList().size(); i++) {
            Card card = currentHand.getCardList().get(i);
            CardInfo.Suit suit = (CardInfo.Suit) card.getSuit();
            if (suit.isCharacter() == isCharacter) {
                shortListCards.add(card);
            }
        }

        if (shortListCards.isEmpty() || !isCharacter && random.nextInt(3) == 0) {
            selected = Optional.empty();
        }
        else {
            selected = Optional.of(shortListCards.get(random.nextInt(shortListCards.size())));
            CardInfo.Suit suit = (CardInfo.Suit)selected.get().getSuit();
            if(isCharacter){
                pileIndex = playerIndex % 2;
            }else {
                pileIndex = selectRandomPile();
            }
            if (!Rules.isMoveValid(selected, board.getPiles(pileIndex))) {
                cardNotValid = true;
            }

            if (suit.isMagic() && pileIndex == playerIndex % 2) {
                cardNotValid = true;
            }
            if (!suit.isMagic() && pileIndex == (playerIndex + 1) % 2) {
                cardNotValid = true;
            }
            if(cardNotValid){
                selected = Optional.empty();
            }
        }
        if (selected.isPresent()) {
            GameUIManager.getGameUI().setStatusText("Selected: " + CardInfo.canonical(selected.get()) + ". Player" + playerIndex + " select a pile to play the card.");
            board.placeCard(playerIndex, selected, pileIndex);

        }else {
            GameUIManager.getGameUI().setStatusText("Pass.");
        }

    }
    private int selectRandomPile() {
        return random.nextInt(2);
    }
}