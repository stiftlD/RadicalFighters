package controller.battleaction;

import model.kanji.Kanji;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

// TODO right now this is the same as attack
public class Defense extends BattleAction {
    private int baseDamage; // base dmg of kanji

    public Defense(List<Kanji> kanjis) {
        super(kanjis);
        try {
            this.baseDamage = kanjis.stream().map(k -> k.getPower())
                    .reduce(Integer::sum).get() * 10;
        } catch (NoSuchElementException e) {
            // case no kanji for this defense
            this.baseDamage = 0;
        }

    }

    @Override
    public void applyBoosts() {
        // apply radicals effects on defense
        for (int i = 0; i < radicalBoosts.size(); i++) {
            baseDamage += radicalBoosts.get(i).getDefense();
        }
        baseDamage = Math.max(0, baseDamage);
    }

    public int getBaseDamage() {
        return baseDamage;
    }

}
