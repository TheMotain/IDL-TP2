package multiAgent.avatar.sma;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import multiAgent.ConstantParams;
import multiAgent.avatar.Avatar;
import multiAgent.avatar.CommonAgentBehavour;
import multiAgent.avatar.Defender;
import multiAgent.avatar.Hunter;
import multiAgent.avatar.Victory;
import multiAgent.avatar.Wall;
import multiAgent.core.Agent;
import multiAgent.core.Environnement;
import multiAgent.core.SMAInterface;
import multiAgent.view.MainFrame;

public class SMA implements SMAInterface, KeyListener {

	private Environnement env;

	private Agent[] agentList;

	private Avatar avatar;

	private MainFrame mainFrame;

	private boolean endOfGame;

	private boolean pause;

	private boolean init;

	private boolean victory;

	private long delay = ConstantParams.getDelay();

	private int version = 1;

	@Override
	public void initAgent(Environnement env) {
		init = true;
		pause = false;
		this.env = env;
		if (!ConstantParams.getTorus()) {
			for (int i = 0; i < ConstantParams.getGridSizeX(); i++) {
				new Wall(i, 0, env);
				new Wall(i, ConstantParams.getGridSizeY() - 1, env);
			}
			for (int i = 0; i < ConstantParams.getGridSizeY(); i++) {
				new Wall(0, i, env);
				new Wall(ConstantParams.getGridSizeX() - 1, i, env);
			}
		}
		agentList = new Agent[ConstantParams.getNumberOfHunter() + 1];
		if (version == 1) {
			initAgentV1();
		} else {
			initAgentV2();
		}
	}

