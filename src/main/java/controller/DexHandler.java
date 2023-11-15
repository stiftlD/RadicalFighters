package controller;

import model.kanji.Kanji;
import view.DexWindow;
import model.kanji.KanjiDex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// this class is supposed to handle on the kanjiDex,
// as well as displaying kanji data from the dex in dexwindow
public class DexHandler<T> {
    private Controller parent;
    private DexWindow window;
    private KanjiDex dex;

    public DexHandler(Controller parent) {
        this.parent = parent;
    }

    public void setDex(KanjiDex dex) {
        if (this.dex != null) return;
        this.dex = dex;
    }

    public void setWindow(DexWindow window) {
        if (this.window != null) return;
        this.window = window;
    }

    // select a kanji by index in dex list, get info from dex and display in dexwindow
    public void displayKanjiDexEntry(int id) {
        // TODO need to implement and use other methods in dex
        System.out.println("list length: " + dex.getKanjiRanking().size());
        dex.printRankedKanjiList();
        Kanji k = parent.getDB().getKanjiByID(id); //TODO look it up in the dex who already has that data instead
        window.showKanjiEntry(k.getCharacter(), k.getOnyomi().get(0), k.getKunyomi().get(0), k.getTranslations().get(0),
                k.getGrade(), k.getStrokes(), k.getProficiency());
    }

    // get some statistic regarding study history from dex, and display as a chart in battlewindow
    // TODO also refactor
    public void displayStudyStatisticChart() {
        Map<Integer, Double> map = parent.getStudyService().getAverageProficencyByGrade();
        List<Integer> gradeList = new ArrayList<Integer>();
        List<Double> proficiencyList = new ArrayList<Double>();
        map.forEach((grade, prof) -> {
            gradeList.add(grade);
            proficiencyList.add(prof);
        });
        window.setBarPlot("AVG proficiency by grade", gradeList, proficiencyList, "grades", "avg proficiency");
    }
}
