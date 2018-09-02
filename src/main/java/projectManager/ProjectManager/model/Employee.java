package projectManager.ProjectManager.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "employees")
@EntityListeners(AuditingEntityListener.class)
public class Employee implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull
	private String email;

	@NotNull
	private String name;

	@NotNull
	private String password;

	@NotNull
	private boolean manager;

	@ManyToOne
	@JoinColumn(name = "skill_id")
	private Skill skill;

	@OneToMany(mappedBy = "employee", cascade = { CascadeType.PERSIST })
	private List<Task> tasks;

	@OneToMany(mappedBy = "employee", cascade = { CascadeType.REMOVE })
	private List<Comment> comments;

	public Employee() {

	}

	public Employee(String email, String name, String password, boolean manager) {
		super();
		this.email = email;
		this.name = name;
		this.password = password;
		this.manager = manager;
		tasks = new ArrayList<Task>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isManager() {
		return manager;
	}

	public void setManager(boolean manager) {
		this.manager = manager;
	}

	public Skill getSkill() {
		return skill;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
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
