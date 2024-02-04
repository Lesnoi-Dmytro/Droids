package game.droid;

import game.DroidGame;
import game.effects.Effect;
import game.util.Out;
import game.util.Stats;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public abstract class Droid {
    protected final Random random = new Random();
    protected final Out team;
    protected final Map<Stats, Integer> stats;
    protected int maxHealth;
    protected int currentHealth;
    protected final ArrayList<Effect> effects;
    protected final String action1Name;
    protected final String action2Name;
    protected boolean tookTurn;
    protected ArrayList<String> log;
    protected String mapMarker;
    protected int x;
    protected int y;

    public Droid(Out team, int accuracy, int speed, int armor, int damage,
                 String action1Name, String action2Name, int attackDistance,
                 int maxHealth, String mapMarker, int y) {
        this.team = team;
        this.maxHealth = maxHealth;
        this.action1Name = action1Name;
        this.action2Name = action2Name;
        this.mapMarker = mapMarker;
        this.y = y;
        x = 0;
        currentHealth = maxHealth;
        tookTurn = false;
        stats = new HashMap<>(Map.of(
                Stats.ACCURACY, accuracy,
                Stats.SPEED, speed,
                Stats.ARMOR, armor,
                Stats.DAMAGE, damage,
                Stats.ATTACK_DISTANCE, attackDistance
        ));
        log = new ArrayList<>();
        effects = new ArrayList<>();
    }

    public abstract boolean action1(List<Droid> units, DroidGame.MapMarker[][] map);

    public abstract boolean action2(List<Droid> units, DroidGame.MapMarker[][] map);

    public abstract void action1(List<Droid> units, DroidGame.MapMarker[][] map, boolean hit);

    public abstract void action2(List<Droid> units, DroidGame.MapMarker[][] map, boolean hit);

    public abstract boolean canAffect1(Droid unit, DroidGame.MapMarker[][] map);

    public abstract boolean canAffect2(Droid unit, DroidGame.MapMarker[][] map);

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
        n--;
        List<Droid> units = affect.subList(n, n + 1);
        try {
            out.write('1');
            out.write(n);
            out.write(action1(units, map) ? 'T' : 'F');
        } catch (IOException ignore) {
        }
    }

    protected void makeAction2(List<Droid> affect, DroidGame.MapMarker[][] map, FileWriter out) {
        Scanner in = new Scanner(System.in);
        int n = -1;
        for (int i = 0; i < affect.size(); i++) {
            System.out.println((i + 1) + " - " + affect.get(i));
        }
        while (n < 0 || n > affect.size()) {
            System.out.print("Chosen unit = ");
            n = in.nextInt();
        }
        n--;
        List<Droid> units = affect.subList(n, n + 1);
        try {
            out.write('2');
            out.write(n);
            out.write(action2(units, map) ? 'T' : 'F');
        } catch (IOException ignore) {
        }
    }

    public void canAffect(ArrayList<Droid> affect1, ArrayList<Droid> affect2,
                          ArrayList<Droid> team1, ArrayList<Droid> team2, DroidGame.MapMarker[][] map) {
        Stream.concat(team1.stream(), team2.stream())
                .forEach(d -> {
                    if (canAffect1(d, map)) {
                        affect1.add(d);
                    }
                });
        if (!isConfused()) {
            Stream.concat(team1.stream(), team2.stream())
                    .forEach(d -> {
                        if (canAffect2(d, map)) {
                            affect2.add(d);
                        }
                    });
        }
    }

    public void takeTurn(ArrayList<Droid> team1, ArrayList<Droid> team2, DroidGame.MapMarker[][] map, FileWriter out) {
        if (isStunned()) {
            log.add(this + " is stunned");
            tookTurn = true;
            DroidGame.printMap();
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignore) {
            }
            return;
        } else if (team == Out.ANSI_RED) {
            DroidGame.printMap();
            AI(team1, team2, map, out);
            DroidGame.removeTurnOnly();
            tookTurn = true;
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignore) {
            }
            return;
        }

        setMoves(map);
        ArrayList<Droid> affect1 = new ArrayList<>();
        ArrayList<Droid> affect2 = new ArrayList<>();
        canAffect(affect1, affect2, team1, team2, map);
        setAffects(affect1, affect2, map);
        DroidGame.printMap();

        if (!affect1.isEmpty()) {
            System.out.println("1 - " + this + " can affect " + affect1 + " with " + action1Name);
        }
        if (!affect2.isEmpty()) {
            System.out.println("2 - " + this + " can affect " + affect2 + " with " + action2Name);
        }
        Scanner in = new Scanner(System.in);
        boolean canMove = canMove(map);
        if (canMove) {
            System.out.println("M - move");
        }
        System.out.println("S - skip turn");

        while (!tookTurn) {
            System.out.print("Your action: ");
            String action = in.nextLine();
            switch (action) {
                case "1" -> {
                    if (affect1.isEmpty()) {
                        break;
                    }
                    makeAction1(affect1, map, out);
                    tookTurn = true;
                }
                case "2" -> {
                    if (affect2.isEmpty()) {
                        break;
                    }
                    makeAction2(affect2, map, out);
                    tookTurn = true;
                }
                case "M" -> {
                    if (!canMove) {
                        return;
                    }
                    makeMove(map, out);
                }
                case "S" -> {
                    try {
                        out.write('S');
                    } catch (IOException ignore) {
                    }
                    tookTurn = true;
                }
            }
        }
        DroidGame.removeTurnOnly();
    }

    public void takeTurn(ArrayList<Droid> team1, ArrayList<Droid> team2, DroidGame.MapMarker[][] map, FileReader in) {
        if (isStunned()) {
            log.add(this + " is stunned");
            tookTurn = true;
            DroidGame.printMap();
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignore) {
            }
            return;
        }
        setMoves(map);
        ArrayList<Droid> affect1 = new ArrayList<>();
        ArrayList<Droid> affect2 = new ArrayList<>();
        canAffect(affect1, affect2, team1, team2, map);
        setAffects(affect1, affect2, map);
        DroidGame.printMap();
        try {
            switch (in.read()) {
                case '1' -> {
                    int n = in.read();
                    if (n == 'A') {
                        action1(affect1, map, in.read() == 'T');
                    } else {
                        action1(affect1.subList(n, n + 1), map, in.read() == 'T');
                    }
                    for (Droid unit : affect1) {
                        if (!unit.isAlive()) {

                        }
                    }
                }
                case '2' -> {
                    int n = in.read();
                    if (n == 'A') {
                        action2(affect2, map, in.read() == 'T');
                    } else {
                        action2(affect2.subList(n, n + 1), map, in.read() == 'T');
                    }
                }
                case 'M' -> moveTo(in.read(), in.read(), map);
                case 'T' -> log.add(this + " is stunned");
            }
        } catch (IOException ignore) {
        }
        tookTurn = true;
        DroidGame.removeTurnOnly();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignore) {
        }
    }

    public void endTurn() {
        removeEffects();
        tookTurn = false;
    }

    private void setMoves(DroidGame.MapMarker[][] map) {
        int speed = getStat(Stats.SPEED);
        for (int i = 0; i <= speed; i++) {
            for (int j = Math.max(0, y - i); j <= Math.min(map.length - 1, y + i); j++) {
                int toMove = speed - i;
                int n_x = x + toMove;
                if (n_x > -1 && n_x < map.length &&
                        map[n_x][j] == DroidGame.MapMarker.FREE) {
                    map[n_x][j] = DroidGame.MapMarker.CAN_MOVE;
                }
                n_x = x - toMove;
                if (n_x > -1 && n_x < map.length &&
                        map[n_x][j] == DroidGame.MapMarker.FREE) {
                    map[n_x][j] = DroidGame.MapMarker.CAN_MOVE;
                }
            }
        }
    }

    private void setAffects(ArrayList<Droid> affect1, ArrayList<Droid> affect2, DroidGame.MapMarker[][] map) {
        Stream.concat(affect1.stream(), affect2.stream())
                .forEach(d -> map[d.x][d.y] = DroidGame.MapMarker.CAN_AFFECT);
    }

    private boolean canMove(DroidGame.MapMarker[][] map) {
        for (DroidGame.MapMarker[] mapMarkers : map) {
            for (DroidGame.MapMarker marker : mapMarkers) {
                if (marker == DroidGame.MapMarker.CAN_MOVE) {
                    return true;
                }
            }
        }
        return false;
    }

    private void makeMove(DroidGame.MapMarker[][] map, FileWriter out) {
        Scanner in = new Scanner(System.in);
        String move;
        int n_x = -1;
        int n_y = -1;
        while (n_x < 0 || n_x > map.length - 1 || n_y < 0 || n_y > map.length - 1 || map[n_x][n_y] != DroidGame.MapMarker.CAN_MOVE) {
            System.out.print(this + " move to: ");
            move = in.nextLine();
            if (move.length() < 3) {
                continue;
            }
            n_x = move.charAt(0) - 'A';
            n_y = Integer.parseInt(move.substring(1));
        }
        moveTo(n_x, n_y, map);
        try {
            out.write('M');
            out.write(n_x);
            out.write(n_y);
        } catch (IOException ignore) {
        }
        tookTurn = true;
    }

    protected void moveTo(int n_x, int n_y, DroidGame.MapMarker[][] map) {
        if (n_x < 0 || n_x >= map.length || n_y < 0 || n_y >= map.length
                || map[n_x][n_y] != DroidGame.MapMarker.FREE
                && map[n_x][n_y] != DroidGame.MapMarker.CAN_MOVE) {
            return;
        }
        map[x][y] = DroidGame.MapMarker.FREE;
        map[n_x][n_y] = DroidGame.MapMarker.DROID;
        x = n_x;
        y = n_y;
        log.add("%s has moved to %c%02d".formatted(this, n_x + 'A', n_y));
    }

    protected int[] stepNear(int t_x, int t_y, int max_step, DroidGame.MapMarker[][] map) {
        for (int i = t_x - 1; i <= t_x + 1; i++) {
            for (int j = t_y - 1; j <= t_y + 1; j++) {
                if (i == x && j == y ||
                        i > -1 && i < map.length && j > -1 && j < map.length
                                && map[i][j] == DroidGame.MapMarker.CAN_MOVE
                                && distance(i, j, this) <= max_step) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    protected boolean isConfused() {
        for (Effect ignore : effects) {
            if (ignore.getName().equals("CONFUSED")) {
                return true;
            }
        }
        return false;
    }

    protected boolean isStunned() {
        Iterator<Effect> iter = effects.iterator();
        while (iter.hasNext()) {
            Effect e = iter.next();
            if (e.getName().equals("STUNNED")) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public boolean hasCoordinates(int x, int y) {
        return this.x == x && this.y == y;
    }

    protected int distance(Droid unit) {
        return Math.abs(unit.x - x) + Math.abs(unit.y - y);
    }

    protected int distance(int x, int y, Droid unit) {
        return Math.abs(unit.x - x) + Math.abs(unit.y - y);
    }

    public void addEffect(Effect effect) {
        if (!effect.getName().equals("MELT") && !effect.getName().equals("REGEN")) {
            if (effects.remove(effect)) {
                effects.add(effect);
                return;
            }
        }
        effects.add(effect);
        effect.firstApply(this);
    }

    public void applyEffects() {
        effects.forEach(ignore -> ignore.apply(this));
    }

    public void removeEffects() {
        Iterator<Effect> iter = effects.iterator();
        while (iter.hasNext()) {
            if (iter.next().remove(this)) {
                iter.remove();
            }
        }
    }

    public void takeHit(int damage) {
        damage = damage < getStat(Stats.ARMOR) ? 0 : damage - getStat(Stats.ARMOR);
        log.add(this + " has taken " + damage + " damage");
        currentHealth -= damage;
    }

    public void pureDamage(int damage, String source) {
        log.add(this + " has taken " + damage + " damage by " + source);
        currentHealth -= damage;
    }

    public void heal(int amount, String source) {
        currentHealth += amount;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
        log.add(this + " hp has been healed for " + amount + " by " + source);
    }

    public ArrayList<String> clearLog() {
        ArrayList<String> result = new ArrayList<>(log);
        log.clear();
        return result;
    }

    public void toLog(String message) {
        log.add(message);
    }

    public void changeStat(Stats stat, int amount) {
        stats.merge(stat, amount, Integer::sum);
    }

    public int getStat(Stats stat) {
        if (stat == Stats.SPEED || stat == Stats.ATTACK_DISTANCE) {
            return Math.max(1, stats.get(stat));
        }
        return stats.get(stat);
    }

    public String getMapMarker() {
        return team + mapMarker + Out.ANSI_RESET;
    }

    public int getHealth() {
        return currentHealth;
    }

    public boolean notTookTurn() {
        return !tookTurn;
    }

    public Out getTeam() {
        return team;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public String toString() {
        return team + mapMarker + this.getClass().getSimpleName() + Out.ANSI_RESET;
    }

    private void AI(ArrayList<Droid> team1, ArrayList<Droid> team2, DroidGame.MapMarker[][] map, FileWriter out) {
        ArrayList<Droid> affect1 = new ArrayList<>();
        ArrayList<Droid> affect2 = new ArrayList<>();
        setMoves(map);
        canAffect(affect1, affect2, team1, team2, map);
        if ((affect1.isEmpty() || affect1.size() == 1 && affect1.get(0) == this)
                && (affect2.isEmpty() || affect2.size() == 1 && affect2.get(0) == this)) {
            AIMove(team1, team2, map, out);
        } else {
            if (!affect1.isEmpty() && !affect2.isEmpty()) {
                if (random.nextBoolean()) {
                    makeAction1AI(affect1, map, out);
                } else {
                    makeAction2AI(affect2, map, out);
                }
            } else if (!affect1.isEmpty()) {
                makeAction1AI(affect1, map, out);
            } else {
                makeAction2AI(affect2, map, out);
            }
        }
    }

    protected void AIMove(ArrayList<Droid> team1, ArrayList<Droid> team2, DroidGame.MapMarker[][] map, FileWriter out) {
        for (int i = 1; i <= getStat(Stats.SPEED); i++) {
            for (int j = 0; j <= i; j++) {
                int step = i - j;
                if (canAffectFrom(x + step, y + j, team1, team2, map)) {
                    try {
                        out.write('M');
                        out.write(x + step);
                        out.write(y + j);
                    } catch (IOException ignore) {
                    }
                    return;
                }
                if (canAffectFrom(x + step, y - j, team1, team2, map)) {
                    try {
                        out.write('M');
                        out.write(x + step);
                        out.write(y - j);
                    } catch (IOException ignore) {
                    }
                    return;
                }
                if (canAffectFrom(x - step, y + j, team1, team2, map)) {
                    try {
                        out.write('M');
                        out.write(x - step);
                        out.write(y + j);
                    } catch (IOException ignore) {
                    }
                    return;
                }
                if (canAffectFrom(x - step, y - j, team1, team2, map)) {
                    try {
                        out.write('M');
                        out.write(x - step);
                        out.write(y - j);
                    } catch (IOException ignore) {
                    }
                    return;
                }
            }
        }
        moveClosestAI(team1, team2, map, out);
    }

    protected void moveClosestAI(ArrayList<Droid> team1, ArrayList<Droid> team2, DroidGame.MapMarker[][] map, FileWriter out) {
        int n_x = x;
        int n_y = y;
        int min_distance = Integer.MAX_VALUE;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map.length; j++) {
                if (map[i][j] == DroidGame.MapMarker.CAN_MOVE) {
                    for (Droid d : team1) {
                        int distance = distance(i, j, d);
                        if (distance < min_distance) {
                            min_distance = distance;
                            n_x = i;
                            n_y = j;
                        }
                    }
                }
            }
        }
        if (n_x != x || n_y != y) {
            moveTo(n_x, n_y, map);
            try {
                out.write('M');
                out.write(n_x);
                out.write(n_y);
            } catch (IOException ignore) {
            }
        } else {
            log.add(this + " skipped turn");
            try {
                out.write('S');
            } catch (IOException ignore) {
            }
        }
    }

    protected void makeAction1AI(List<Droid> affect, DroidGame.MapMarker[][] map, FileWriter out) {
        int ind = random.nextInt(affect.size());
        try {
            out.write('1');
            out.write(ind);
            out.write(action1(affect.subList(ind, ind + 1), map) ? 'T' : 'F');
        } catch (IOException ignore) {
        }
    }

    protected void makeAction2AI(List<Droid> affect, DroidGame.MapMarker[][] map, FileWriter out) {
        int ind = random.nextInt(affect.size());
        try {
            out.write('2');
            out.write(ind);
            out.write(action2(affect.subList(ind, ind + 1), map) ? 'T' : 'F');
        } catch (IOException ignore) {
        }
    }

    protected boolean canAffectFrom(int n_x, int n_y,
                                    ArrayList<Droid> team1, ArrayList<Droid> team2, DroidGame.MapMarker[][] map) {
        if (n_x < 0 || n_x >= map.length ||
                n_y < 0 || n_y >= map.length ||
                map[n_x][n_y] != DroidGame.MapMarker.FREE) {
            return false;
        }
        int o_x = x;
        int o_y = y;
        x = n_x;
        y = n_y;
        ArrayList<Droid> affect1 = new ArrayList<>();
        ArrayList<Droid> affect2 = new ArrayList<>();
        canAffect(affect1, affect2, team1, team2, map);
        x = o_x;
        y = o_y;
        if (!affect1.isEmpty() || !affect2.isEmpty()) {
            moveTo(n_x, n_y, map);
            return true;
        }
        return false;
    }
}