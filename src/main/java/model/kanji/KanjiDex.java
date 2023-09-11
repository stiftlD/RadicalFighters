package model.kanji;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import controller.Controller;

public class KanjiDex {
    private Controller controller;
    private List<Kanji> rankedKanjiList;

    public KanjiDex(Controller controller) {
        rankedKanjiList = new ArrayList<Kanji>();
    }

    public void setRankedKanjiList(List<Kanji> rankedKanjiList) {
        this.rankedKanjiList = rankedKanjiList;
    }

    public void printRankedKanjiList() {
        System.out.println("Studied Kanji ranked by proficiency (total " + rankedKanjiList.size() + "): ");
        rankedKanjiList.stream().forEach(k -> {
            System.out.print(k.getCharacter() + " ");
        });
        System.out.print("\n");
    }

    public List<Kanji> getRankedKanjiList() { return this.rankedKanjiList; }

}
