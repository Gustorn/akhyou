package dulleh.akhyou.Models.SearchProviders;

import java.util.List;

import dulleh.akhyou.Models.Anime;
import rx.exceptions.OnErrorThrowable;

public interface SearchProvider {
    List<Anime> searchFor (String searchTerm) throws OnErrorThrowable;
}
