package projectManager.ProjectManager.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
public class Task implements Serializable {

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	
	@NotNull
	private String name;
	
	@NotNull
	private String description;
	
	@NotNull
	private int duration;
	
	@NotNull
	private String minStartDate;
	
	@NotNull 
	private boolean completed;
	
	@OneToOne
	private Task dependency;
	
	@ManyToOne
	@JoinColumn(name = "skill_id")
	private Skill skillRequired;
	
	@ManyToOne
	@JoinColumn(name = "employee_id")
	private Employee employee;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "project_id")
	private Project project;
	
	public Task() {
		
	}

	public Task(String name, String description, int duration,
			String minStartDate, boolean completed, String skillRequired, 
			Project project) {
		super();
		this.name = name;
		this.description = description;
		this.duration = duration;
		this.minStartDate = minStartDate;
		this.completed = completed;
		this.skillRequired = new Skill();
		this.project = project;
		employee = new Employee();
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getMinStartDate() {
		return minStartDate;
	}

	public void setMinStartDate(String minStartDate) {
		this.minStartDate = minStartDate;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Skill getSkillRequired() {
		return skillRequired;
	}

	public void setSkillRequired(Skill skillRequired) {
		this.skillRequired = skillRequired;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Task getDependency() {
		return dependency;
	}

	public void setDependency(Task dependency) {
		this.dependency = dependency;
	}
}
