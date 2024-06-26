package controller;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Flow.*;
import java.util.concurrent.Flow.Publisher;
import java.util.stream.Collectors;


import controller.task.KanjiSubject;
import controller.task.KanjiTask;
import controller.task.MultipleChoiceTask;
import controller.battleaction.Attack;
import model.kanji.Kanji;
import model.kanji.KanjiScheduler;
import model.radicals.Radical;
import model.radicals.RadicalFighter;
import controller.battleaction.*;

import utils.UpdateEvent;
import view.BattleWindow; //TODO access via controller


class FighterUpdateEvent implements UpdateEvent<RadicalFighter[]> {
    private RadicalFighter fighter1;
    private RadicalFighter fighter2;

    public FighterUpdateEvent(RadicalFighter f1, RadicalFighter f2) {
        this.fighter1 = f1;
        this.fighter2 = f2;
    }

    public RadicalFighter[] getData() {
        return new RadicalFighter[]{fighter1, fighter2};
    }
}

public class KanjiBattle implements Publisher<FighterUpdateEvent>, Runnable {
    private RadicalFighter[] team1;
    private RadicalFighter[] team2;
    private int currentTurn;

    private BattleWindow battleWindow;
    private KanjiScheduler kanjiScheduler;
    private Controller parent;

    private List<Subscriber<FighterUpdateEvent>> subscribers;

    public KanjiBattle(BattleWindow battleWindow, Controller parent, KanjiScheduler kanjiScheduler, RadicalFighter[] team1, RadicalFighter[] team2) {
        this.team1 = team1;
        this.team2 = team2;
        this.currentTurn = 0;
        this.parent = parent;
        this.battleWindow = battleWindow;
        this.kanjiScheduler = kanjiScheduler;
        this.subscribers = new ArrayList<Subscriber<FighterUpdateEvent>>();
    }

    @Override
    public void run() {
        subscribe(battleWindow);
        //battleWindow.run();

        //System.out.println(observers);
        //performTurn();
        while (!isBattleOver()) {
            //notifyObservers();
            System.out.println("----- Turn " + (currentTurn + 1) + " -----");
            battleWindow.writeToOutput(("----- Turn " + (currentTurn + 1) + " -----"));

            performTurn();

            //boolean playerLost = Arrays.asList(team1).stream().filter(r -> r.getHP() > 0).noneMatch();
            //boolean opponentLost = Arrays.asList(team2).stream().filter(r -> r.getHP() > 0).noneMatch();


            currentTurn++;
        }
        parent.endBattle();
}

    // TODO inject progress data gathering
    // TODO extract Task logic and write different tasks

    private void performTurn() {

        parent.updateKanjiDex();
        //System.out.println(subscribers.size());


        // TODO implement different tasks in view, have them scheduled here and pass them what they need
        // TODO we still need to handle radical effects, maybe implement kanjiction and do it all at once
        Attack chosenAttack = (Attack) chooseKanjiAction(true); //proficientKanji.get(chosenIndex)
        //System.out.println("Chose: " + chosenAttackKanji.getCharacter());
        List<Kanji> chosenAttackKanjis = chosenAttack.getKanjis();

        // TODO have the scheduler select tasks
        KanjiTask attackTask = new MultipleChoiceTask(parent, chosenAttackKanjis, KanjiSubject.MEANING, battleWindow, 4);

        boolean attackSuccessful = attackTask.performTask();

        //System.out.println(attackSuccessful ? "That's right!" : "Wrong, it's " + expectedMeaning);
        int damageDealt = chosenAttack.getBaseDamage();

        if (attackSuccessful) damageDealt *= 2;

        team2[0].takeDamage(damageDealt);

        //battleWindow.waitContinueCommand();

        Defense chosenDefense = (Defense) chooseKanjiAction(false); //inproficientKanji.get(chosenIndex);
        List<Kanji> chosenDefenseKanjis = chosenDefense.getKanjis();

        // TODO have the scheduler select tasks
        KanjiTask defenseTask = new MultipleChoiceTask(parent, chosenDefenseKanjis, KanjiSubject.MEANING, battleWindow, 4);

        boolean defenseSuccessful = defenseTask.performTask();

        int damageTaken = chosenDefense.getBaseDamage();

        if (defenseSuccessful) damageTaken /= 2;

        team1[0].takeDamage(damageTaken);

        // turn is over, start some update jobs etc
        publish(new FighterUpdateEvent(team1[0], team2[0]));

        // to test at the end of every round update kanjidex and then print current stats
        parent.updateKanjiDex();

        //battleWindow.waitContinueCommand();
    }

