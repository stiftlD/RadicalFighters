package model.kanji;

import data.StudyService.Tuple;

import java.util.List;
import java.util.stream.Collectors;

// TODO write some layer in controller model to access and prepare all data the view needs
// TODO this could be an interface as well
// TODO most importantly update proficiency in database whenever there is a change with kanji
public class DexData {
    private List<Kanji> kanjiEntries;
    // Add other fields and references as needed.

    public DexData() {
        this.kanjiEntries = kanjiEntries;
        // Initialize other fields and references as needed.
    }

    public void setKanjiEntries(List<Kanji> kanjiEntries) {
        this.kanjiEntries = kanjiEntries;
    }

    public List<Kanji> getKanjiEntries() {
        return kanjiEntries;
    }

    public List<Tuple<String, Double>> getKanjiProfRanking() {
        return kanjiEntries.stream().map(k -> new Tuple<String, Double>(k.getCharacter(), (double) k.getProficiency())).collect(Collectors.toList());
    }

    public String getCharacterAtIndex(int index) throws IndexOutOfBoundsException {
        // TODO this should really be prepared in controller and the whole update approach rethought
        return kanjiEntries.get(index).getCharacter();
    }

    // Add getters and setters for other fields and references.
}

