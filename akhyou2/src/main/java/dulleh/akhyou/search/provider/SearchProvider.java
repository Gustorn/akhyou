package dulleh.akhyou.search.provider;

import java.util.List;

import dulleh.akhyou.anime.Anime;
import rx.exceptions.OnErrorThrowable;

public interface SearchProvider {
    List<Anime> searchFor (String searchTerm) throws OnErrorThrowable;
}
