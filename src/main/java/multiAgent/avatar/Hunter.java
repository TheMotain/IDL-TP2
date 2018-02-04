package multiAgent.avatar;

import java.awt.Color;
import java.awt.Graphics;

import multiAgent.ConstantParams;
import multiAgent.avatar.sma.SMA;
import multiAgent.core.Environnement;

public class Hunter extends CommonAgentBehavour {

	private int nextX, nextY;

	public Hunter(int posX, int posY, Environnement env) {
		super(posX, posY, env);
		nextX = posX; nextY = posY;
	}

	@Override
	public boolean canGoOn() {
		return true;
	}
	
	@Override
	public void decide() {
		int[][] dijkstra = ((SMA) env.getSMA()).getDijkstra();
		int currMin = dijkstra[posX][posY];
		int calculatePosX, calculatePosY;
		for(int[] move : enableMovement) {
			calculatePosX = posX + move[0];
			calculatePosY = posY + move[1];
			if(dijkstra[calculatePosX][calculatePosY] != -1 
					&& dijkstra[calculatePosX][calculatePosY] < currMin) {
				nextX = calculatePosX;
				nextY = calculatePosY;
				currMin = dijkstra[nextX][nextY];
			}
		}
		while (dijkstra[nextX][nextY] == -1) {
			int[] move = enableMovement[ConstantParams.getRandom().nextInt(enableMovement.length)];
			nextX += move[0];
			nextY += move[1];
		}
		env.applyTransition(this);
	}

	@Override
	public void update() {
		posX = nextX;
		posY = nextY;
		if(((SMA)env.getSMA()).getDijkstra()[posX][posY] == 0) {
			((SMA)env.getSMA()).endOfGame();
		}
	}

	@Override
	public int getNewPosX() {
		return nextX;
	}

	@Override
	public int getNewPosY() {
		return nextY;
	}

	@Override
	public void drawAgent(Graphics g) {
		g.setColor(Color.BLUE);
		super.drawAgent(g);
	}
	
	@Override
	public String toString() {
		return "Hunter;"+ (posX - nextX) +";" + (posY - nextY) + ";" + super.toString();
	}
}
