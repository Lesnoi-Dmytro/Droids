package game.effects;

import game.droid.Droid;

import java.util.function.BiConsumer;

public class Effect {
    private final String name;
    private final boolean debuff;
    private final int amount;
    private int duration;
    private boolean thisTurn;
    private final BiConsumer<Droid, Integer> onUse;

    Effect(String name, boolean debuff, int duration, int amount, BiConsumer<Droid, Integer> onUse) {
        this.name = name;
        this.debuff = debuff;
        this.duration = duration;
        this.amount = amount;
        this.onUse = onUse;
        thisTurn = false;
    }

    public boolean isDebuff() {
        return debuff;
    }

    public String getName() {
        return name;
    }

    public void firstApply(Droid droid) {
        onUse.accept(droid, amount);
        droid.toLog(droid + " is " + this);
    }

    public void apply(Droid droid) {
        if (!thisTurn) {
            onUse.accept(droid, amount);
            droid.toLog(droid + " is " + this);
        }
    }

    public boolean remove(Droid droid) {
        if (!name.equals("MELTED") && !name.equals("REGEN")) {
            onUse.accept(droid, -amount);
            thisTurn = false;
        }
        if (!name.equals("STUNNED")) {
            duration--;
        }
        return duration == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Effect e) {
            return name.equals(e.name);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return name + " for " + duration + " turns";
    }
}