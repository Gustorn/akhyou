package dulleh.akhyou.event;

public class SearchSubmittedEvent {
    public final String searchTerm;

    public SearchSubmittedEvent (String searchTerm) {
        this.searchTerm = searchTerm;
    }
}
