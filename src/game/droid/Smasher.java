package game.droid;

import game.DroidGame;
import game.effects.EffectList;
import game.util.Out;
import game.util.Stats;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Smasher extends Droid {
    public Smasher(Out team, int y) {
        super(team, 70, 3, 15, 45,
                "Wide swing", "Stunning smash",
                2, 175, "T●T", y);
    }

    @Override
    public boolean action1(List<Droid> units, DroidGame.MapMarker[][] map) {
        boolean hit = random.nextInt(101) <= getStat(Stats.ACCURACY);
        action1(units, map, hit);
        return hit;
    }

    @Override
    public boolean action2(List<Droid> units, DroidGame.MapMarker[][] map) {
        boolean stuns = random.nextInt(101) <= getStat(Stats.ACCURACY) - 20;
        action2(units, map, stuns);
        return stuns;
    }

    @Override
    public void action1(List<Droid> units, DroidGame.MapMarker[][] map, boolean hit) {
        if (!hit) {
            log.add(this + " has missed");
            return;
        }
        for (Droid unit : units) {
            unit.takeHit(getStat(Stats.DAMAGE));
            unit.addEffect(EffectList.SLOWED.getEffect());
            unit.addEffect(EffectList.CRACKED.getEffect());
        }
    }

    @Override
    public void action2(List<Droid> units, DroidGame.MapMarker[][] map, boolean stuns) {
        Droid unit = units.get(0);
        int[] coordinates = stepNear(unit.x, unit.y, getStat(Stats.ATTACK_DISTANCE), map);
        moveTo(coordinates[0], coordinates[1], map);
        unit.takeHit(getStat(Stats.DAMAGE));
        if (stuns) {
            unit.addEffect(EffectList.STUNNED.getEffect());
        }
        unit.addEffect(EffectList.CONFUSED.getEffect());
    }

    @Override
    public boolean canAffect1(Droid unit, DroidGame.MapMarker[][] map) {
        return Math.sqrt((x - unit.x) * (x - unit.x) + (y - unit.y) * (y - unit.y)) < 2 && unit != this;
    }

    @Override
    public boolean canAffect2(Droid unit, DroidGame.MapMarker[][] map) {
        return unit.team != team && stepNear(unit.x, unit.y, getStat(Stats.ATTACK_DISTANCE), map) != null;
    }

    @Override
    protected void makeAction1(List<Droid> affect, DroidGame.MapMarker[][] map, FileWriter out) {
        try {
            out.write('1');
            out.write('A');
            out.write(action1(affect, map) ? 'T' : 'F');
        } catch (IOException ignore) {
        }
    }

    @Override
    protected void makeAction1AI(List<Droid> affect, DroidGame.MapMarker[][] map, FileWriter out) {
        try {
            out.write('1');
            out.write('A');
            out.write(action1(affect, map) ? 'T' : 'F');
        } catch (IOException ignore) {
        }
    }
}