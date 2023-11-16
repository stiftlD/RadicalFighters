package controller.task;

import controller.Controller;
import model.kanji.Kanji;
import view.BattleWindow;
import view.task.TaskUI;

import java.sql.Timestamp;
import java.util.List;

// a kanji task holds logic for revising a certain property for one or more kanjis
// TODO maybe it should r
public abstract class KanjiTask {
    protected Controller controller;
    protected List<Kanji> kanjis;
    protected KanjiSubject subject;
    protected TaskUI ui;

    public KanjiTask(Controller controller, List<Kanji> kanjis, KanjiSubject subject, BattleWindow window) {
        this.controller = controller;
        this.kanjis = kanjis;
        this.subject = subject;
        this.ui = new TaskUI(window);
    }

    public abstract boolean performTask();

    // this is a mandatory step so perhaps above should not be abstract?
    public void logResults(Timestamp start_time, Timestamp finish_time, boolean success) {

        kanjis.forEach(k -> {
            controller.getDB().appendStudyLog(k.getId(), "ABCD", "Meaning", start_time, finish_time, success);
        });
    }

    public List<Kanji> getKanjis() {return kanjis;}

    public KanjiSubject getSubject() {return subject;}
}
