package dulleh.akhyou.event;

import dulleh.akhyou.anime.Anime;

public class OpenAnimeEvent {
    public final Anime anime;

    public OpenAnimeEvent(Anime anime) {
        this.anime = anime;
    }

}