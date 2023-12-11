package controller.battleaction;

import model.kanji.Kanji;
import model.radicals.Radical;
import model.radicals.RadicalBoost;

import java.util.ArrayList;
import java.util.List;

public abstract class BattleAction {
    protected List<Kanji> kanjis;
    protected List<RadicalBoost> radicalBoosts;

    public BattleAction(List<Kanji> kanjis) {
        this.kanjis = kanjis;
        this.radicalBoosts = new ArrayList<RadicalBoost>();
    }

    public void addBoost(RadicalBoost boost) {
        this.radicalBoosts.add(boost);
    }

    public abstract void applyBoosts();

    public List<Kanji> getKanjis() {return kanjis;}

    public List<RadicalBoost> getRadicalBoosts() {return radicalBoosts;}
}