	private void initAgentV1() {
		avatar = new Avatar(ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeX() - 2) + 1,
				ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeY() - 2) + 1, env);
		agentList[0] = avatar;
		for (int i = 0; i < ConstantParams.getNumberOfHunter(); i++) {
			Hunter hunter;
			int x, y;
			do {
				x = ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeX());
				y = ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeY());
			} while (!env.isEmptyCellule(x, y) || getDijkstra()[x][y] == -1);
			hunter = new Hunter(x, y, env);
			agentList[i + 1] = hunter;
			mainFrame.addKeyListener(hunter);
		}
		labyrinthGeneratorV1(env);
		avatar.calculateDijkstra();
		mainFrame.addEventKeyListener(avatar);
	}

	private void initAgentV2() {
		List<int[]> freeCell = labyrinthGeneratorV2(env);
		if (freeCell.size() < agentList.length) {
			throw new RuntimeException("To much walls");
		}
		Collections.shuffle(freeCell, ConstantParams.getRandom());
		int[] cell = freeCell.remove(0);
		avatar = new Avatar(cell[0], cell[1], env);
		mainFrame.addKeyListener(avatar);
		agentList[0] = avatar;
		Hunter hunter;
		for (int i = 1; i < agentList.length; i++) {
			cell = freeCell.remove(0);
			hunter = new Hunter(cell[0], cell[1], env);
			mainFrame.addEventKeyListener(hunter);
			agentList[i] = hunter;
		}
		avatar.calculateDijkstra();
	}

	private void labyrinthGeneratorV1(Environnement env) {
		for (int i = 0; i < ConstantParams.getNumberOfWall(); i++) {
			for (int j = 0; j < 50; j++) {
				int x, y;
				x = ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeX());
				y = ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeY());
				if (env.isEmptyCellule(x, y)) {
					boolean retry = false;
					new Wall(x, y, env);
					avatar.calculateDijkstra();
					for (Agent agent : agentList) {
						if (getDijkstra()[agent.getPosX()][agent.getNewPosY()] == -1) {
							env.getEnvironnement()[x][y] = null;
							retry = true;
							break;
						}
					}
					if (!retry) {
						break;
					}
				}
			}
		}
	}

	private List<int[]> labyrinthGeneratorV2(Environnement env) {
		List<Wall> walls = new ArrayList<>();
		for (int i = ConstantParams.getTorus() ? 0 : 1; i < (ConstantParams.getTorus() ? ConstantParams.getGridSizeX()
				: ConstantParams.getGridSizeX() - 1); i++) {
			for (int j = ConstantParams.getTorus() ? 0 : 1; j < (ConstantParams.getTorus()
					? ConstantParams.getGridSizeY()
					: ConstantParams.getGridSizeY() - 1); j++) {
				Wall current = new Wall(i, j, env);
				env.addNewAgent(current);
				walls.add(current);
			}
		}
		List<int[]> freeCell = new ArrayList<>();
		Wall current = walls.remove(ConstantParams.getRandom().nextInt(walls.size()));
		int[] currentCell = new int[] { current.getPosX(), current.getPosY() };
		freeCell.add(new int[] { current.getPosX(), current.getPosY() });
		List<int[]> movements = Arrays.asList(CommonAgentBehavour.enableMovement);
		while (walls.size() > ConstantParams.getNumberOfWall()) {
			env.updateDisplay();
			if (ConstantParams.getRandom().nextInt(4) == 0) {
				Collections.shuffle(movements, ConstantParams.getRandom());
			}
			int x = env.calculateTorus(currentCell[0] + movements.get(0)[0], ConstantParams.getGridSizeX());
			int y = env.calculateTorus(currentCell[1] + movements.get(0)[1], ConstantParams.getGridSizeY());
			if (!ConstantParams.getTorus() && (x <= 0 || y <= 0 || x >= ConstantParams.getGridSizeX() - 1
					|| y >= ConstantParams.getGridSizeY() - 1)) {
				continue;
			}
			if (!env.isEmptyCellule(x, y)) {
				freeCell.add(new int[] { x, y });
				walls.remove(env.getCell(x, y));
				env.getEnvironnement()[x][y] = null;
			}
			currentCell = new int[] { x, y };
		}
		return freeCell;
	}

	public void initAgent(Environnement env, MainFrame mainFrame, int version) {
		this.endOfGame = false;
		this.mainFrame = mainFrame;
		this.version = version;
		this.initAgent(env);
	}

	public int[][] getDijkstra() {
		return avatar.getDijkstra();
	}

	@Override
	public void run() {
		for (int i = 0; i < agentList.length; i++) {
			agentList[i].decide();
			if (endOfGame) {
				refreshEnv();
				return;
			}
		}
		generateDefender();
		generateVictory();
	}

	private int generatedDefender = 0;

	private void generateDefender() {
		if (ConstantParams.getRandom().nextInt(100 / ConstantParams.getDefenderPopProbability()) == 0) {
			int x, y, i = 0;
			do {
				x = ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeX());
				y = ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeY());
				if (++i > 50) {
					return;
				}
			} while (!env.isEmptyCellule(x, y) || getDijkstra()[x][y] == -1);
			env.addNewAgent(new Defender(x, y, env));
			generatedDefender++;
		}
	}

	private void generateVictory() {
		if (generatedDefender == ConstantParams.getDefenderVictoryPopLeft()) {
			int x, y, i = 0;
			do {
				x = ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeX());
				y = ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeY());
				if (++i > 50) {
					return;
				}
			} while (!env.isEmptyCellule(x, y) || getDijkstra()[x][y] == -1);
			env.addNewAgent(new Victory(x, y, env));
			generatedDefender = 0;
		}
	}

	public void lost() {
		victory = false;
		endOfGame();
	}

	public void win() {
		victory = true;
		env.updateDisplay();
		endOfGame();
	}

	private void refreshEnv() {
		if (!victory) {
			env.getEnvironnement()[agentList[0].getPosX()][agentList[0].getPosY()] = null;
			for (int i = 1; i < agentList.length; i++) {
				env.getEnvironnement()[agentList[i].getPosX()][agentList[i].getPosY()] = agentList[i];
			}
		} else {
			env.getEnvironnement()[avatar.getPosX()][avatar.getPosY()] = avatar;
			for (int i = 1; i < agentList.length; i++) {
				env.getEnvironnement()[agentList[i].getPosX()][agentList[i].getPosY()] = null;
			}
		}
	}

	@Override
	public void addAgent(Agent agent) {
	}

	@Override
	public void removeAgent(Agent agent) {
	}

	@Override
	public void log() {
		for (int i = 0; i < agentList.length; i++) {
			System.out.println(agentList[i]);
		}
	}

	public boolean isEndOfGame() {
		return endOfGame;
	}

	public boolean isPause() {
		return pause;
	}

	public void endOfGame() {
		endOfGame = true;
	}

	public long getDelay() {
		return delay;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		switch (arg0.getKeyCode()) {
		case KeyEvent.VK_SPACE:
			synchronized (this) {
				pause = !pause;
			}
			break;
		case KeyEvent.VK_S:
			if (init) {
				endOfGame = false;
				init = false;
			}
			break;
		case KeyEvent.VK_I:
			synchronized (this) {
				endOfGame = true;
				env.clean();
				this.initAgent(this.env);
				env.updateDisplay();
			}
			break;
		case KeyEvent.VK_W:
			if (delay > 100) {
				delay -= 100;
			}
			break;
		case KeyEvent.VK_X:
			delay += 100;
			break;
		default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {

	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}
}
