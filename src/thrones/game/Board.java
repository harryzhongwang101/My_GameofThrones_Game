package thrones.game;

import ch.aplu.jcardgame.*;
import thrones.game.PlayerClasses.Player;

import java.util.ArrayList;
import java.util.Optional;

public class Board extends CardGame implements Subject{

    private GameUIManager gameUI;

    private final int numPiles = 2;
    private Hand[] piles;
    private int[] attackRanks = new int[numPiles];
    private int[] defenceRanks = new int[numPiles];
    private String[] playerTeams;
    private ArrayList<Player> observers = new ArrayList<Player>();

    private final int NON_SELECTION_VALUE = -1;
    private int selectedPileIndex = NON_SELECTION_VALUE;
    private final int ATTACK_RANK_INDEX = 0;
    private final int DEFENCE_RANK_INDEX = 1;


    public Board(String[] playerTeams) {
        gameUI = GameUIManager.getGameUI();
        this.playerTeams = playerTeams;


        for (int pileIndex = 0; pileIndex < numPiles; pileIndex++) {
            attackRanks[pileIndex] = 0;
            defenceRanks[pileIndex] = 0;
            gameUI.updatePileRankState(pileIndex, playerTeams[pileIndex], attackRanks[pileIndex], defenceRanks[pileIndex]);
        }
    }

