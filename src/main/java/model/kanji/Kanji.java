package model.kanji;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kanji {
    private int id;
    private int proficiency;
    private String character;
    private int grade;
    private int strokes;
    private int power;
    private List<String> kunyomi;
    private List<String> onyomi;
    private List<String> translations;
    private String unicode;
    private boolean encountered;
    private Map<String, Integer> radicals;

    // TODO more of these params could probably be set later
    public Kanji(int id, String character, int grade, int strokes, List<String> kunyomi, List<String> onyomi, List<String> translations, String unicode, int proficiency, boolean encountered) {
        this.id = id;
        this.proficiency = proficiency;
        this.character = character;
        this.grade = grade;
        this.strokes = strokes;
        this.power = 1 + (this.strokes / 10);
        this.kunyomi = kunyomi;
        this.onyomi = onyomi;
        this.translations = translations;
        this.unicode = unicode;
        this.encountered = encountered;
        this.radicals = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public int getProficiency() {
        return proficiency;
    }

    public String getCharacter() {

        if (character == null) return "";
        return character;
    }

    public int getGrade() {
        return grade;
    }

    public int getStrokes() {
        return strokes;
    }

    public int getPower() {
        return power;
    }

    public List<String> getKunyomi() {
        return kunyomi;
    }

    public List<String> getOnyomi() {return onyomi;}

    public List<String> getTranslations() {
        return translations;
    }

    public String getUnicode() {
        return unicode;
    }

    public boolean getEncountered() {return encountered;}

    public Map<String, Integer> getRadicals() {
        return radicals;
    }

    public void incrProf() {
        proficiency = (Math.max(proficiency + 1, 0)) % 100;
    }

    public void decrProf() {
        proficiency = (Math.max(proficiency - 1, 0)) % 100;
    }

    // TODO remove this! and only get proficiency info from studyservice if possible
    public void setProficiency(int prof) {proficiency = prof;}

    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Kanji other = (Kanji) obj;
        return id == other.id;
    }

    public int hashCode() {
        return id;
    }
}

