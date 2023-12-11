package model.radicals;

public class RadicalFighter {
    private final String name;
    private int hp;
    private Radical radical;

    private int attackPoints;

    private int defensePoints;

    private RadicalBoost boost;

    public RadicalFighter(String name, Radical radical, int hp, int attack, int defense) {
        this.name = name;
        this.radical = radical;
        this.hp = hp;
        this.attackPoints = attack;
        this.defensePoints = defense;
    }

    public String getName() {
        return this.name;
    }

    public int getHP() {
        return hp;
    }

    public int getAttackPoints() {return attackPoints;}

    public int getDefensePoints() { return defensePoints; }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void print() {
        System.out.println(this.name + " (HP: " + this.hp + ", attack: " + this.attackPoints + ", def: " + this.defensePoints);
    }

    public void attack(RadicalFighter otherFighter) {
        int damage = this.attackPoints - otherFighter.defensePoints;
        if (damage > 0) {
            otherFighter.takeDamage(damage);
        }
        System.out.println(this.name + " attacked " + otherFighter.getName() + " for " + damage + " damage.");
        System.out.println(otherFighter.name + " has " + otherFighter.getHP() + " HP remaining.");
    }

    public void takeDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);
    }

    public void heal(int heal) {
        this.hp += heal;

        System.out.println(this.name + " healed for " + heal + " hp.");
    }

    public int defend() {
        return (int) Math.floor(radical.getBoost().getDefense() * Math.random() * 10);
    }
}