package model.kanji;

import model.kanji.Kanji;
import data.KanjiDatabase;

import java.sql.SQLException;
import java.util.List;

public class KanjiScheduler {
    private final KanjiDatabase db;

    public KanjiScheduler(KanjiDatabase dbWrapper) {
        this.db = dbWrapper;
    }

    public Kanji getProficientKanji() {
        // TODO track player max grade
        // Implement scheduling and querying logic for proficient Kanji here using dbWrapper
        try {
            return db.getRandomKanjiInProfInterval(5, 0.0, 0.5).get(0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Kanji getInproficientKanji() {
        // Implement scheduling and querying logic for less proficient Kanji here using db wrapper
        try {
            return db.getRandomKanjiInProfInterval(5, 0.5, 1.0).get(0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Add other scheduling methods as needed
}