    public void placeCard(int playerIndex, Optional<Card> cardToPlace, int pileIndex) {
        // throw exception when move is invalid, should not catch any exception if implement correctly
        try {
            Rules.isMoveLegal(cardToPlace, piles[pileIndex]);
        } catch (BrokeRuleException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        assert cardToPlace.isPresent() : " Pass returned on selection of character.";
        gameUI.log("Player " + playerIndex + " plays " + CardInfo.canonical(cardToPlace.get()) + " on pile " + pileIndex);

        CardInfo.Suit suit = (CardInfo.Suit) cardToPlace.get().getSuit();
        if(suit.isMagic()){
            notifyObservers(((CardInfo.Rank)cardToPlace.get().getRank()).getRankValue());
        }

        int[] ranks = calculatePileRanks(cardToPlace, pileIndex);
        cardToPlace.get().setVerso(false);
        cardToPlace.get().transfer(piles[pileIndex], true);
        updatePileRanks(ranks, pileIndex);
    }

    private void updatePileRanks(int[] ranks, int pileIndex) {
        attackRanks[pileIndex] = ranks[ATTACK_RANK_INDEX];
        defenceRanks[pileIndex] = ranks[DEFENCE_RANK_INDEX];
        gameUI.updatePileRankState(pileIndex, playerTeams[pileIndex], attackRanks[pileIndex], defenceRanks[pileIndex]);
    }

    public int[] calculatePileRanks(Optional<Card> cardToPlay, int pileIndex) {
        Hand currentPile = piles[pileIndex];

        int atk = 0;
        int def = 0;
        int multiplier = 1;
        int pileSize = currentPile.getCardList().size();

        CardInfo.Suit toPlaySuit = (CardInfo.Suit) cardToPlay.get().getSuit();
        CardInfo.Rank toPlayRank = (CardInfo.Rank) cardToPlay.get().getRank();

        if (pileSize == 0) {
            atk = toPlayRank.getRankValue();
            def = toPlayRank.getRankValue();
            return new int[] { atk, def };
        } else {

            int lastIndex = pileSize - 1;

            Card prevCard = currentPile.get(lastIndex);
            CardInfo.Suit prevSuit;
            CardInfo.Rank prevRank = (CardInfo.Rank) prevCard.getRank();

            if (toPlayRank.getRankValue() == prevRank.getRankValue()) {
                multiplier *= 2;
            }

            if (toPlaySuit.isAttack()) {

                atk = toPlayRank.getRankValue() * multiplier;

            } else if (toPlaySuit.isDefence()) {

                def = toPlayRank.getRankValue() * multiplier;

            } else if (toPlaySuit.isMagic()) {

                multiplier *= -1;

                int i = lastIndex;
                while (i >= 0) {

                    prevCard = currentPile.get(i);
                    prevSuit = (CardInfo.Suit) prevCard.getSuit();

                    if (prevSuit.isAttack()) {
                        atk = toPlayRank.getRankValue() * multiplier;
                        break;
                    } else if (prevSuit.isDefence()) {
                        def = toPlayRank.getRankValue() * multiplier;
                        break;
                    } else if (prevSuit.isMagic()) {
                        i--;
                    } else if (prevSuit.isCharacter()) {
                        break;
                    }
                }

            }

            atk = attackRanks[pileIndex] + atk;
            def = defenceRanks[pileIndex] + def;

        }

        return new int[] { atk, def };
    }


    public void resetPiles(Deck deck) {
        if (piles != null) {
            for (Hand pile : piles) {
                pile.removeAll(true);
            }
        }
        piles = new Hand[numPiles];
        for (int i = 0; i < numPiles; i++) {
            piles[i] = new Hand(deck);
            gameUI.displayPile(i, piles[i]);
            final Hand currentPile = piles[i];
            final int pileIndex = i;
            piles[i].addCardListener(new CardAdapter() {
                public void leftClicked(Card card) {
                    selectedPileIndex = pileIndex;
                    currentPile.setTouchEnabled(false);
                }
            });
            attackRanks[i] = 0;
            defenceRanks[i] = 0;
            gameUI.updatePileRankState(i, playerTeams[i], attackRanks[i], defenceRanks[i]);
        }
    }

    public void calculateRoundWinner(Player[] players) {

        for (int i = 0; i < numPiles; i++) {
            attackRanks[i] = Math.max(attackRanks[i], 0);
            defenceRanks[i] = Math.max(defenceRanks[i], 0);
        }

        gameUI.log("piles[0]: " + CardInfo.canonical(piles[0]));
        gameUI.log("piles[0] is " + "Attack: " + attackRanks[0] + " - Defence: " + defenceRanks[0]);
        gameUI.log("piles[1]: " + CardInfo.canonical(piles[1]));
        gameUI.log("piles[1] is " + "Attack: " + attackRanks[1] + " - Defence: " + defenceRanks[1]);

        CardInfo.Rank pile0CharacterRank = (CardInfo.Rank) piles[0].getCardList().get(0).getRank();
        CardInfo.Rank pile1CharacterRank = (CardInfo.Rank) piles[1].getCardList().get(0).getRank();
        String character0Result;
        String character1Result;


        if (attackRanks[0] > defenceRanks[1]) {
            players[0].addScore(pile1CharacterRank.getRankValue());
            players[2].addScore(pile1CharacterRank.getRankValue());
            character0Result = "Character 0 attack on character 1 succeeded.";
        } else {
            players[1].addScore(pile1CharacterRank.getRankValue());
            players[3].addScore(pile1CharacterRank.getRankValue());
            character0Result = "Character 0 attack on character 1 failed.";
        }

        if (attackRanks[1] > defenceRanks[0]) {
            players[1].addScore(pile0CharacterRank.getRankValue());
            players[3].addScore(pile0CharacterRank.getRankValue());
            character1Result = "Character 1 attack on character 0 succeeded.";
        } else {
            players[0].addScore(pile0CharacterRank.getRankValue());
            players[2].addScore(pile0CharacterRank.getRankValue());
            character1Result = "Character 1 attack character 0 failed.";
        }

        gameUI.log(character0Result);
        gameUI.log(character1Result);
        gameUI.setStatusText(character0Result + " " + character1Result);
    }

    public int waitForPileSelection() {
        selectedPileIndex = NON_SELECTION_VALUE;
        for (Hand pile : piles) {
            pile.setTouchEnabled(true);
        }
        while(selectedPileIndex == NON_SELECTION_VALUE) {
            GameUIManager.getGameUI().delay(100);
        }
        for (Hand pile : piles) {
            pile.setTouchEnabled(false);
        }

        return selectedPileIndex;
    }

    @Override
    public void addObserver(Player player) {
        observers.add(player);
    }

    @Override
    public void notifyObservers(int rank) {
        for(Player player: observers){
            player.update(rank);
        }
    }

    public Hand getPiles(int pileIndex){
        return piles[pileIndex];
    }

    public int[] getAttackRanks() {
        return attackRanks;
    }

    public int[] getDefenceRanks() {
        return defenceRanks;
    }
}
