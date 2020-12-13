package util;

import java.util.TimerTask;

import models.StatsModel;

public class Simulator extends TimerTask {
	StatsModel dao = new StatsModel();
	@Override
	public void run() {
		dao.callStoredProcedure();
	}
}
