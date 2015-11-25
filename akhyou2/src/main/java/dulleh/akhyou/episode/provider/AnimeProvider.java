package dulleh.akhyou.episode.provider;

import java.util.List;

import dulleh.akhyou.anime.Anime;
import dulleh.akhyou.episode.Source;
import rx.exceptions.OnErrorThrowable;

public interface AnimeProvider {
    Anime updateCachedAnime (Anime cachedAnime) throws OnErrorThrowable;
    List<Source> fetchSources (Anime anime) throws OnErrorThrowable;
    Source fetchVideo (Source source) throws OnErrorThrowable;

}
