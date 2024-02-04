package game.effects;

import game.util.Stats;

public enum EffectList {
    CONFUSED,
    STUNNED,
    BLIND,
    WEAKENED,
    CRACKED,
    SLOWED,
    MELTED,
    AIMED,
    STRENGTHENED,
    ARMED,
    ACCELERATED,
    REGEN;

    public Effect getEffect() {
        Effect new_effect = null;
        String name = this.name();
        switch (this) {
            case CONFUSED -> new_effect = new Effect(name, true, 3, 0, (d, a) -> {
            });
            case STUNNED -> new_effect = new Effect(name, true, 1, 0, (d, a) -> {
            });
            case BLIND -> new_effect = new Effect(name, true, 2, -40, (d, a) -> {
                d.changeStat(Stats.ACCURACY, a);
                d.changeStat(Stats.ATTACK_DISTANCE, -1);
            });
            case WEAKENED -> new_effect = new Effect(name, true, 3, -10, (d, a) -> {
                d.changeStat(Stats.DAMAGE, a);
            });
            case CRACKED -> new_effect = new Effect(name, true, 3, -5, (d, a) -> {
                d.changeStat(Stats.ARMOR, a);
            });
            case SLOWED -> new_effect = new Effect(name, true, 3, -2, (d, a) -> {
                d.changeStat(Stats.SPEED, a);
            });
            case MELTED -> new_effect = new Effect(name, true, 3, 4, (d, a) -> {
                d.pureDamage(a, "MELT");
            });
            case AIMED -> new_effect = new Effect(name, false, 3, 10, (d, a) -> {
                d.changeStat(Stats.ACCURACY, a);
                d.changeStat(Stats.ATTACK_DISTANCE, 1);
            });
            case STRENGTHENED -> new_effect = new Effect(name, false, 3, 10, (d, a) -> {
                d.changeStat(Stats.DAMAGE, a);
            });
            case ARMED -> new_effect = new Effect(name, false, 3, 5, (d, a) -> {
                d.changeStat(Stats.ARMOR, a);
            });
            case ACCELERATED -> new_effect = new Effect(name, false, 3, 2, (d, a) -> {
                d.changeStat(Stats.SPEED, a);
            });
            case REGEN -> new_effect = new Effect(name, false, 5, 4, (d, a) -> {
                d.heal(a, "REGEN");
            });
        }
        return new_effect;
    }
}