package model.radicals;

import java.util.Random;

public class Radical {
    private int id;
    private String name;
    private String unicode;
    private RadicalBoost boost;
    private String description;

    public Radical(int id, String name, String unicode, String description, RadicalBoost boost) {
        this.id = id;
        this.name = name;
        this.unicode = unicode;
        if (boost == null) {
            Random rand = new Random();
            if (rand.nextBoolean()) {
                this.boost = new RadicalBoost(1, 0);
            } else {
                this.boost = new RadicalBoost(0, 1);
            }
        } else {
            this.boost = boost;
        }
        this.description = description;
    }

    public void print() {
        System.out.printf("%s model.radicals.Radical %d: %s, %s, %s\n",
                this.description, this.id, this.unicode, this.name, this.boost.getBoostStrings());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnicode() {
        return unicode;
    }

    public RadicalBoost getBoost() {
        return boost;
    }

    public String getDescription() {
        return description;
    }
}


