package game.droid;

import game.DroidGame;
import game.effects.Effect;
import game.effects.EffectList;
import game.util.Out;
import game.util.Stats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Support extends Droid {
    public Support(Out team, int y) {
        super(team, 80, 3, 7, 20,
                "Healing pistol", "Buffing wave",
                5, 120, "-◌-", y);
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
        units.get(0).heal(getStat(Stats.DAMAGE), toString());
        units.get(0).addEffect(EffectList.REGEN.getEffect());
        units.get(0).addEffect(EffectList.ARMED.getEffect());
    }

    @Override
    public void action2(List<Droid> units, DroidGame.MapMarker[][] map, boolean hit) {
        for (Droid unit : units) {
            unit.addEffect(EffectList.REGEN.getEffect());
            unit.addEffect(EffectList.ACCELERATED.getEffect());
            unit.addEffect(EffectList.STRENGTHENED.getEffect());
            unit.effects.removeIf(s -> {
                if (s.isDebuff()) {
                    s.remove(unit);
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public boolean canAffect1(Droid unit, DroidGame.MapMarker[][] map) {
        return team == unit.team && distance(unit) <= getStat(Stats.ATTACK_DISTANCE);
    }

    @Override
    public boolean canAffect2(Droid unit, DroidGame.MapMarker[][] map) {
        return team == unit.team && distance(unit) <= getStat(Stats.ATTACK_DISTANCE) - 2;
    }

    @Override
    protected void makeAction1(List<Droid> affect, DroidGame.MapMarker[][] map, FileWriter out) {
        Scanner in = new Scanner(System.in);
        int n = -1;
        for (int i = 0; i < affect.size(); i++) {
            System.out.println((i + 1) + " - " + affect.get(i));
        }
        while (n < 0 || n > affect.size()) {
            System.out.print("Chosen unit = ");
            n = in.nextInt();
        }
        List<Droid> units = affect.subList(n - 1, n);
        try {
            out.write('1');
            out.write(n);
            out.write(action1(units, map) ? 'T' : 'F');
        } catch (IOException ignore) {
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
    protected void makeAction2AI(List<Droid> affect, DroidGame.MapMarker[][] map, FileWriter out) {
        try {
            out.write('2');
            out.write('A');
            out.write(action2(affect, map) ? 'T' : 'F');
        } catch (IOException ignore) {
        }
    }

    @Override
    protected void moveClosestAI(ArrayList<Droid> team1, ArrayList<Droid> team2, DroidGame.MapMarker[][] map, FileWriter out) {
        super.moveClosestAI(team2, team1, map, out);
    }
}