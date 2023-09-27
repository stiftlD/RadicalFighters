package controller;

import model.kanji.Kanji;
import view.DexWindow;
import model.kanji.KanjiDex;

import java.util.stream.Collectors;

// this class is supposed to handle on the kanjiDex,
// as well as displaying kanji data from the dex in dexwindow
public class DexHandler {
    private Controller parent;

    public DexHandler(Controller parent) {
        this.parent = parent;
    }

    // select a kanji by id, get info from dex and display in dexwindow
    public void displayKanjiDexEntry(DexWindow window, KanjiDex dex, int id) {
        // TODO need to implement and use other methods in dex
        System.out.println("list length: " + dex.getKanjiRanking().size());
        dex.printRankedKanjiList();
        Kanji k = dex.getKanjiRanking().get(id);
        window.showKanjiEntry(k.getCharacter(), k.getOnyomi().get(0), k.getTranslations().get(0),
                k.getGrade(), k.getStrokes(), k.getProficiency());
    }
}
