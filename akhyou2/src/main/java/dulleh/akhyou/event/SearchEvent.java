package dulleh.akhyou.event;

public class SearchEvent {
    public final String searchTerm;

    public SearchEvent (String searchTerm) {
        this.searchTerm = searchTerm;
    }
}