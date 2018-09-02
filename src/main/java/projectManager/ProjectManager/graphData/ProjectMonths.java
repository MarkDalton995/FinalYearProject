package projectManager.ProjectManager.graphData;

public class ProjectMonths {
	String start;
	String end;
	String label;
	
	public ProjectMonths() {
		
	}

	public ProjectMonths(String start, String end, String label) {
		super();
		this.start = start;
		this.end = end;
		this.label = label;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
