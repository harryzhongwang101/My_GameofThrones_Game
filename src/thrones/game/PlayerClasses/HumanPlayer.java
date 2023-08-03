package thrones.game.PlayerClasses;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.CardAdapter;
import ch.aplu.jcardgame.Hand;
import thrones.game.Board;
import thrones.game.CardInfo;
import thrones.game.GameUIManager;
import thrones.game.Rules;

import java.util.Optional;

public class HumanPlayer extends Player {
    public HumanPlayer(Hand hand, int playerIndex) {
        super(hand, playerIndex);
        currentHand.addCardListener(new CardAdapter() {
            public void leftDoubleClicked(Card card) {
                selected = Optional.of(card);
                currentHand.setTouchEnabled(false);
            }
            public void rightClicked(Card card) {
                selected = Optional.empty(); // Don't care which card we right-clicked for player to pass
                currentHand.setTouchEnabled(false);
            }
        });
    }

    @Override
    public void playACard(Board board, boolean isCharacter) {
        int pileIndex = NON_SELECTION_VALUE;
        boolean haveNotDoneValidChoice = true;

        while (haveNotDoneValidChoice){
            waitForCorrectSuit(isCharacter);

            // select none, passing
            if(selected.isEmpty()){
                break;
            }

            if (isCharacter) {
                pileIndex = playerIndex % 2;
            } else {
                GameUIManager.getGameUI().setStatusText("Selected: " + CardInfo.canonical(selected.get()) + ". Player" + playerIndex + " select a pile to play the card.");
                pileIndex = board.waitForPileSelection();
            }
            if(!Rules.isMoveValid(selected, board.getPiles(pileIndex))){
                GameUIManager.getGameUI().setStatusText("Player" + playerIndex + " invalid move, please re-select card");
            }else {
                haveNotDoneValidChoice = false;
            }
        }

        if (selected.isPresent()) {
            board.placeCard(playerIndex, selected, pileIndex);
        } else {
            GameUIManager.getGameUI().setStatusText("Pass.");
        }


    }


    private void waitForCorrectSuit(boolean isCharacter) {
        if (currentHand.isEmpty()) {
            selected = Optional.empty();
        } else {
            selected = null;
            currentHand.setTouchEnabled(true);
            do {
                if (selected == null) {
                    GameUIManager.getGameUI().delay(100);
                    continue;
                }
                CardInfo.Suit suit = selected.isPresent() ? (CardInfo.Suit) selected.get().getSuit() : null;
                if (isCharacter && suit != null && suit.isCharacter() ||         // If we want character, can't pass and suit must be right
                        !isCharacter && (suit == null || !suit.isCharacter())) { // If we don't want character, can pass or suit must not be character
                    break;
                } else {
                    selected = null;
                    currentHand.setTouchEnabled(true);
                }
                GameUIManager.getGameUI().delay(100);
            } while (true);
        }
    }


}
