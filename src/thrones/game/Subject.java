package thrones.game;

import thrones.game.PlayerClasses.Player;

public interface Subject {
    void notifyObservers(int rank); //notify() will have override error
    void addObserver(Player player);
}
