package projectManager.ProjectManager.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "comments")
@EntityListeners(AuditingEntityListener.class)
public class Comment implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull
	private String comment;
	
	@NotNull
	private String dateTime;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "project_id")
	private Project project;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "employee_id")
	private Employee employee;

	public Comment() {

	}

	public Comment(String comment, String dateTime, Project project, Employee employee) {
		super();
		this.dateTime = dateTime;
		this.comment = comment;
		this.project = project;
		this.employee = employee;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}
}
