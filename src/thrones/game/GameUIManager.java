package thrones.game;

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.GameGrid;
import ch.aplu.jgamegrid.Location;
import ch.aplu.jgamegrid.TextActor;
import thrones.game.PlayerClasses.Player;

import java.awt.*;

public class GameUIManager extends CardGame {

    private static GameUIManager Instance = null;

    private GameUIManager() {
        super(700, 700, 30);
    }

    public static GameUIManager getGameUI(){
        if (Instance == null) {
            Instance = new GameUIManager();
        }
        return Instance;
    }

    /* -------------------------------------------------------- */

    private final Location[] handLocations = {
            new Location(350, 625),
            new Location(75, 350),
            new Location(350, 75),
            new Location(625, 350)
    };

    private final Location[] scoreLocations = {
            new Location(575, 675),
            new Location(25, 575),
            new Location(25, 25),
            new Location(575, 125)
    };
    private final Location[] pileLocations = {
            new Location(350, 280),
            new Location(350, 430)
    };
    private final Location[] pileStatusLocations = {
            new Location(250, 200),
            new Location(250, 520)
    };

    Font bigFont = new Font("Arial", Font.BOLD, 36);
    Font smallFont = new Font("Arial", Font.PLAIN, 10);
    private final int handWidth = 400;
    private final int pileWidth = 40;


    private Actor[] pileTextActors = { null, null };
    private Actor[] scoreActors = {null, null, null, null};



    public void updateScore(int player, int score) {
        if (scoreActors[player] != null) {
            removeActor(scoreActors[player]);
        }
        String text = "P" + player + "-" + score;
        scoreActors[player] = new TextActor(text, Color.WHITE, bgColor, bigFont);
        addActor(scoreActors[player], scoreLocations[player]);
    }

    public void updatePileRankState(int pileIndex, String team, int attackRank, int defenceRank) {
        TextActor currentPile = (TextActor) pileTextActors[pileIndex];
        if (currentPile != null) {
            removeActor(currentPile);
        }
        String text = team + " Attack: " + Math.max(attackRank, 0) + " - Defence: " + Math.max(defenceRank, 0);
        pileTextActors[pileIndex] = new TextActor(text, Color.WHITE, bgColor, smallFont);
        addActor(pileTextActors[pileIndex], pileStatusLocations[pileIndex]);
    }

    public void displayPile(int pileIndex, Hand currPile) {
        currPile.setView(this, new RowLayout(pileLocations[pileIndex], 8 * pileWidth));
        currPile.draw();
    }

    public void displayHands(int nbPlayers, Player[] players) {
        RowLayout[] layouts = new RowLayout[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            layouts[i] = new RowLayout(handLocations[i], handWidth);
            layouts[i].setRotationAngle(90 * i);
            players[i].getCurrentHand().setView(this, layouts[i]);
            players[i].getCurrentHand().draw();
        }
    }

    public void refresh() {
        super.refresh();
    }

    public void log(String msg) {
        System.out.println(msg);
    }

    public void setTitle(String title) {
        super.setTitle(title);
    }

    public void setStatusText(String statusText) {
        super.setStatusText(statusText);
    }

    public void delay(int time) {
        GameGrid.delay(time);
    }


}
