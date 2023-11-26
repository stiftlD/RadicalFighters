package controller;


import data.KanjiDatabase;
import data.StudyService;
import model.kanji.KanjiScheduler;

// this class is meant to provide access to database services etc. to different controller interfaces globally
// consider refactoring it again if it creates problems for testing or development
public class ServiceLocator {

    private static KanjiDatabase db = new KanjiDatabase();
    private static StudyService studyService = new StudyService();
    private static KanjiScheduler kanjiScheduler = new KanjiScheduler(studyService);

    public static KanjiDatabase getDB() { return db; }

    public static StudyService getStudyService() { return studyService; }

    public static KanjiScheduler getKanjiScheduler() { return kanjiScheduler; }

}
