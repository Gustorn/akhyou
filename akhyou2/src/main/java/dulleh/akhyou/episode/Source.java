package dulleh.akhyou.episode;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Source implements Serializable {
    private String title;
    private String providerName;
    private List<Video> videos;
}
