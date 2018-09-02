package projectManager.ProjectManager.graphData;

public class DataPoints {
	int y;
	String label;

	public DataPoints() {

	}

	public DataPoints(int y, String label) {
		super();
		this.y = y;
		this.label = label;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
