package controller.battleaction;

import model.kanji.Kanji;
import model.radicals.RadicalBoost;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AttackTest {

    @Test
    public void testApplyBoosts() {
        // Arrange
        List<Kanji> kanjis = new ArrayList<>();
        Attack attack = new Attack(kanjis);

        // Act
        RadicalBoost boost = new RadicalBoost(1, 2);
        attack.addBoost(boost);
        attack.applyBoosts();

        int expectedBoostCount = 1;
        int expectedDamage = 1; // attack with no kanji power and 1 radical attack applied

        // Add assertions
        // TODO applyBoosts() modifies some internal state, check that state here.
        assertEquals(expectedBoostCount, attack.getRadicalBoosts().size());
        assertEquals(expectedDamage, attack.getBaseDamage());
    }
}
