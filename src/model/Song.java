package model;

import javax.persistence.*;

@Entity
@Table(name = "Song")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;
    private String lyrics;
    private String audioPath;
    private String imagePath;

    public Song(int id, String title, String lyrics, String imagePath, String audioPath) {
        super();
        this.id = id;
        this.title = title;
        this.lyrics = lyrics;
        this.imagePath = imagePath;
        this.audioPath = audioPath;
    }

    public Song() {
        super();
    }

    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLyrics() {
        StringBuilder output = new StringBuilder();
        int length = lyrics.length();
        for (int i = 0; i < length; i++) {
            char currentChar = lyrics.charAt(i);
            if (currentChar == '\\' && i + 1 < length) {
                char nextChar = lyrics.charAt(i + 1);
                if (nextChar == '\\' || nextChar == '"') {
                    output.append(nextChar);
                    i++;
                } else {
                    output.append(currentChar);
                }
            } else {
                output.append(currentChar);
            }
        }
        lyrics = output.toString();
        lyrics = lyrics.replace("\\n", System.lineSeparator());

        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }
}
