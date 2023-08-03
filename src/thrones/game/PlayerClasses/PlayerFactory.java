package thrones.game.PlayerClasses;

import ch.aplu.jcardgame.Hand;

import java.util.Random;

public class PlayerFactory {

    public PlayerFactory() {}

    public void createPlayers(int nbPlayers, Player[] players, String[] playerType, Hand[] hands, Random random) {
        for(int i=0; i<nbPlayers; i++){
            switch (playerType[i]){
                case "random":
                    players[i] = new RandomPlayer(hands[i], i, random);
                    break;
                case "simple":
                    players[i] = new SimplePlayer(hands[i], i, random);
                    break;
                case "human":
                    players[i] = new HumanPlayer(hands[i], i);
                    break;
                case "smart":
                    players[i] = new SmartPlayer(hands[i], i);
                    break;
                default:
                    players[i] = new RandomPlayer(hands[i], i, random);
            }
        }
        //return players;
    }
}
