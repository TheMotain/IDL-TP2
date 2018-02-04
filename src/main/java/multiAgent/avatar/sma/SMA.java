package multiAgent.avatar.sma;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import multiAgent.ConstantParams;
import multiAgent.avatar.Avatar;
import multiAgent.avatar.Hunter;
import multiAgent.avatar.Wall;
import multiAgent.core.Agent;
import multiAgent.core.Environnement;
import multiAgent.core.SMAInterface;
import multiAgent.view.MainFrame;

public class SMA implements SMAInterface, KeyListener{
	
	private Agent[] agentList;
	
	private Avatar avatar;
	
	private MainFrame mainFrame;

	private boolean endOfGame;
	
	@Override
	public void initAgent(Environnement env) {
		endOfGame = false;
		for(int i = 0; i < ConstantParams.getGridSizeX(); i++) {
			new Wall(i, 0, env);
			new Wall(i, ConstantParams.getGridSizeY() - 1, env);
		}
		for(int i = 0; i < ConstantParams.getGridSizeY(); i++) {
			new Wall(0, i, env);
			new Wall(ConstantParams.getGridSizeX() - 1, i, env);
		}
		agentList = new Agent[ConstantParams.getNumberOfHunter() + 1];
		avatar = new Avatar(
				ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeX() - 2) + 1,
				ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeY() - 2) + 1,
				env);
		agentList[0] = avatar;
		for(int i = 0; i < ConstantParams.getNumberOfHunter(); i++) {
			int x,y;
			do {
				x = ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeX());
				y = ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeY());
			}while(!env.isEmptyCellule(x, y) && getDijkstra()[x][y] == -1);
			agentList[i + 1] = new Hunter(x, y, env);
		}
		for(int i = 0; i < ConstantParams.getNumberOfWall(); i++) {
			for(int j = 0; j < 50; j++) {
				int x,y;
				x = ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeX());
				y = ConstantParams.getRandom().nextInt(ConstantParams.getGridSizeY());
				if(env.isEmptyCellule(x, y)) {
					boolean retry = false;
					new Wall(x, y, env);
					avatar.calculateDijkstra();
					for(Agent agent : agentList) {
						if(getDijkstra()[agent.getPosX()][agent.getNewPosY()] == -1) {
							env.getEnvironnement()[x][y] = null;
							retry = true;
							break;
						}
					}
					if(!retry) {
						break;
					}
				}
			}
		}
		avatar.calculateDijkstra();
		mainFrame.addEventKeyListener(avatar);
	}
	
	public void initAgent(Environnement env, MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.initAgent(env);
	}
	
	public int[][] getDijkstra() {
		return avatar.getDijkstra();
	}

	@Override
	public void run() {
		for(int i = 0; i < agentList.length; i++){
			agentList[i].decide();
		}
	}

	@Override
	public void addAgent(Agent agent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAgent(Agent agent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void log() {
		for(int i = 0; i < agentList.length; i++) {
			System.out.println(agentList[i]); 
		}
	}

	public boolean isEndOfGame() {
		return endOfGame;
	}

	public void endOfGame() {
		endOfGame = true;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_SPACE){
			endOfGame = !endOfGame;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}
}
