package controller.task;

import controller.Controller;
import model.kanji.Kanji;
import view.BattleWindow;
import view.task.TaskUI;

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

    public List<Kanji> getKanjis() {return kanjis;}

    public KanjiSubject getSubject() {return subject;}
}
