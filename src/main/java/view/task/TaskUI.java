package view.task;

import view.BattleWindow;

public class TaskUI {
    private BattleWindow parent;

    public TaskUI(BattleWindow parent) {
        this.parent = parent;
    }

    public BattleWindow getParent() { return parent; }
}
