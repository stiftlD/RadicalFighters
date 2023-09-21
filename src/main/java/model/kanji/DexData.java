package model.kanji;

import data.StudyService;

import java.util.List;

// TODO write some layer in controller model to access and prepare all data the view needs
// TODO this could be an interface as well
public class DexData {
    private List<StudyService.Tuple<String, Double>> kanjiEntries;
    // Add other fields and references as needed.

    public DexData() {
        this.kanjiEntries = kanjiEntries;
        // Initialize other fields and references as needed.
    }

    public void setKanjiEntries(List<StudyService.Tuple<String, Double>> kanjiEntries) {
        this.kanjiEntries = kanjiEntries;
    }

    public List<StudyService.Tuple<String, Double>> getKanjiEntries() {
        return kanjiEntries;
    }

    // Add getters and setters for other fields and references.
}

