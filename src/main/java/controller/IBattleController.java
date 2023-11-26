package controller;


import controller.task.IGameController;
import view.BattleWindow;

public interface IBattleController extends IGameController {

    public void startBattle(BattleWindow window);

    public void endBattle();

}
