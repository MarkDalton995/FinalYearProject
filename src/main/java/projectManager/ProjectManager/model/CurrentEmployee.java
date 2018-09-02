package projectManager.ProjectManager.model;

public class CurrentEmployee {
	public static CurrentEmployee instance;
	Employee currentEmployee = new Employee();

	private CurrentEmployee() {

	}

	static {
		instance = new CurrentEmployee();
	}

	public static CurrentEmployee getInstance() {
		return instance;
	}

	public void setCurrentEmployee(Employee e) {
		this.currentEmployee = e;
	}

	public Employee getCurrentEmployee() {
		return currentEmployee;
	}

	public void resetCurrentEmployee() {
		currentEmployee = null;
	}
}
