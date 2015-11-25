package dulleh.akhyou.event;

import dulleh.akhyou.anime.Anime;

public class FavoriteEvent {
    public enum Action {
        ADD,
        REMOVE
    }

    public FavoriteEvent.Action action; // if false: remove from favourites
    public Anime anime;

    public FavoriteEvent (FavoriteEvent.Action action, Anime anime) {
        this.action = action;
        this.anime = anime;
    }

}
