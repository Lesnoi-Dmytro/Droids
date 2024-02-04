package game.droid;

import game.DroidGame;
import game.effects.EffectList;
import game.util.Out;
import game.util.Stats;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Defender extends Droid {
    public Defender(Out team, int y) {
        super(team, 65, 2, 15, 25,
                "Snatch energy", "Barricade",
                3, 75, "(●)", y);
        stats.put(Stats.SHIELD, 50);
    }

    @Override
    public boolean action1(List<Droid> units, DroidGame.MapMarker[][] map) {
        boolean hit = random.nextInt(101) <= getStat(Stats.ACCURACY);
        action1(units, map, hit);
        return hit;
    }

    @Override
    public boolean action2(List<Droid> units, DroidGame.MapMarker[][] map) {
        action2(units, map, true);
        return true;
    }

    @Override
    public void action1(List<Droid> units, DroidGame.MapMarker[][] map, boolean hit) {
        if (!hit) {
            log.add(this + " has missed");
            return;
        }
        units.get(0).takeHit(getStat(Stats.DAMAGE));
        this.changeStat(Stats.SHIELD, 15);
        log.add(this + " now have " + getStat(Stats.SHIELD) + " shield points");
    }

    @Override
    public void action2(List<Droid> units, DroidGame.MapMarker[][] map, boolean hit) {
        if (getStat(Stats.SHIELD) > 0) {
            addEffect(EffectList.ACCELERATED.getEffect());
            addEffect(EffectList.ARMED.getEffect());
        }
        changeStat(Stats.SHIELD, 50);
        log.add(this + " now have " + getStat(Stats.SHIELD) + " shield points");
    }

    @Override
    public boolean canAffect1(Droid unit, DroidGame.MapMarker[][] map) {
        return team != unit.team && distance(unit) <= getStat(Stats.ATTACK_DISTANCE);
    }

    @Override
    public boolean canAffect2(Droid unit, DroidGame.MapMarker[][] map) {
        return unit == this;
    }

    @Override
    public void takeHit(int damage) {
        damage = damage - getStat(Stats.ARMOR);
        if (damage < 0) {
            return;
        }
        if (getStat(Stats.SHIELD) > damage) {
            changeStat(Stats.SHIELD, -damage);
            log.add(this + " now have " + getStat(Stats.SHIELD) + " shield points");
        } else {
            if (getStat(Stats.SHIELD) > 0) {
                damage -= getStat(Stats.SHIELD);
                changeStat(Stats.SHIELD, -getStat(Stats.SHIELD));
                log.add(this + " shield's broken");
            }
            addEffect(EffectList.CRACKED.getEffect());
            super.takeHit((int) (damage * 1.5));
        }
    }

    @Override
    protected void makeAction2(List<Droid> affect, DroidGame.MapMarker[][] map, FileWriter out) {
        try {
            out.write('2');
            out.write('A');
            out.write(action2(affect, map) ? 'T' : 'F');
        } catch (IOException ignore) {
        }
    }

    @Override
    protected void makeAction2AI(List<Droid> affects, DroidGame.MapMarker[][] map, FileWriter out) {
        try {
            out.write('2');
            out.write('A');
            out.write(action2(affects, map) ? 'T' : 'F');
        } catch (IOException ignore) {
        }
    }
}