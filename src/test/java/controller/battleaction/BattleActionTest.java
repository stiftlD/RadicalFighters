package controller.battleaction;

import controller.battleaction.Attack;
import controller.battleaction.BattleAction;
import model.kanji.Kanji;
import model.radicals.RadicalBoost;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BattleActionTest {

    @Test
    public void testAddBoost() {
        // Arrange
        List<Kanji> kanjis = new ArrayList<>();
        BattleAction battleAction = new Attack(kanjis); // Attack as concrete implementation

        // Act
        RadicalBoost boost = new RadicalBoost(5, 5);
        battleAction.addBoost(boost);

        // Assert
        List<RadicalBoost> boosts = battleAction.getRadicalBoosts();
        assertEquals(1, boosts.size());
        assertEquals(boost, boosts.get(0));
    }

}