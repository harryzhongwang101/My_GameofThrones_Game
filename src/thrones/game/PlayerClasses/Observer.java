package thrones.game.PlayerClasses;

public interface Observer {
    default void update(int rank){
        return;
    }
}
