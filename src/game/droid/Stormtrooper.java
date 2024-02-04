package game.droid;

import game.DroidGame;
import game.effects.EffectList;
import game.util.Out;
import game.util.Stats;

import java.util.List;

public class Stormtrooper extends Droid {

    public Stormtrooper(Out team, int y) {
        super(team, 80, 4, 5, 35,
                "Knee shot", "Aiming shot",
                5, 100, "/◌\\", y);
    }

    @Override
    public boolean action1(List<Droid> units, DroidGame.MapMarker[][] map) {
        boolean hit = random.nextInt(101) <= getStat(Stats.ACCURACY);
        action1(units, map, hit);
        return hit;
    }

    @Override
    public boolean action2(List<Droid> units, DroidGame.MapMarker[][] map) {
        boolean hit = random.nextInt(101) <= getStat(Stats.ACCURACY) + 10;
        action2(units, map, hit);
        return hit;
    }

    @Override
    public void action1(List<Droid> units, DroidGame.MapMarker[][] map, boolean hit) {
        if (!hit) {
            log.add(this + " has missed");
            return;
        }
        units.get(0).addEffect(EffectList.SLOWED.getEffect());
        units.get(0).addEffect(EffectList.MELTED.getEffect());
        units.get(0).takeHit(getStat(Stats.DAMAGE));
    }

    @Override
    public void action2(List<Droid> units, DroidGame.MapMarker[][] map, boolean hit) {
        if (!hit) {
            log.add(this + " has missed");
            return;
        }
        addEffect(EffectList.AIMED.getEffect());
        units.get(0).takeHit(getStat(Stats.DAMAGE));
    }

    @Override
    public boolean canAffect1(Droid unit, DroidGame.MapMarker[][] map) {
        return team != unit.team && distance(unit) <= getStat(Stats.ATTACK_DISTANCE);
    }

    @Override
    public boolean canAffect2(Droid unit, DroidGame.MapMarker[][] map) {
        return team != unit.team && distance(unit) <= getStat(Stats.ATTACK_DISTANCE) + 1;
    }
}