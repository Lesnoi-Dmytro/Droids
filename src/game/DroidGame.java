package game;

import game.droid.*;
import game.util.Out;
import game.util.Stats;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class DroidGame {
	private final static Scanner scan = new Scanner(System.in);
	private final static Random random = new Random();
	private static FileWriter out = null;
	private final static ArrayList<String> log = new ArrayList<>();
	private static MapMarker[][] battlefiend;
	private final static ArrayList<Droid> team1 = new ArrayList<>();
	private final static ArrayList<Droid> team2 = new ArrayList<>();
	private static Droid selected;
	private static int size;

	public static void main(String[] args) {
		boolean play = true;
		while (play) {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
			System.out.print("""
					2 - 2 x 2 Battle
					4 - 4 x 4 Battle
					R - play record of last game
					E - Exit
					Your chose:\s""");
			char input = scan.nextLine().charAt(0);
			switch (input) {
				case '2' -> play2x2();
				case '4' -> play4x4();
				case 'R' -> playRecord();
				case 'E' -> play = false;
			}
		}
	}

	private static void play() {
		int round = 0;
		while (!team1.isEmpty() && !team2.isEmpty()) {
			round++;
			System.out.println("\n\tRound " + round);
			applyEffects();
			do {
				selected = Stream.concat(team1.stream(), team2.stream())
						.filter(Droid::notTookTurn)
						.max(Comparator.comparing(d -> d.getStat(Stats.SPEED)))
						.orElse(null);
				if (selected != null) {
					selected.takeTurn(team1, team2, battlefiend, out);
				}
				claimDead();
			} while (selected != null);
			Stream.concat(team1.stream(), team2.stream())
					.forEach(Droid::endTurn);
			claimDead();
		}
		printMap();
		if (team1.isEmpty()) {
			if (team2.isEmpty()) {
				System.out.print("\n\tDraw");
			} else {
				System.out.print("\n\tTeam2 wins");
			}
		} else {
			System.out.print("\n\tTeam1 wins");
		}
		System.out.println(" after " + round + " rounds\n");
		team1.clear();
		team2.clear();
		scan.nextLine();
		try {
			out.close();
		} catch (IOException ignored) {
		}
	}

	private static void play2x2() {
		size = 10;
		File file = new File("log.rec");
		file.delete();
		try {
			file.createNewFile();
			out = new FileWriter("log.rec");
			out.write("2");
		} catch (IOException e) {
			System.out.println("Error during file creation");
			return;
		}
		createMap();
		makeTeam1(2);
		makeTeam2(2);
		selected = team1.get(0);
		log.clear();
		play();
	}

	private static void play4x4() {
		size = 15;
		File file = new File("log.rec");
		file.delete();
		try {
			file.createNewFile();
			out = new FileWriter("log.rec");
			out.write("4");
		} catch (IOException e) {
			System.out.println("Error during file creation");
			return;
		}
		createMap();
		makeTeam1(4);
		makeTeam2(4);
		log.clear();
		play();
	}

	private static void playRecord() {
		try (FileReader in = new FileReader("log.rec")) {
			int n = in.read();
			size = switch (n) {
				case '2' -> 10;
				case '4' -> 15;
				default -> 0;
			};
			createMap(in);
			makeTeams(in, n - '0');
			int round = 0;
			while (!team1.isEmpty() && !team2.isEmpty()) {
				round++;
				System.out.println("\n\tRound " + round);
				applyEffects();
				do {
					selected = Stream.concat(team1.stream(), team2.stream())
							.filter(Droid::notTookTurn)
							.max(Comparator.comparing(d -> d.getStat(Stats.SPEED)))
							.orElse(null);
					if (selected != null) {
						selected.takeTurn(team1, team2, battlefiend, in);
					}
					claimDead();
				} while (selected != null);
				Stream.concat(team1.stream(), team2.stream())
						.forEach(Droid::endTurn);
			}
			printMap();
			if (team1.isEmpty()) {
				if (team2.isEmpty()) {
					System.out.print("\n\tDraw");
				} else {
					System.out.print("\n\tTeam2 wins");
				}
			} else {
				System.out.print("\n\tTeam1 wins");
			}
			System.out.println(" after " + round + " rounds\n");
			team1.clear();
			team2.clear();
		} catch (IOException ignored) {
		}
	}

	private static void makeTeam1(int num) {
		LinkedList<Droid> available = new LinkedList<>(List.of(
				new Stormtrooper(Out.ANSI_BLUE, 0),
				new Defender(Out.ANSI_BLUE, 0),
				new Support(Out.ANSI_BLUE, 0),
				new Sniper(Out.ANSI_BLUE, 0),
				new Smasher(Out.ANSI_BLUE, 0),
				new Swordsman(Out.ANSI_BLUE, 0)
		));
		LinkedList<Integer> x = new LinkedList<>();
		for (int i = 0; i < size; i++) {
			x.push(i);
		}
		for (int i = 0; i < num; ) {
			System.out.println("\nYour team " + team1);
			System.out.println("Available droids:");
			for (int j = 0; j < available.size(); j++) {
				System.out.println((j + 1) + " - " + available.get(j).getClass().getSimpleName());
			}
			System.out.print("Chose the droid: ");
			int ind = scan.nextInt();
			if (ind > 0 && ind <= available.size()) {
				Droid droid = available.get(ind - 1);
				available.remove(ind - 1);
				team1.add(droid);
				System.out.println("Available start x positions: " + x);
				int n_x = -1;
				while (!x.contains(n_x)) {
					System.out.print("Chose start pos: ");
					n_x = scan.nextInt();
				}
				droid.setX(n_x);
				battlefiend[n_x][0] = MapMarker.DROID;
				x.remove(Integer.valueOf(n_x));
				i++;
				try {
					out.write(switch (droid.getClass().getSimpleName()) {
						case "Defender" -> 'D';
						case "Smasher" -> 'S';
						case "Sniper" -> 'N';
						case "Support" -> 'U';
						case "Swordsman" -> 'W';
						default -> 'T';
					});
					out.write(n_x);
				} catch (IOException ignore) {
				}
			} else {
				System.out.println("Unavailable droid");
			}
		}
	}

	private static void makeTeam2(int num) {
		LinkedList<Droid> available = new LinkedList<>(List.of(
				new Stormtrooper(Out.ANSI_RED, size - 1),
				new Defender(Out.ANSI_RED, size - 1),
				new Support(Out.ANSI_RED, size - 1),
				new Sniper(Out.ANSI_RED, size - 1),
				new Smasher(Out.ANSI_RED, size - 1),
				new Swordsman(Out.ANSI_RED, size - 1)
		));
		LinkedList<Integer> x = new LinkedList<>();
		for (int i = 0; i < size; i++) {
			x.push(i);
		}
		Collections.shuffle(available);
		Collections.shuffle(x);
		for (int i = 0; i < num; i++) {
			Droid droid = available.pop();
			int n_x = x.pop();
			droid.setX(n_x);
			battlefiend[n_x][size - 1] = MapMarker.DROID;
			team2.add(droid);
			try {
				out.write(switch (droid.getClass().getSimpleName()) {
					case "Defender" -> 'D';
					case "Smasher" -> 'S';
					case "Sniper" -> 'N';
					case "Support" -> 'U';
					case "Swordsman" -> 'W';
					default -> 'T';
				});
				out.write(n_x);
			} catch (IOException ignore) {
			}
		}
	}

	private static void makeTeams(FileReader in, int n) {
		try {
			for (int i = 0; i < n; i++) {
				team1.add(switch (in.read()) {
					case 'D' -> new Defender(Out.ANSI_BLUE, 0);
					case 'S' -> new Smasher(Out.ANSI_BLUE, 0);
					case 'N' -> new Sniper(Out.ANSI_BLUE, 0);
					case 'U' -> new Support(Out.ANSI_BLUE, 0);
					case 'W' -> new Swordsman(Out.ANSI_BLUE, 0);
					default -> new Stormtrooper(Out.ANSI_BLUE, 0);
				});
				int n_x = in.read();
				battlefiend[n_x][0] = MapMarker.DROID;
				team1.get(i).setX(n_x);
			}
			for (int i = 0; i < n; i++) {
				team2.add(switch (in.read()) {
					case 'D' -> new Defender(Out.ANSI_RED, size - 1);
					case 'S' -> new Smasher(Out.ANSI_RED, size - 1);
					case 'N' -> new Sniper(Out.ANSI_RED, size - 1);
					case 'U' -> new Support(Out.ANSI_RED, size - 1);
					case 'W' -> new Swordsman(Out.ANSI_RED, size - 1);
					default -> new Stormtrooper(Out.ANSI_RED, size - 1);
				});
				int n_x = in.read();
				battlefiend[n_x][size - 1] = MapMarker.DROID;
				team2.get(i).setX(n_x);
			}
		} catch (IOException ignored) {
		}
	}

	private static void createMap() {
		battlefiend = new MapMarker[size][];
		Arrays.setAll(battlefiend, i -> {
			MapMarker[] line = new MapMarker[size];
			Arrays.setAll(line, j -> MapMarker.FREE);
			return line;
		});
		for (int i = 0; i < random.nextInt(size - 3, 2 * size); i++) {
			battlefiend[random.nextInt(1, size - 1)][random.nextInt(1, size - 1)]
					= switch (random.nextInt(2)) {
				case 1 -> MapMarker.OBSTACLE2;
				default -> MapMarker.OBSTACLE1;
			};
		}
		try {
			for (MapMarker[] markers : battlefiend) {
				for (MapMarker marker : markers) {
					out.write(switch (marker) {
						case OBSTACLE1 -> '1';
						case OBSTACLE2 -> '2';
						default -> 'F';
					});
				}
			}
		} catch (IOException e) {
		}
	}

	private static void createMap(FileReader in) {
		try {
			battlefiend = new MapMarker[size][size];
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					battlefiend[i][j] = switch (in.read()) {
						case '1' -> MapMarker.OBSTACLE1;
						case '2' -> MapMarker.OBSTACLE2;
						default -> MapMarker.FREE;
					};
				}
			}
		} catch (IOException ignored) {
		}
	}

	public static void removeTurnOnly() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (battlefiend[i][j] == MapMarker.CAN_MOVE) {
					battlefiend[i][j] = MapMarker.FREE;
				} else if (battlefiend[i][j] == MapMarker.CAN_AFFECT) {
					battlefiend[i][j] = MapMarker.DROID;
				}
			}
		}
	}

	public static void claimDead() {
		Iterator<Droid> iter = team1.iterator();
		while (iter.hasNext()) {
			Droid unit = iter.next();
			if (!unit.isAlive()) {
				battlefiend[unit.getX()][unit.getY()] = MapMarker.DEAD;
				log.add(unit + " has died");
				iter.remove();
			}
		}
		iter = team2.iterator();
		while (iter.hasNext()) {
			Droid unit = iter.next();
			if (!unit.isAlive()) {
				battlefiend[unit.getX()][unit.getY()] = MapMarker.DEAD;
				log.add(unit + " has died");
				iter.remove();
			}
		}
	}

	public static void printMap() {
		System.out.println();
		for (int i = 0; i < size; i++) {
			System.out.println("-".repeat(size * 4 + 1));
			printUpperLine(i);
			printMidLine(i);
			printLowerLine(i);
		}
		System.out.println("-".repeat(size * 4 + 1));
		System.out.println("\tBattle log");
		for (Droid d : team1) {
			ArrayList<String> l = d.clearLog();
			if (!l.isEmpty()) {
				log.addAll(l);
			}
		}
		for (Droid d : team2) {
			ArrayList<String> l = d.clearLog();
			if (!l.isEmpty()) {
				log.addAll(l);
			}
		}
		log.forEach(System.out::println);
		log.clear();
		System.out.println("-".repeat(size * 4 + 1));
	}

	private static void printUpperLine(int line) {
		for (int i = 0; i < size; i++) {
			System.out.print("|");
			if (battlefiend[line][i] == MapMarker.DROID) {
				if (selected != null && selected.hasCoordinates(line, i)) {
					System.out.print(selected.getTeam() + " ● " + Out.ANSI_RESET);
				} else {
					System.out.print("   ");
				}
			} else {
				System.out.print(battlefiend[line][i].upperPart);
			}
		}
		System.out.println("|");
	}

	private static void printMidLine(int line) {
		for (int i = 0; i < size; i++) {
			System.out.print("|");
			if (battlefiend[line][i] == MapMarker.DROID || battlefiend[line][i] == MapMarker.CAN_AFFECT) {
				Droid droid = getDroid(line, i);
				System.out.print(droid.getMapMarker());
			} else if (battlefiend[line][i] == MapMarker.CAN_MOVE) {
				System.out.printf("%c%02d", line + 'A', i);
			} else {
				System.out.print(battlefiend[line][i].midPart);
			}
		}
		System.out.println("|");
	}

	private static void printLowerLine(int line) {
		for (int i = 0; i < size; i++) {
			System.out.print("|");
			if (battlefiend[line][i] == MapMarker.DROID || battlefiend[line][i] == MapMarker.CAN_AFFECT) {
				Droid droid = getDroid(line, i);
				System.out.printf(droid.getTeam() + "%03d" + Out.ANSI_RESET,
						droid.getHealth());
			} else if (battlefiend[line][i] == MapMarker.CAN_MOVE) {
				System.out.print(" ↑ ");
			} else {
				System.out.print("   ");
			}
		}
		System.out.println("|");
	}

	private static Droid getDroid(int x, int y) {
		for (Droid d : team1) {
			if (d.getX() == x && d.getY() == y) {
				return d;
			}
		}
		for (Droid d : team2) {
			if (d.getX() == x && d.getY() == y) {
				return d;
			}
		}
		return null;
	}

	private static void applyEffects() {
		Iterator<Droid> iter = team1.iterator();
		while (iter.hasNext()) {
			Droid d = iter.next();
			d.applyEffects();
			if (!d.isAlive()) {
				log.add(d + " has died");
				battlefiend[d.getX()][d.getY()] =
						MapMarker.DEAD;
				iter.remove();
			}
		}
		iter = team2.iterator();
		while (iter.hasNext()) {
			Droid d = iter.next();
			d.applyEffects();
			if (!d.isAlive()) {
				log.add(d + " has died");
				battlefiend[d.getX()][d.getY()] =
						MapMarker.DEAD;
				iter.remove();
			}
		}
	}

	public enum MapMarker {
		FREE("   ", "   "),
		OBSTACLE1("   ", "/̅\\"),
		OBSTACLE2(" /̅", "/  "),
		CAN_MOVE(" ↆ ", ""),
		DEAD("   ", "XXX"),
		CAN_AFFECT(" ◌ ", ""),
		DROID("", "");

		private final String upperPart;
		private final String midPart;

		MapMarker(String upperPart, String midPart) {
			this.upperPart = upperPart;
			this.midPart = midPart;
		}
	}
}