package dulleh.akhyou.event;

import dulleh.akhyou.anime.Anime;

public class LastAnimeEvent {
    public final Anime anime;

    public LastAnimeEvent (Anime anime) {
        this.anime = anime;
    }

}
