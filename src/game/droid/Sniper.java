package game.droid;

import game.DroidGame;
import game.effects.EffectList;
import game.util.Out;
import game.util.Stats;

import java.util.List;
import java.util.Random;

public class Sniper extends Droid {

    public Sniper(Out team, int y) {
        super(team, 100, 2, -5, 25,
                "Eye shot", "Flashbang",
                15, 70, "//̅", y);
    }

    @Override
    public boolean action1(List<Droid> units, DroidGame.MapMarker[][] map) {
        boolean hit = random.nextInt(101) <= getStat(Stats.ACCURACY);
        action1(units, map, hit);
        return hit;
    }

    @Override
    public boolean action2(List<Droid> units, DroidGame.MapMarker[][] map) {
        boolean hit = random.nextInt(101) <= getStat(Stats.ACCURACY) - 15;
        action2(units, map, hit);
        return hit;
    }

    @Override
    public void action1(List<Droid> units, DroidGame.MapMarker[][] map, boolean hit) {
        if (!hit) {
            log.add(this + " has missed");
            return;
        }
        int d_x = x - units.get(0).x;
        int d_y = y - units.get(0).y;
        int bonusDamage = (int) (Math.sqrt(d_x * d_x + d_y * d_y) * 2);
        units.get(0).pureDamage(getStat(Stats.DAMAGE) + bonusDamage, action1Name);
        units.get(0).addEffect(EffectList.BLIND.getEffect());
    }

    @Override
    public void action2(List<Droid> units, DroidGame.MapMarker[][] map, boolean hit) {
        if (!hit) {
            log.add(this + " has missed");
            return;
        }
        Droid unit = units.get(0);
        unit.addEffect(EffectList.CONFUSED.getEffect());
        int n_x = unit.x + Integer.compare(unit.x, x);
        int n_y = unit.y + Integer.compare(unit.y, y);
        if (n_x > -1 && n_x < map.length &&
                (map[n_x][unit.y] == DroidGame.MapMarker.FREE || map[n_x][unit.y] == DroidGame.MapMarker.CAN_MOVE)) {
            if (n_y > -1 && n_y < map.length &&
                    (map[n_x][n_y] == DroidGame.MapMarker.FREE || map[n_x][n_y] == DroidGame.MapMarker.CAN_MOVE)) {
                unit.moveTo(n_x, n_y, map);
            } else {
                unit.moveTo(n_x, unit.y, map);
            }
        } else {
            unit.moveTo(unit.x, n_y, map);
        }
    }

    @Override
    public boolean canAffect1(Droid unit, DroidGame.MapMarker[][] map) {
        return team != unit.team && distance(unit) <= getStat(Stats.ATTACK_DISTANCE);
    }

    @Override
    public boolean canAffect2(Droid unit, DroidGame.MapMarker[][] map) {
        return team != unit.team && distance(unit) <= getStat(Stats.ATTACK_DISTANCE) - 10;
    }
}