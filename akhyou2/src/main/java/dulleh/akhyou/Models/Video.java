package dulleh.akhyou.Models;

import java.io.Serializable;

import lombok.Data;

@Data
public class Video implements Serializable {
    private final String title;
    private final String url;
}
