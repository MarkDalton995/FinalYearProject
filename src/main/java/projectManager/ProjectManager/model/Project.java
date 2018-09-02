package projectManager.ProjectManager.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "projects")
@EntityListeners(AuditingEntityListener.class)
public class Project implements Serializable {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	
	@NotNull
    private String name;
	
	@NotNull
    private String description;
	
	@NotNull
    private String startDate;
	
	@OneToOne
    private Employee projectManager;
	
	@OneToMany(mappedBy="project", cascade={CascadeType.REMOVE})
	private List<Task> tasks;
	
    @OneToMany(mappedBy="project", cascade={CascadeType.REMOVE})
    private List<Comment> comments;
    
	public Project() {
		
	}

	public Project(String name, String description, 
			String startDate) {
		super();
		this.name = name;
		this.description = description;
		this.startDate = startDate;
		projectManager = new Employee();
		tasks = new ArrayList<Task>();
		comments = new ArrayList<Comment>();
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

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public Employee getProjectManager() {
		return projectManager;
	}

	public void setProjectManager(Employee projectManager) {
		this.projectManager = projectManager;
	}
	
	public List<Task> getTasks() {
		return tasks;
	}
	
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	
	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
}
