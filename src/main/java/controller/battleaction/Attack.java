package controller.battleaction;

import model.kanji.Kanji;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

// TODO write unit tests!
public class Attack extends BattleAction {
    private int baseDamage; // base dmg of kanji

    public Attack(List<Kanji> kanjis) {
        super(kanjis);
        // base damage is kanjis power * 10 for now
        try {
            this.baseDamage = kanjis.stream().map(k -> k.getPower())
                    .reduce(Integer::sum).get() * 10;
        } catch (NoSuchElementException e) {
            // case no kanji in attack
            this.baseDamage = 0;
        }
        applyBoosts();
    }

    @Override
    public void applyBoosts() {
        // apply radicals effects on attack
        for (int i = 0; i < radicalBoosts.size(); i++) {
            baseDamage += radicalBoosts.get(i).getAttack();
        }
    }

    public int getBaseDamage() {
        return baseDamage;
    }

}
