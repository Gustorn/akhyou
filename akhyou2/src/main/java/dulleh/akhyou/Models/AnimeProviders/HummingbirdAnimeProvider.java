package dulleh.akhyou.Models.AnimeProviders;

import java.util.List;

import dulleh.akhyou.Models.Anime;
import dulleh.akhyou.Models.Source;
import rx.exceptions.OnErrorThrowable;

public class HummingbirdAnimeProvider implements AnimeProvider {
    @Override
    public Anime fetchAnime(String url) throws OnErrorThrowable {
        return null;
    }

    @Override
    public Anime updateCachedAnime(Anime cachedAnime) throws OnErrorThrowable {
        return cachedAnime;
    }

    @Override
    public List<Source> fetchSources(String url) throws OnErrorThrowable {
        return null;
    }

    @Override
    public Source fetchVideo(Source source) throws OnErrorThrowable {
        return null;
    }
}
