package model.radicals;

public class RadicalBoost {
    private int attack;
    private int defense;

    public RadicalBoost(int attack, int defense) {
        this.attack = attack;
        this.defense = defense;
    }

    public String[] getBoostStrings() {
        return new String[]{String.format("+%d atk", this.attack), String.format("+%d def", this.defense)};
    }

    public int getAttack() {
        return this.attack;
    }

    public int getDefense() {
        return this.defense;
    }
}