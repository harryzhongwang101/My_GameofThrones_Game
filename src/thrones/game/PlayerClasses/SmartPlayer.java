package thrones.game.PlayerClasses;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.Board;
import thrones.game.CardInfo;
import thrones.game.GameUIManager;
import thrones.game.Rules;

import java.lang.reflect.Array;
import java.util.*;

public class SmartPlayer extends Player{
    private int[] leftDiamondCards = {1,1,1,1,1,1,1,1,1,4};

    public SmartPlayer(Hand hand, int playerIndex){
        super(hand, playerIndex);
        initLeftDiamondCards(hand);
    }

    @Override
    public void playACard(Board board, boolean isCharacter) {
        //Hand currentHand = hands[playerIndex];
        int pileIndex = NON_SELECTION_VALUE;
        List<Card> shortListCards = new ArrayList<>();


        for (int i = 0; i < currentHand.getCardList().size(); i++) {
            Card card = currentHand.getCardList().get(i);
            CardInfo.Suit suit = (CardInfo.Suit) card.getSuit();
            if (suit.isCharacter() == isCharacter) {
                shortListCards.add(card);
            }
        }

        // skip if have no playable cards
        if (shortListCards.isEmpty()) {
            selected = Optional.empty();
            //board.setSelected(selected );
        }else {
            int handSize = shortListCards.size();
            boolean canPlay = false;
            // goes through all card to find playable card
            for(int i=0; i<handSize; i++){
                selected = Optional.of(shortListCards.get(i));

                // playing heart card, no need to check anything
                if(isCharacter){
                    pileIndex = selectPile((CardInfo.Suit) selected.get().getSuit());
                    canPlay = true;
                    break;
                }

                CardInfo.Suit suit = (CardInfo.Suit) selected.get().getSuit();
                int rank = ((CardInfo.Rank)selected.get().getRank()).getRankValue();
                pileIndex = selectPile((CardInfo.Suit) selected.get().getSuit());

                // every effect card check if it can change current outcome
                if(canChangeOutcome(board, selected, pileIndex)){
                    // non-diamond cards check if its corresponding diamond card has been played
                    if(!suit.isMagic()){
                        if(notEffective(rank)){
                            // can't play, keep iterating through the cards
                            continue;
                        }
                    }
                    if(Rules.isMoveValid(selected, board.getPiles(pileIndex))){
                        canPlay = true;
                        break;
                    }else {
                        canPlay = false;
                    }
                }
            }
            // go through hand but no card is smart to play so unselect the card
            if(!canPlay){
                selected = Optional.empty();
            }

            // GameUIManager.getGameUI().log("Player " + playerIndex + " plays " + CardInfo.canonical(selected.get()) + " on pile " + pileIndex);
        }
        if (selected.isPresent()) {
            GameUIManager.getGameUI().setStatusText("Selected: " + CardInfo.canonical(selected.get()) + ". Player" + playerIndex + " select a pile to play the card.");
            //pileIndex = selectPile((CardInfo.Suit) selected.get().getSuit());
            board.placeCard(playerIndex, selected, pileIndex);
        }else {
            GameUIManager.getGameUI().setStatusText("Pass.");
        }


    }
    public int selectPile(CardInfo.Suit suit) {
        // always play diamond on enemy pile, the rest effect cards on team pile
        if(suit.isMagic()){
            return (playerIndex+1) % 2;
        }
        return playerIndex % 2;
    }

    private boolean canChangeOutcome(Board board, Optional<Card> selected, int pileIndex){
        int rankArraySize = 2;
        int[] attackRanks = Arrays.copyOf(board.getAttackRanks(), rankArraySize);
        int[] defenceRanks = Arrays.copyOf(board.getDefenceRanks(), rankArraySize);
        // array of boolean containing {winning, losing, draw}
        boolean[] oldSituations = setSituation(attackRanks, defenceRanks);
        int[] newRanks = board.calculatePileRanks(selected, pileIndex);

        // updates attack and defence, only the pile that has been played on will have a difference
        attackRanks[pileIndex] = newRanks[0];
        defenceRanks[pileIndex] = newRanks[1];
        boolean[] newSituations = setSituation(attackRanks, defenceRanks);

        // if array is equal to each other than the outcome is unchanged, thus a '!' in front
        return !Arrays.equals(oldSituations, newSituations);
    }

    private boolean[] setSituation(int[] attackRanks, int[] defenceRanks){
        int teamIndex = playerIndex%2;
        int enemyIndex = (playerIndex+1)%2;
        boolean winning; // our attack compare to their defence
        boolean losing; // our defence compare ot their attack

        if(attackRanks[teamIndex] > defenceRanks[enemyIndex]){
            winning = true;
        }else {
            winning = false;
        }

        if(defenceRanks[teamIndex] < attackRanks[enemyIndex]){
            losing = true;
        }else{
            losing = false;
        }
        boolean[] returnValue = {winning, losing};
        return returnValue;
    }
    private boolean notEffective(int rank){
        if(leftDiamondCards[rank-1] > 0){
            return true;
        }
        return false;
    }

    @Override
    public void update(int rank) {
        leftDiamondCards[rank-1] -= 1;
    }

    private void initLeftDiamondCards(Hand hand){
        for(Card card: hand.getCardList()){
            if(((CardInfo.Suit)card.getSuit()).isMagic()){
                update(((CardInfo.Rank)card.getRank()).getRankValue());
            }
        }
    }
}
