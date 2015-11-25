package dulleh.akhyou.event;

import dulleh.akhyou.anime.Anime;

public class FavoriteEvent {
    public FavoriteAction action; // if false: remove from favourites
    public Anime anime;

    public FavoriteEvent (FavoriteAction action, Anime anime) {
        this.action = action;
        this.anime = anime;
    }

}
