package thrones.game.PlayerClasses;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import thrones.game.*;

import java.util.*;

public class RandomPlayer extends Player {
    private Random random = new Random();
    boolean isPlaying = false;

    public RandomPlayer(Hand hand, int playerIndex, Random random){
        super(hand, playerIndex);
        this.random = random;
    }

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
        } else {
            selected = Optional.of(shortListCards.get(random.nextInt(shortListCards.size())));

            if (!isCharacter) {
                pileIndex = selectRandomPile();
            }else {
                pileIndex = playerIndex % 2;
            }
            assert selected.isPresent() : " Pass returned on selection of character.";

            if (!Rules.isMoveValid(selected, board.getPiles(pileIndex))) {
                cardNotValid = true;
            }

        }


        if (cardNotValid) {
            selected = Optional.empty();
        }

        if (selected.isPresent()) {
            GameUIManager.getGameUI().setStatusText("Selected: " + CardInfo.canonical(selected.get()) + ". Player" + playerIndex + " select a pile to play the card.");
            board.placeCard(playerIndex, selected, pileIndex);
        } else {
            GameUIManager.getGameUI().setStatusText("Pass.");
        }
    }


    private int selectRandomPile() {
        return random.nextInt(2);
    }

}
