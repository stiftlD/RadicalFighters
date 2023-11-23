package controller.task;

import controller.Controller;
import data.KanjiDatabase;
import data.StudyService;
import model.kanji.Kanji;
import org.junit.jupiter.api.Test;
import org.sqlite.core.DB;
import view.BattleWindow;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
public class MultipleChoiceTaskTest {

    // TODO more tests! especially regarding gui (leads to refactoring into taskUI)

    // TODO test objects are still just placeholders
    private Kanji testKanjiA = new Kanji(
            0,
            "笑",
            8,
            1,
            Arrays.asList("わら", "え"),
            Arrays.asList("ショウ"),
            Arrays.asList("laugh"),
            "u000", // idk
            1,
            true);
    private Kanji testKanjiB = new Kanji(
            1,
            "泣",
            8,
            1,
            Arrays.asList("な"),
            Arrays.asList("キュウ"),
            Arrays.asList("cry"),
            "u001",
            0,
            false);

    // TODO extract common setup

    @Test
    public void testPerformTaskQueriesKanjiDB() {
        // Arrange
        Controller mockController = mock(Controller.class);
        StudyService mockDB = mock(StudyService.class);

        // record
        when(mockController.getStudyService()).thenReturn(mockDB);
        when(mockDB.getRandomKanjiInProfInterval(anyInt(), anyDouble(), anyDouble()))
                .thenReturn(new ArrayList<Kanji>(Arrays.asList(testKanjiB)));

        List<Kanji> mockKanjis = new ArrayList<Kanji>(Arrays.asList(testKanjiA));
        KanjiSubject mockSubject = KanjiSubject.MEANING;
        BattleWindow mockWindow = mock(BattleWindow.class);
        int choiceCount = 4;

        // Inject mocks into the MultipleChoiceTask
        MultipleChoiceTask multipleChoiceTask = new MultipleChoiceTask(mockController, mockKanjis, mockSubject, mockWindow, choiceCount);

        // run
        boolean result = multipleChoiceTask.performTask();

        verify(mockDB, times(choiceCount - 1)).getRandomKanjiInProfInterval(
                anyInt(),
                anyDouble(),
                anyDouble()
        );
    }

    @Test
    public void testPerformTaskLogsResults() {
        // Arrange
        Controller mockController = mock(Controller.class);
        StudyService mockDB = mock(StudyService.class);

        // record mock behaviour
        when(mockController.getStudyService()).thenReturn(mockDB);
        // TODO these dots smell
        when(mockDB.getRandomKanjiInProfInterval(anyInt(), anyDouble(), anyDouble()))
                .thenReturn(new ArrayList(Arrays.asList(testKanjiB)));

        // TODO test kanji is a) dummy and b) should be invokable by all test methods
        List<Kanji> mockKanjis = new ArrayList<Kanji>(Arrays.asList(testKanjiA));
        KanjiSubject mockSubject = KanjiSubject.MEANING;
        BattleWindow mockWindow = mock(BattleWindow.class);
        int choiceCount = 4;

        // Inject mocks into the MultipleChoiceTask
        MultipleChoiceTask multipleChoiceTask = new MultipleChoiceTask(mockController, mockKanjis, mockSubject, mockWindow, choiceCount);

        // Act
        boolean result = multipleChoiceTask.performTask();

        // Verify that the appendStudyLog method is called with the expected arguments
        verify(mockDB, times(mockKanjis.size())).appendStudyLog(
                anyInt(), // Kanji ID or any other relevant argument
                eq("ABCD"), // Example string argument
                eq("Meaning"), // Example string argument
                any(Timestamp.class), // Timestamp start_time
                any(Timestamp.class), // Timestamp finish_time
                anyBoolean() // boolean success
        );
    }
}