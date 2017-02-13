package xyz.sayangoswami.urbandictionary;

/**
 * Created by Sayan on 21/08/16.
 */
public class Definitions {
    /**
     * Definition adapter to store each definition for use with recycler view.
     */
    private String definition, author, word;
    private int thumb_up, thumb_down, def_id;
    private String vote = "null";

    public Definitions(String definition, String author, int thumb_up, int thumb_down, int def_id, String word) {
        setId(def_id);
        setDefinition(definition);
        setAuthor(author);
        setThumb_up(thumb_up);
        setThumb_down(thumb_down);
        setWord(word);
    }
    public Definitions(){
        this.author = "Undefined";
        this.definition = "Undefined";
        this.thumb_up = -1;
        this.thumb_down = -1;
        this.word = "Undefined";
    }

    public void setWord(String word){ this.word = word;}

    public String getWord() { return this.word;}

    public void setVote(String myVote){ this.vote = myVote;}

    public String getVote() {return vote;}

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getThumb_up() {
        return thumb_up;
    }

    public void setThumb_up(int thumb_up) {
        this.thumb_up = thumb_up;
    }

    public int getThumb_down() {
        return thumb_down;
    }

    public void setThumb_down(int thumb_down) {
        this.thumb_down = thumb_down;
    }

    public int getId() {return def_id;}

    public void setId(int id) { this.def_id = id; }
}
