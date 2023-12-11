package controller.battleaction;

import model.kanji.Kanji;
import model.radicals.RadicalBoost;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DefenseTest {

    // dummy objects
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
    @Test
    public void testApplyBoosts() {
        // Arrange
        List<Kanji> kanjis = new ArrayList<Kanji>(Arrays.asList(testKanjiA));
        Defense defense = new Defense(kanjis);

        // Act
        RadicalBoost boost = new RadicalBoost(1, 2);
        defense.addBoost(boost);
        defense.applyBoosts();

        int expectedDamage = 12; // defense with 10 kanji power and 2 radical defense applied

        // Add assertions
        // TODO applyBoosts() modifies some internal state, check that state here.
        assertEquals(expectedDamage, defense.getBaseDamage());
    }
}
