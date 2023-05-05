package model.kanji;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kanji {
    private int id;
    private int proficiency;
    private String character;
    private List<Integer> grades;
    private int strokes;
    private int power;
    private List<String> kunyomi;
    private List<String> translations;
    private String radicalName;
    private Map<String, Integer> radicals;

    public Kanji(int id, String character, List<Integer> grades, int strokes, List<String> kunyomi, List<String> translations, String radicalName) {
        this.id = id;
        this.proficiency = 5;
        this.character = character;
        this.grades = grades;
        this.strokes = strokes;
        this.power = 1 + (this.strokes % 10);
        this.kunyomi = kunyomi;
        this.translations = translations;
        this.radicalName = radicalName;
        this.radicals = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public int getProficiency() {
        return proficiency;
    }

    public String getCharacter() {
        return character;
    }

    public List<Integer> getGrades() {
        return grades;
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

    public List<String> getTranslations() {
        return translations;
    }

    public String getRadicalName() {
        return radicalName;
    }

    public Map<String, Integer> getRadicals() {
        return radicals;
    }

    public void incrProf() {
        proficiency = (Math.max(proficiency + 1, 0)) % 100;
    }

    public void decrProf() {
        proficiency = (Math.max(proficiency - 1, 0)) % 100;
    }

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