    // TODO implement kanji actions (attack, defense with modifiers etc) and return one here
    // @param: attack or defense
    private BattleAction chooseKanjiAction(boolean attack) {

        // TODO prepare kanji/radical/action data to display to the player
        // TODO for example query kanji based on different criteria as wrong answers

        // select 4 kanji the player is proficient with as possible attacks
        // or 4 they are not proficient with as enemy attacks
        List<Kanji> kanjiChoices = new ArrayList<Kanji>();
        for (int i = 0; i < 4; i++) {
            kanjiChoices.add(attack ? kanjiScheduler.getProficientKanji() : kanjiScheduler.getInproficientKanji());
        }

        // starting with 1, for every 10 strokes in a kanji we select one of its components.
        // the components boost is applied to attack and displayed to the player
        List<List<Radical>> radicals = (List<List<Radical>>) kanjiChoices.stream().map(k -> {
            List<Radical> result = new ArrayList<Radical>();
            List<Radical> components = parent.getDB().getRadicalComponents(k.getId());
            if (components == null || components.size() < 1) return result;
            int effectCount = k.getStrokes() / 10 + 1;
            for (int i = 0; i < effectCount; i++) {
                int randomIndex = (int) (Math.random() * (double) effectCount);

                result.add(components.get(randomIndex % components.size()));//.getBoost().getBoostStrings()[0];
            }

            return result;
        }).toList();

        String[] radicalEffects = (String[]) radicals.stream().map(rads -> {
            List<String> resultStrings = new ArrayList<String>();
            rads.stream().forEach(r -> {
                // effect is the attack value of the radicals boost for now
                resultStrings.add(r.getBoost().getBoostStrings()[attack ? 0 : 1]);
            });

            return String.join(";", resultStrings);
        }).toArray(String[]::new);

        // display kanji's power before the effects
        for (int i = 0; i < radicalEffects.length; i++) {
            radicalEffects[i] = Integer.toString(kanjiChoices.get(i).getPower()) + "\n" + radicalEffects[i];
        }

        // have player choose a kanji as attack, set task and then perform attack with updated dmg
        //int rightAnswer = (int) (Math.random() * 4.0); // select random kanji to be the right one
        // choose1 just returns the index
        //String chosenAttackKanji = battleWindow.choose1OutOf4((String[]) proficientKanji.stream().map(Kanji::getCharacter).toArray(String[]::new), "Choose a kanji action:", "Attack selection");
        int chosenIndex = battleWindow.choose1OutOf4(radicalEffects, attack ? "Choose a kanji attack:" : "Choose a kanji defense:");

        // TODO use RadicalBoosts
        //String chosenActionEffect = radicalEffects[chosenIndex];
        Kanji chosenKanji = kanjiChoices.get(chosenIndex);
        List<Radical> chosenRadicals = radicals.get(chosenIndex);

        // just one kanji for now
        List<Kanji> chosenKanjiList = new ArrayList<Kanji>();
        chosenKanjiList.add(chosenKanji);

        BattleAction chosenAction = attack ? new Attack(chosenKanjiList) : new Defense(chosenKanjiList);
        for (int i = 0; i < chosenRadicals.size(); i++) {
            chosenAction.addBoost(chosenRadicals.get(i).getBoost());
        }

        chosenAction.applyBoosts();

        return chosenAction;
    }

    // TODO when we implement multiple fighters this should either be constant or based on speed stat
    private RadicalFighter getNextAttacker() {
        return currentTurn % 2 == 0 ? team1[currentTurn / 2] : team2[currentTurn / 2];
    }

    private RadicalFighter getNextDefender(RadicalFighter attacker) {
        RadicalFighter[] defenders = attacker == team1[currentTurn / 2] ? team2 : team1;
        return defenders[currentTurn / 2];
    }

    public void printBattleState() {
        System.out.println("Current Battle State:");
        System.out.println("Team 1:");
        for (int i = 0; i < 3; i++) {
            RadicalFighter fighter = team1[i];
            System.out.println(fighter.getName() + " (HP: " + fighter.getHP() + ")");
        }
        System.out.println("Team 2:");
        for (int i = 0; i < 3; i++) {
            RadicalFighter fighter = team2[i];
            System.out.println(fighter.getName() + " (HP: " + fighter.getHP() + ")");
        }
        System.out.println();
    }

    private boolean isTeamDefeated(List<RadicalFighter> fighters) {
        return fighters.stream().noneMatch(r -> r.getHP() > 0);
    }

    private boolean isBattleOver() {
        // TODO add winnings etc
        if (isTeamDefeated(Arrays.asList(team1))) {
            System.out.println("Player was defeated");
            return true;
        } else if (isTeamDefeated(Arrays.asList(team2))) {
            System.out.println("Enemy was defeated");
            return true;
        }
        return false;
    }

    public int getOpponentHealth() {
        return team1[0].getHP();
    }

    public int getPlayerHealth() {
        return team2[0].getHP();
    }

    //Publisher interface
    @Override
    public void subscribe(Subscriber<? super FighterUpdateEvent> subscriber) {
        subscribers.add((Subscriber<FighterUpdateEvent>) subscriber);
    }
    public void publish(FighterUpdateEvent event) {
        subscribers.forEach(subscriber -> subscriber.onNext(event));
    }
}
