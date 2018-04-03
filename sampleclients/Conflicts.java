package sampleclients;

import java.util.List;

public class Conflicts {
	public static char[][] MainBoard;
	private List< Agent > agents;
    private List< Box > boxes;
	
	public void handleConflict(String response, List<Command> jact) {
		String tmp = response.substring(1, response.length());
		String[] parts = tmp.split(", ");
		Boolean boolean1;
		for (int i = 0; i < parts.length; i++) {
			boolean1 = Boolean.valueOf(parts[i]);
			if(boolean1 == false){
				System.err.println("conflict on agent: "+i);
				getConflictPartner(i, jact);
			}
		}
	}
	
	private int getConflictPartner(int id1) {
		
	}
}
