package thrones.game;

// Oh_Heaven.java

import ch.aplu.jcardgame.*;
import thrones.game.PlayerClasses.Player;
import thrones.game.PlayerClasses.PlayerFactory;
import utility.PropertiesLoader;

import java.util.*;

@SuppressWarnings("serial")
public class GameOfThrones {

    public static void main(String[] args) {
        String propertiesPath = DEFAULT_PROPERTIES_PATH;
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        Properties properties = new Properties();
        //properties.setProperty("watchingTime", "5000");

        if (args == null || args.length == 0) {
            properties = PropertiesLoader.loadPropertiesFile(propertiesPath);
        } else {
            properties = PropertiesLoader.loadPropertiesFile(args[0]);
        }

        String seedProp = properties.getProperty("seed", "130006");  //Seed property
        if (seedProp != null) { // Use property seed
            seed = Integer.parseInt(seedProp);
        } else { // and no property
            seed = new Random().nextInt(); // so randomise
        }

        //GameOfThrones.seed = 130006; // should be commented out later
        System.out.println("Seed = " + seed);
        GameOfThrones.random = new Random(seed);
        new GameOfThrones(properties);
    }


    private GameUIManager gameUI;
    private final String version = "1.0";
    private final String TITLE = "Game of Thrones (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)";
    private Board gameBoard;

    public GameOfThrones(Properties properties) {
        gameUI = GameUIManager.getGameUI();
        initWithProperties(properties);

        random = new Random(seed);
        setupGame();
        for (int i = 0; i < nbPlays; i++) {
            try {
                executeAPlay();
            } catch (BrokeRuleException e) {
                e.printStackTrace();
            }
            updateScores();
        }

        String text;
        if (players[0].getScore() > players[1].getScore()) {
            text = "Players 0 and 2 won.";
        } else if (players[0].getScore() == players[1].getScore()) {
            text = "All players drew.";
        } else {
            text = "Players 1 and 3 won.";
        }
        gameUI.log("Result: " + text);
        gameUI.setStatusText(text);

        gameUI.refresh();
    }

    private void executeAPlay() throws BrokeRuleException {
        gameBoard.resetPiles(deck);

        nextStartingPlayer = getPlayerIndex(nextStartingPlayer);
        if (players[nextStartingPlayer].getCurrentHand().getNumberOfCardsWithSuit(CardInfo.Suit.HEARTS) == 0)
            nextStartingPlayer = getPlayerIndex(nextStartingPlayer + 1);
        assert players[nextStartingPlayer].getCurrentHand().getNumberOfCardsWithSuit(CardInfo.Suit.HEARTS) != 0 : " Starting player has no hearts.";

        // 1: play the first 2 hearts
        for (int i = 0; i < 2; i++) {
            int playerIndex = getPlayerIndex(nextStartingPlayer + i);
            gameUI.setStatusText("Player " + playerIndex + " select a Heart card to play");
            players[playerIndex].playACard(gameBoard, true);
        }

        // 2: play the remaining nbPlayers * nbRounds - 2
        int remainingTurns = nbPlayers * nbRounds - 2;
        int nextPlayer = nextStartingPlayer + 2;

        while(remainingTurns > 0) {
            nextPlayer = getPlayerIndex(nextPlayer);
            gameUI.setStatusText("Player" + nextPlayer + " select a non-Heart card to play.");
            players[nextPlayer].playACard(gameBoard,false); // is character is false because not playing heart cards

            nextPlayer++;
            remainingTurns--;
        }

        gameBoard.calculateRoundWinner(players);

        // 3: discarded all cards on the piles
        nextStartingPlayer += 1;
        gameUI.delay(watchingTime);
    }

