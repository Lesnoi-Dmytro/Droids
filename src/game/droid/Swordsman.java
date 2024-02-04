package game.droid;

import game.DroidGame;
import game.effects.Effect;
import game.effects.EffectList;
import game.util.Out;
import game.util.Stats;

import java.util.List;

public class Swordsman extends Droid {
    public Swordsman(Out team, int y) {
        super(team, 70, 4, 5, 55,
                "Dash", "Charged swing",
                2, 120, "|◌|", y);
    }

    @Override
    public boolean action1(List<Droid> units, DroidGame.MapMarker[][] map) {
        boolean hit = random.nextInt(101) <= getStat(Stats.ACCURACY) + 10;
        action1(units, map, hit);
        return true;
    }

    @Override
    public boolean action2(List<Droid> units, DroidGame.MapMarker[][] map) {
        boolean hit = random.nextInt(101) <= getStat(Stats.ACCURACY);
        action2(units, map, hit);
        return hit;
    }

    @Override
    public void action1(List<Droid> units, DroidGame.MapMarker[][] map, boolean hit) {
        int[] coordinates = stepNear(units.get(0).x, units.get(0).y, getStat(Stats.ATTACK_DISTANCE) + 1, map);
        moveTo(coordinates[0], coordinates[1], map);
        if (!hit) {
            return;
        }
        addEffect(EffectList.ACCELERATED.getEffect());
        units.get(0).takeHit(getStat(Stats.DAMAGE));
    }

    @Override
    public void action2(List<Droid> units, DroidGame.MapMarker[][] map, boolean hit) {
        int[] coordinates = stepNear(units.get(0).x, units.get(0).y, getStat(Stats.ATTACK_DISTANCE), map);
        moveTo(coordinates[0], coordinates[1], map);
        if (!hit) {
            log.add(this + " has missed");
            return;
        }
        addEffect(EffectList.STRENGTHENED.getEffect());
        addEffect(EffectList.AIMED.getEffect());
        units.get(0).pureDamage(getStat(Stats.DAMAGE), action2Name);
    }

    @Override
    public boolean canAffect1(Droid unit, DroidGame.MapMarker[][] map) {
        return unit.team != team && stepNear(unit.x, unit.y, getStat(Stats.ATTACK_DISTANCE) + 1, map) != null;
    }

    @Override
    public boolean canAffect2(Droid unit, DroidGame.MapMarker[][] map) {
        return unit.team != team && stepNear(unit.x, unit.y, getStat(Stats.ATTACK_DISTANCE), map) != null;
    }
}
