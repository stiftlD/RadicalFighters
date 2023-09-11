package controller;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Flow.*;
import java.util.concurrent.Flow.Publisher;
import java.util.stream.Collectors;


import model.kanji.Kanji;
import model.kanji.KanjiScheduler;
import model.radicals.Radical;
import model.radicals.RadicalFighter;
import model.battleaction.*;

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

public class KanjiBattle implements Publisher<FighterUpdateEvent> {
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

    public void start() {
        subscribe(battleWindow);
        battleWindow.run();

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
    }

    // TODO inject progress data gathering
    // TODO extract Task logic and write different tasks

    private void performTurn() {
        //System.out.println(subscribers.size());
        // select 4 players the player is proficient with as possible attacks and 4 they are not proficient with as enemy attacks
        List<Kanji> proficientKanji = new ArrayList<Kanji>();
        List<Kanji> inproficientKanji = new ArrayList<Kanji>();
        for (int i = 0; i < 4; i++) {
            proficientKanji.add(kanjiScheduler.getProficientKanji());
            inproficientKanji.add(kanjiScheduler.getProficientKanji());
        }

        // TODO implement different tasks in view, have them scheduled here and pass them what they need
        // TODO prepare kanji/radical/action data to display to the player
        // TODO for example query kanji based on different criteria as wrong answers

        // starting with 1, for every 10 strokes in a kanji we select one of its components.
        // the components boost is applied to attack and displayed to the player
        List<List<Radical>> radicals = (List<List<Radical>>) proficientKanji.stream().map(k -> {
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
                resultStrings.add(r.getBoost().getBoostStrings()[0]);
            });

            return String.join(";", resultStrings);
        }).toArray(String[]::new);

        // display kanji's power before the effects
        for (int i = 0; i < radicalEffects.length; i++) {
            radicalEffects[i] = Integer.toString(proficientKanji.get(i).getPower()) + "\n" + radicalEffects[i];
        }

        // have player choose a kanji as attack, set task and then perform attack with updated dmg
        //int rightAnswer = (int) (Math.random() * 4.0); // select random kanji to be the right one
        // choose1 just returns the index
        //String chosenAttackKanji = battleWindow.choose1OutOf4((String[]) proficientKanji.stream().map(Kanji::getCharacter).toArray(String[]::new), "Choose a kanji action:", "Attack selection");
        int chosenIndex = battleWindow.choose1OutOf4(radicalEffects, "Choose a kanji action:", "Attack selection");
        String chosenAttackEffect = radicalEffects[chosenIndex];
        Kanji chosenAttackKanji = proficientKanji.get(chosenIndex);
        //System.out.println("Chose: " + chosenAttackKanji.getCharacter());
        for (int i = 0; i < chosenAttackKanji.getTranslations().size(); i++) System.out.println(chosenAttackKanji.getTranslations().get(i));
        List<Radical> chosenRadicals = radicals.get(chosenIndex);

        //battleWindow.waitContinueCommand();
        String[] meanings = proficientKanji.stream().map(k -> String.join(",", k.getTranslations())).toArray(String[]::new);
        String expectedMeaning = meanings[chosenIndex];
        //System.out.println("Expected answer: " + expectedMeaning);
        Collections.shuffle(Arrays.asList(meanings));

        Timestamp start_time = Timestamp.from(Instant.now());
        chosenIndex = battleWindow.choose1OutOf4(meanings, "What is the meaning of " + chosenAttackKanji.getCharacter(), "Kanji Attack");
        Timestamp finish_time = Timestamp.from(Instant.now());
        //System.out.println("chosenIndex: " + chosenIndex);
        //for (int i = 0; i < 4; i++) System.out.println(meanings[i]);
        String answer = meanings[chosenIndex];
        //System.out.println("Choices afterwards: ");
        /*for (int i = 0; i < 4; i++) {
            System.out.println(meanings[i]);
        }*/
        //System.out.println("Answer given: " + answer);

        boolean attackSuccessful = expectedMeaning.equals(answer);
        battleWindow.writeToOutput(attackSuccessful ? "That's right!" : "Wrong, it's " + expectedMeaning);
        //System.out.println(attackSuccessful ? "That's right!" : "Wrong, it's " + expectedMeaning);
        int damage = chosenAttackKanji.getPower() * 10;
        // apply radicals effects on attack
        for (int i = 0; i < chosenRadicals.size(); i++) {
            damage += chosenRadicals.get(i).getBoost().getAttack();
        }
        if (attackSuccessful) damage *= 2;
        team2[0].takeDamage(damage);
        // log task result in DB
        parent.getDB().appendStudyLog(chosenAttackKanji.getId(), "ABCD", "Meaning", start_time, finish_time, attackSuccessful);

        //battleWindow.waitContinueCommand();

        //System.out.println(chosenAttackKanji);
        // TODO code is duplicated from above, refactor
        // honestly just do querying kanji etc in function with parameter bool attack|defense
        radicals = (List<List<Radical>>) inproficientKanji.stream().map(k -> {
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

        radicalEffects = (String[]) radicals.stream().map(rads -> {
            List<String> resultStrings = new ArrayList<String>();
            rads.stream().forEach(r -> {
                // effect is the defense value of the radicals boost for now
                resultStrings.add(r.getBoost().getBoostStrings()[1]);
            });

            return String.join(";", resultStrings);
        }).toArray(String[]::new);

        // display kanji's power before the effects
        for (int i = 0; i < radicalEffects.length; i++) {
            radicalEffects[i] = Integer.toString(inproficientKanji.get(i).getPower()) + "\n" + radicalEffects[i];
        }

        chosenIndex = battleWindow.choose1OutOf4(radicalEffects, "Choose a kanji action:", "Defense selection");

        String chosenDefenseEffect = radicalEffects[chosenIndex];
        Kanji chosenDefenseKanji = inproficientKanji.get(chosenIndex);
        chosenRadicals = radicals.get(chosenIndex);
        //battleWindow.waitContinueCommand();
        meanings = inproficientKanji.stream().map(k -> String.join(",", k.getTranslations())).toArray(String[]::new);
        expectedMeaning = meanings[chosenIndex];
        Collections.shuffle(Arrays.asList(meanings));
        start_time = Timestamp.from(Instant.now());
        chosenIndex = battleWindow.choose1OutOf4(meanings, "What is the meaning of " + chosenDefenseKanji.getCharacter(), "Kanji Defense");
        answer = meanings[chosenIndex];

        finish_time = Timestamp.from(Instant.now());
        boolean defenseSuccessful = expectedMeaning.equals(answer);
        damage = chosenDefenseKanji.getPower() * 10;
        if (defenseSuccessful) damage /= 2;
        // apply radicals effects on defense
        for (int i = 0; i < chosenRadicals.size(); i++) {
            damage -= chosenRadicals.get(i).getBoost().getAttack();
            damage = Math.max(0, damage);
        }
        team1[0].takeDamage(damage);
        // log task results in db
        parent.getDB().appendStudyLog(chosenDefenseKanji.getId(), "ABCD", "Meaning", start_time, finish_time, defenseSuccessful);

        publish(new FighterUpdateEvent(team1[0], team2[0]));

        // to test at the end of every round update kanjidex and then print current stats
        parent.updateKanjiDex();
        parent.getKanjiDex().printRankedKanjiList();

        //battleWindow.waitContinueCommand();
    }

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

    private boolean isBattleOver() {
        for (RadicalFighter fighter : team1) {
            if (fighter.getHP() > 0) {
                boolean allAlliesDefeated = true;
                for (RadicalFighter opponent : team2) {
                    if (opponent.getHP() > 0) {
                        allAlliesDefeated = false;
                        break;
                    }
                }
                if (allAlliesDefeated) {
                    System.out.println("Team 1 wins!");
                    return true;
                } else {
                    break;
                }
            }
        }
        for (RadicalFighter fighter : team2) {
            if (fighter.getHP() > 0) {
                boolean allOpponentsDefeated = true;
                for (RadicalFighter opponent : team1) {
                    if (opponent.getHP() > 0) {
                        allOpponentsDefeated = false;
                        break;
                    }
                }
                if (allOpponentsDefeated) {
                    System.out.println("Team 2 wins!");
                    return true;
                } else {
                    break;
                }
            }
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
