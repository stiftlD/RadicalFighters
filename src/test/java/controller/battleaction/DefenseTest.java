package controller.battleaction;

import model.kanji.Kanji;
import model.radicals.RadicalBoost;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DefenseTest {
    @Test
    public void testApplyBoosts() {
        // Arrange
        List<Kanji> kanjis = new ArrayList<>();
        Defense defense = new Defense(kanjis);

        // Act
        RadicalBoost boost = new RadicalBoost(1, 2);
        defense.addBoost(boost);
        defense.applyBoosts();

        int expectedDamage = 2; // defense with no kanji power and 2 radical defense applied

        // Add assertions
        // TODO applyBoosts() modifies some internal state, check that state here.
        assertEquals(expectedDamage, defense.getBaseDamage());
    }
}
