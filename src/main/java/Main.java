import controller.Controller;
import controller.KanjiBattle;
import model.radicals.RadicalFighter;
import controller.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import data.KanjiDatabase;

public class Main {
    public static void main(String[] args) throws SQLException {

        KanjiDatabase db = new KanjiDatabase();
        db.initialize(); //TODO static init?
        Controller controller = new Controller();
        controller.start();

        // Mock fighters
        /*RadicalFighter[] team1 = {new RadicalFighter("Kanji1", 50, 50), new RadicalFighter("Kanji2", 40, 60)};
        RadicalFighter[] team2 = {new RadicalFighter("Kanji3", 60, 40), new RadicalFighter("Kanji4", 30, 70)};
        // Create battle object
        KanjiBattle battle = new KanjiBattle(team1, team2);
        // Print initial battle info
        System.out.println("Initial Battle Info:");
        battle.printBattleState();
        // Perform attack
        //int damage = battle.showAttackResult(team1[0], team2[0]);
        // Print battle info after attack
        System.out.println("\nBattle Info After Attack:");
        battle.printBattleState();*/


        /*model.radicals.Radical radical = new model.radicals.Radical(1, "ichi", "一", "one", new model.RadicalBoost(1, 1));
        model.radicals.RadicalFighter fighter = new model.radicals.RadicalFighter("iiko", radical, 10, 5, 5);
        model.radicals.RadicalFighter otherFighter = new model.radicals.RadicalFighter("waruiko", radical, 10, 6, 4);

        fighter.print();
        fighter.attack(otherFighter);
        fighter.print();
        fighter.takeDamage(5);
        fighter.print();
        fighter.heal(3);
        fighter.print();

         */
    }
}
