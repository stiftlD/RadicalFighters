package controller.task;

import controller.Controller;
import model.kanji.Kanji;
import model.radicals.Radical;
import view.BattleWindow;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MultipleChoiceTask extends KanjiTask {
    int choiceCount;

    // TODO define MC tasks with multiple Kanji (if it makes sense)

    public MultipleChoiceTask(Controller controller, List<Kanji> kanjis, KanjiSubject subject, BattleWindow window, int count) {
        super(controller, kanjis, subject, window);
        this.choiceCount = count;
    }

    @Override
    public boolean performTask() {
        // TODO pull more of this into TaskUI

        Kanji exerciseKanji = kanjis.get(0);
        //for (int i = 0; i < exerciseKanji.getTranslations().size(); i++) System.out.println(exerciseKanji.getTranslations().get(i));
        //List<Radical> chosenRadicals = radicals.get(chosenIndex);

        // get some random kanji as wrong options TODO implement some selection logic
        List<Kanji> kanjiOptions = new ArrayList<Kanji>();
        for (int i = 0; i < choiceCount -1; i++) { // count -1 bc 1 will be our correct option
            kanjiOptions.add(controller.getStudyService().getRandomKanjiInProfInterval(exerciseKanji.getGrade(), 0f, 1f).get(0));
        }
        kanjiOptions.add(exerciseKanji);

        //battleWindow.waitContinueCommand();
        String[] meanings = kanjiOptions.stream().map(k -> String.join(",", k.getTranslations())).toArray(String[]::new);
        String expectedMeaning = String.join(",", exerciseKanji.getTranslations());
        
        //System.out.println("Expected answer: " + expectedMeaning);
        Collections.shuffle(Arrays.asList(meanings));

        Timestamp start_time = Timestamp.from(Instant.now());
        int chosenIndex = ui.getParent().choose1OutOf4(meanings, "What is the meaning of " + exerciseKanji.getCharacter());
        Timestamp finish_time = Timestamp.from(Instant.now());
        //System.out.println("chosenIndex: " + chosenIndex);
        //for (int i = 0; i < 4; i++) System.out.println(meanings[i]);
        //System.out.println(chosenIndex);
        String answer = meanings[chosenIndex];
        //System.out.println("Choices afterwards: ");
        /*for (int i = 0; i < 4; i++) {
            System.out.println(meanings[i]);
        }*/
        //System.out.println("Answer given: " + answer);

        boolean attackSuccessful = expectedMeaning.equals(answer);
        ui.getParent().writeToOutput(attackSuccessful ? "That's right!" : "Wrong, it's " + expectedMeaning);

        logResults(start_time, finish_time, attackSuccessful);

        return attackSuccessful;
    }

    @Override
    public void logResults(Timestamp start_time, Timestamp finish_time, boolean success) {
        kanjis.forEach(k -> {
            controller.getStudyService().appendStudyLog(k.getId(), "ABCD", "Meaning", start_time, finish_time, success);
        });
    }
}
