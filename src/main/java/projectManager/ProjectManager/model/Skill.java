package projectManager.ProjectManager.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "skills")
@EntityListeners(AuditingEntityListener.class)
public class Skill implements Serializable {

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	
	@NotNull
	private String name;
	
	@NotNull
	private int dailyRate;
	
	@OneToMany (mappedBy="skillRequired")
	private List<Task> tasks;
	
	@OneToMany(mappedBy="skill")
	private List<Employee> employees;

	public Skill() {
		
	}
	
	public Skill(String name, int dailyRate) {
		super();
		this.name = name;
		this.dailyRate = dailyRate;
		employees = new ArrayList<Employee>();
		tasks = new ArrayList<Task>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDailyRate() {
		return dailyRate;
	}

	public void setDailyRate(int dailyRate) {
		this.dailyRate = dailyRate;
	}

	public List<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(List<Employee> employees) {
		this.employees = employees;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
}