    private void setupGame() {

        // Set up game window
        gameUI.setTitle(TITLE);
        gameUI.setStatusText("Initializing...");

        // Create board
        gameBoard = new Board(playerTeams);

        // Create and deal players
        Hand[] hands = new Hand[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            hands[i] = new Hand(deck);
        }
        dealingOut(hands, nbPlayers, nbStartCards);

        // initialize players
        playerFactory.createPlayers(nbPlayers, players, playerType, hands, random);
        // initialize observers
        initBoardObservers(gameBoard);

        for (int i = 0; i < nbPlayers; i++) {
            players[i].getCurrentHand().sort(Hand.SortType.SUITPRIORITY, true);
            gameUI.log("hands[" + i + "]: " + CardInfo.canonical(players[i].getCurrentHand()));
            gameUI.updateScore(i, players[i].getScore());
        }

        // graphics
        gameUI.displayHands(nbPlayers, players);
        // End graphics
    }

    public final int nbPlayers = 4;
    public final int nbStartCards = 9;
    public final int nbPlays = 6;
    public final int nbRounds = 3;

    private final String[] playerTeams = { "[Players 0 & 2]", "[Players 1 & 3]"};
    private int nextStartingPlayer = random.nextInt(nbPlayers);

    private Player[] players = new Player[nbPlayers];
    private String[] playerType = new String[nbPlayers];
    private PlayerFactory playerFactory = new PlayerFactory();

    private int getPlayerIndex(int index) {
        return index % nbPlayers;
    }

    private void updateScores() {
        for (int i = 0; i < nbPlayers; i++) {
            gameUI.updateScore(i, players[i].getScore());
        }
        gameUI.log(playerTeams[0] + " score = " + players[0].getScore() + "; " + playerTeams[1] + " score = " + players[1].getScore());
    }


    static private int seed;
    static Random random;
    private Deck deck = new Deck(CardInfo.Suit.values(), CardInfo.Rank.values(), "cover");

    // return random Card from Hand
    public static Card randomCard(Hand hand) {
        assert !hand.isEmpty() : " random card from empty hand.";
        int x = random.nextInt(hand.getNumberOfCards());
        return hand.get(x);
    }

    private void dealingOut(Hand[] hands, int nbPlayers, int nbCardsPerPlayer) {
        Hand pack = deck.toHand(false);
        assert pack.getNumberOfCards() == 52 : " Starting pack is not 52 cards.";
        // Remove 4 Aces
        List<Card> aceCards = pack.getCardsWithRank(CardInfo.Rank.ACE);
        for (Card card : aceCards) {
            card.removeFromHand(false);
        }
        assert pack.getNumberOfCards() == 48 : " Pack without aces is not 48 cards.";
        // Give each player 3 heart cards
        for (int i = 0; i < nbPlayers; i++) {
            for (int j = 0; j < 3; j++) {
                List<Card> heartCards = pack.getCardsWithSuit(CardInfo.Suit.HEARTS);
                int x = random.nextInt(heartCards.size());
                Card randomCard = heartCards.get(x);
                randomCard.removeFromHand(false);
                hands[i].insert(randomCard, false);
            }
        }
        assert pack.getNumberOfCards() == 36 : " Pack without aces and hearts is not 36 cards.";
        // Give each player 9 of the remaining cards
        for (int i = 0; i < nbCardsPerPlayer; i++) {
            for (int j = 0; j < nbPlayers; j++) {
                assert !pack.isEmpty() : " Pack has prematurely run out of cards.";
                Card dealt = randomCard(pack);
                dealt.removeFromHand(false);
                hands[j].insert(dealt, false);
            }
        }
        for (int j = 0; j < nbPlayers; j++) {
            assert hands[j].getNumberOfCards() == 12 : " Hand does not have twelve cards.";
        }
    }

    // Initialise object
    public static final String DEFAULT_PROPERTIES_PATH = "properties/smart.properties";
    private int watchingTime;// = 5000;

    private void initWithProperties(Properties properties) {
        watchingTime = Integer.parseInt(properties.getProperty("watchingTime", "5000"));

        // put in the playertype as strings store in string array
        for(int i=0; i<nbPlayers; i++){
            playerType[i] = properties.getProperty("players."+i, "random");
        }
    }

    private void initBoardObservers(Board board){
        String observer = "smart";
        for(int i=0; i<nbPlayers; i++){
            if(playerType[i].equals(observer)){
                board.addObserver(players[i]);
            }
        }
    }





}
