package projectManager.ProjectManager.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import projectManager.ProjectManager.graphData.*;
import projectManager.ProjectManager.model.Comment;
import projectManager.ProjectManager.model.CurrentEmployee;
import projectManager.ProjectManager.model.Employee;
import projectManager.ProjectManager.model.Project;
import projectManager.ProjectManager.model.Skill;
import projectManager.ProjectManager.model.Task;
import projectManager.ProjectManager.repository.CommentRepository;
import projectManager.ProjectManager.repository.EmployeeRepository;
import projectManager.ProjectManager.repository.GlobalSettingsRepository;
import projectManager.ProjectManager.repository.ProjectRepository;
import projectManager.ProjectManager.repository.SkillRepository;
import projectManager.ProjectManager.repository.TaskRepository;

@Controller
public class ProjectsController {

	CurrentEmployee currentEmployee = CurrentEmployee.getInstance();

	@Autowired
	GlobalSettingsRepository GSetRepos;
	@Autowired
	EmployeeRepository EmpRepos;
	@Autowired
	SkillRepository SkillRepos;
	@Autowired
	ProjectRepository ProRepos;
	@Autowired
	TaskRepository TaskRepos;
	@Autowired
	CommentRepository CommRepos;

	// Returns create project form
	@RequestMapping(value = "/createProject", method = RequestMethod.GET)
	public String CreateProject(Model model) {
		if (currentEmployee.getCurrentEmployee().isManager()) {
			ArrayList<Employee> managers = new ArrayList<Employee>();
			for (Employee e : EmpRepos.findAll()) {
				if (e.isManager()) {
					managers.add(e);
				}
			}
			model.addAttribute("managers", managers);
			return "projects/createProject";
		}
		model.addAttribute("employee", currentEmployee.getCurrentEmployee());
		model.addAttribute("chartPoints", getEmployeeInfo(currentEmployee.getCurrentEmployee().getId()));
		model.addAttribute("employeeTasks", getCurrentEmployeeTasks(currentEmployee.getCurrentEmployee().getId()));
		return "home";
	}

	// Returns the tracker page
	@RequestMapping(value = "/tracker", method = RequestMethod.GET)
	public String Tracker(Model model) {
		model.addAttribute("ProjectNames", getAllProjectNames());
		model.addAttribute("ProjectDates", getAllProjectDates());
		model.addAttribute("ProjectMonths", getProjectMonths());
		return "projects/tracker";
	}

	// Creates the project
	@RequestMapping(value = "/createTheProject", method = RequestMethod.POST)
	public ModelAndView CreateTheProject(@RequestParam("name") String name,
			@RequestParam("description") String description, @RequestParam("startDate") String startDate,
			@RequestParam("projectManager") String email) {
		Project project = new Project();
		Employee projectManager = EmpRepos.findByEmail(email);
		project.setName(name);
		project.setDescription(description);
		project.setStartDate(startDate);
		project.setProjectManager(projectManager);
		ProRepos.save(project);
		return new ModelAndView("redirect:/viewAllProjects");
	}

	// Returns the all projects page
	@RequestMapping(value = "/viewAllProjects", method = RequestMethod.GET)
	public String ViewAllProjects(Model model) {
		model.addAttribute("projects", ProRepos.findAll());
		return "projects/allProjects";
	}

	// Returns update project page
	@RequestMapping(value = "/editProject/{id}", method = RequestMethod.GET)
	public String editProject(@PathVariable long id, Model model) {
		model.addAttribute("project", ProRepos.findByid(id));
		return "projects/editProject";
	}

	// Returns update project page
	@RequestMapping(value = "/updateProject", method = RequestMethod.POST)
	public ModelAndView updateProject(@RequestParam("id") long id, @RequestParam("name") String name,
			@RequestParam("description") String description, Model model) {
		Project project = ProRepos.findByid(id);
		project.setName(name);
		project.setDescription(description);
		ProRepos.save(project);
		model.addAttribute("project", ProRepos.findByid(id));
		model.addAttribute("projectTasks", ProRepos.findByid(id).getTasks());
		model.addAttribute("comments", ProRepos.findByid(id).getComments());
		model.addAttribute("managers", managers());
		model.addAttribute("budgetTotal", budgetTotal(id));
		model.addAttribute("budgetToDate", budgetToDate(id));
		model.addAttribute("progress", getProjectPercentage(id));
		model.addAttribute("ProjectTaskNames", getAllProjectTaskNames(id));
		model.addAttribute("ProjectTaskDates", getAllProjectTaskDates(id));
		model.addAttribute("ProjectTaskMonths", getProjectTaskMonths(id));
		return new ModelAndView("projects/viewProject", "", model);
	}

	// Returns selected project from list of all projects
	@RequestMapping(value = "/viewProject/{id}", method = RequestMethod.GET)
	public String viewAProject(@PathVariable long id, Model model) {
		model.addAttribute("project", ProRepos.findByid(id));
		model.addAttribute("projectTasks", ProRepos.findByid(id).getTasks());
		model.addAttribute("comments", ProRepos.findByid(id).getComments());
		model.addAttribute("managers", managers());
		model.addAttribute("budgetTotal", budgetTotal(id));
		model.addAttribute("budgetToDate", budgetToDate(id));
		model.addAttribute("progress", getProjectPercentage(id));
		model.addAttribute("ProjectTaskNames", getAllProjectTaskNames(id));
		model.addAttribute("ProjectTaskDates", getAllProjectTaskDates(id));
		model.addAttribute("ProjectTaskMonths", getProjectTaskMonths(id));
		return "projects/viewProject";
	}

	// Deletes project
	@RequestMapping(value = "/deleteProject/{id}", method = RequestMethod.GET)
	public ModelAndView deleteProject(@PathVariable long id) {
		ProRepos.deleteById(id);
		return new ModelAndView("redirect:/viewAllProjects");
	}

	// Assign project manager
	@RequestMapping(value = "/assignProjectManager", method = RequestMethod.POST)
	public ModelAndView assignProjectManager(@RequestParam("pManager") String email, @RequestParam("id") long id,
			Model model) {
		Project project = ProRepos.findByid(id);
		Employee projectManager = EmpRepos.findByEmail(email);
		project.setProjectManager(projectManager);
		ProRepos.save(project);
		return new ModelAndView("redirect:/viewAllProjects");
	}
	// End of Projects functions

	// Returns the create task page for a project
	@RequestMapping(value = "/newTask/{id}", method = RequestMethod.GET)
	public String newTask(@PathVariable long id, Model model) {
		model.addAttribute("projectID", id);
		model.addAttribute("skills", SkillRepos.findAll());
		return "projects/newTask";
	}

	// Creates new task
	@RequestMapping(value = "/createTask", method = RequestMethod.POST)
	public ModelAndView CreateTask(@RequestParam("name") String name, @RequestParam("description") String description,
			@RequestParam("duration") String duration, @RequestParam("minStartDate") String minStartDate,
			@RequestParam("skill") String skillName, @RequestParam("projectID") long projectID, Model model) {
		Task task = new Task();
		Project project = ProRepos.findByid(projectID);
		String projectDate = project.getStartDate();
		String projectDateParts[] = projectDate.split("-", 3);
		String minStartDateParts[] = minStartDate.split("-", 3);
		int pYear = Integer.parseInt(projectDateParts[0]);
		int pMonth = Integer.parseInt(projectDateParts[1]);
		int pDay = Integer.parseInt(projectDateParts[2]);
		int tYear = Integer.parseInt(minStartDateParts[0]);
		int tMonth = Integer.parseInt(minStartDateParts[1]);
		int tDay = Integer.parseInt(minStartDateParts[2]);
		if (!(tYear >= pYear) || (tYear == pYear && !(tMonth >= pMonth))
				|| (tYear == pYear && tMonth == pMonth && !(tDay >= pDay))) {
			model.addAttribute("dateError", "Cannot have a task start before the project starts!");
			model.addAttribute("projectID", projectID);
			model.addAttribute("skills", SkillRepos.findAll());
			return new ModelAndView("projects/newTask", "", model);
		}

		Skill skillRequired = SkillRepos.findByName(skillName);
		int durationInt = 0;
		try {
			durationInt = Integer.parseInt(duration);
			if (durationInt <= 0) {
				model.addAttribute("daysError", "Duration must be a positive integer!");
				model.addAttribute("projectID", projectID);
				model.addAttribute("skills", SkillRepos.findAll());
				return new ModelAndView("projects/newTask", "", model);
			}
		} catch (NumberFormatException nfe) {
			model.addAttribute("daysError", "Duration must be a positive integer!");
			model.addAttribute("projectID", projectID);
			model.addAttribute("skills", SkillRepos.findAll());
			return new ModelAndView("projects/newTask", "", model);
		}
		task.setName(name);
		task.setDescription(description);
		task.setDuration(durationInt);
		task.setMinStartDate(minStartDate);
		task.setSkillRequired(skillRequired);
		task.setProject(ProRepos.findByid(projectID));
		project.getTasks().add(task);
		TaskRepos.save(task);
		ProRepos.save(project);
		model.addAttribute("project", ProRepos.findByid(projectID));
		model.addAttribute("projectTasks", ProRepos.findByid(projectID).getTasks());
		model.addAttribute("comments", ProRepos.findByid(projectID).getComments());
		model.addAttribute("managers", managers());
		model.addAttribute("budgetTotal", budgetTotal(projectID));
		model.addAttribute("budgetToDate", budgetToDate(projectID));
		model.addAttribute("progress", getProjectPercentage(projectID));
		model.addAttribute("ProjectTaskNames", getAllProjectTaskNames(projectID));
		model.addAttribute("ProjectTaskDates", getAllProjectTaskDates(projectID));
		model.addAttribute("ProjectTaskMonths", getProjectTaskMonths(projectID));
		return new ModelAndView("projects/viewProject", "", model);
	}

	// Returns selected task from project
	@RequestMapping(value = "/viewTask/{id}", method = RequestMethod.GET)
	public String viewTask(@PathVariable long id, Model model) {

		if (TaskRepos.findByid(id).getSkillRequired() == null) {
			model.addAttribute("skills", SkillRepos.findAll());
		}
		model.addAttribute("task", TaskRepos.findByid(id));
		model.addAttribute("employeeSkills", empsRequiredSkill(id));
		model.addAttribute("dependencies", taskDependencies(id));
		return "projects/viewTask";
	}

	// updates task
	@RequestMapping(value = "/updateTask", method = RequestMethod.POST)
	public ModelAndView updateTask(@RequestParam("name") String name, 
			@RequestParam("description") String description, @RequestParam("id") long id, Model model) {
		Task task = TaskRepos.findByid(id);
		if (TaskRepos.findByid(id).getSkillRequired() == null) {
			model.addAttribute("skills", SkillRepos.findAll());
		}
		task.setName(name);
		task.setDescription(description);
		TaskRepos.save(task);
		model.addAttribute("task", TaskRepos.findByid(id));
		model.addAttribute("employeeSkills", empsRequiredSkill(id));
		model.addAttribute("dependencies", taskDependencies(id));
		return new ModelAndView("projects/viewTask", "", model);
	}

	// updates task
	@RequestMapping(value = "/updateTaskNoDep", method = RequestMethod.POST)
	public ModelAndView updateTaskNoDep(@RequestParam("name") String name,
			@RequestParam("description") String description, @RequestParam("id") long id, Model model) {
		Task task = TaskRepos.findByid(id);
		task.setName(name);
		task.setDescription(description);
		TaskRepos.save(task);

		if (TaskRepos.findByid(id).getSkillRequired() == null) {
			model.addAttribute("skills", SkillRepos.findAll());
		}
		model.addAttribute("task", TaskRepos.findByid(id));
		model.addAttribute("employeeSkills", empsRequiredSkill(id));
		model.addAttribute("dependencies", taskDependencies(id));
		return new ModelAndView("projects/viewTask", "", model);
	}

	// deletes a task
	@RequestMapping(value = "/deleteTask", method = RequestMethod.POST)
	public ModelAndView deleteTask(@RequestParam("id") long id, Model model) {
		Long projectID = TaskRepos.findByid(id).getProject().getId();
		Task task = TaskRepos.findByid(id);
		for (Task t : TaskRepos.findAll()) {
			if (t.getDependency() != null) {
				if (t.getDependency().getId() == task.getId()) {
					t.setDependency(null);
				}
			}
		}
		TaskRepos.deleteById(id);
		model.addAttribute("project", ProRepos.findByid(projectID));
		model.addAttribute("projectTasks", ProRepos.findByid(projectID).getTasks());
		model.addAttribute("comments", ProRepos.findByid(projectID).getComments());
		model.addAttribute("managers", managers());
		model.addAttribute("budgetTotal", budgetTotal(projectID));
		model.addAttribute("budgetToDate", budgetToDate(projectID));
		model.addAttribute("progress", getProjectPercentage(projectID));
		model.addAttribute("ProjectTaskNames", getAllProjectTaskNames(projectID));
		model.addAttribute("ProjectTaskDates", getAllProjectTaskDates(projectID));
		model.addAttribute("ProjectTaskMonths", getProjectTaskMonths(projectID));
		return new ModelAndView("projects/viewProject", "", model);
	}

	// Set Skill Required
	@RequestMapping(value = "/setSkillRequired", method = RequestMethod.POST)
	public ModelAndView updateTaskSkill(@RequestParam("skill") String skillName, @RequestParam("id") long id) {
		Task task = TaskRepos.findByid(id);
		Skill skillRequired = SkillRepos.findByName(skillName);
		task.setSkillRequired(skillRequired);
		TaskRepos.save(task);
		return new ModelAndView("redirect:/viewAllProjects");
	}

	// Assigns an Employee to a task
	@RequestMapping(value = "/assignEmployeeTask", method = RequestMethod.POST)
	public ModelAndView assignEmployeeTask(@RequestParam("employeeSkill") String email, @RequestParam("id") long id,
			Model model) {
		Employee employee = EmpRepos.findByEmail(email);
		Task task = TaskRepos.findByid(id);
		Long projectID = TaskRepos.findByid(id).getProject().getId();
		employee.getTasks().add(task);
		task.setEmployee(employee);
		TaskRepos.save(task);
		EmpRepos.save(employee);
		model.addAttribute("project", ProRepos.findByid(projectID));
		model.addAttribute("projectTasks", ProRepos.findByid(projectID).getTasks());
		model.addAttribute("comments", ProRepos.findByid(projectID).getComments());
		model.addAttribute("managers", managers());
		model.addAttribute("budgetTotal", budgetTotal(projectID));
		model.addAttribute("budgetToDate", budgetToDate(projectID));
		model.addAttribute("progress", getProjectPercentage(projectID));
		model.addAttribute("ProjectTaskNames", getAllProjectTaskNames(projectID));
		model.addAttribute("ProjectTaskDates", getAllProjectTaskDates(projectID));
		model.addAttribute("ProjectTaskMonths", getProjectTaskMonths(projectID));
		return new ModelAndView("projects/viewProject", "", model);
	}

	// Returns my Tasks page(Task)
	@RequestMapping(value = "/myTasks", method = RequestMethod.GET)
	public String myProjects(Model model) {
		model.addAttribute("tasks", getEmployeeTasks(currentEmployee.getCurrentEmployee().getId()));
		return "projects/myTasks";
	}

	// Set Skill Required
	@RequestMapping(value = "/setComplete", method = RequestMethod.POST)
	public ModelAndView setComplete(@RequestParam("id") long id, Model model) {
		Task task = TaskRepos.findByid(id);
		if (task.getDependency() != null) {
			if (task.getDependency().isCompleted()) {
				task.setCompleted(true);
				TaskRepos.save(task);
			}
		} else {
			task.setCompleted(true);
			TaskRepos.save(task);
		}
		model.addAttribute("task", TaskRepos.findByid(id));
		model.addAttribute("employeeSkills", empsRequiredSkill(id));
		model.addAttribute("dependencies", taskDependencies(id));
		return new ModelAndView("projects/viewTask", "", model);
	}

	// Sets Task Dependency
	@RequestMapping(value = "/setDependency", method = RequestMethod.POST)
	public ModelAndView setTaskDependency(@RequestParam("dependency") Long dependencyId, @RequestParam("id") long id,
			Model model) {
		Task task = TaskRepos.findByid(id);
		Task dependency = TaskRepos.findByid(dependencyId);
		task.setDependency(dependency);
		TaskRepos.save(task);
		if (task.getSkillRequired() == null) {
			model.addAttribute("skills", SkillRepos.findAll());
		}
		model.addAttribute("task", TaskRepos.findByid(id));
		model.addAttribute("employeeSkills", empsRequiredSkill(id));
		model.addAttribute("dependencies", taskDependencies(id));
		return new ModelAndView("projects/viewTask", "", model);
	}
	// End of Task functions

	// Create a comment
	@RequestMapping(value = "/createComment", method = RequestMethod.POST)
	public ModelAndView createComment(@RequestParam("comment") String theComment, @RequestParam("id") long id,
			Model model) {
		Employee employee = currentEmployee.getCurrentEmployee();
		Project project = ProRepos.findByid(id);
		Comment comment = new Comment();
		comment.setComment(theComment);
		comment.setEmployee(employee);
		comment.setProject(project);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String dateTime = dtf.format(now);
		comment.setDateTime(dateTime);
		CommRepos.save(comment);
		project.getComments().add(comment);
		ProRepos.save(project);
		model.addAttribute("project", ProRepos.findByid(id));
		model.addAttribute("projectTasks", ProRepos.findByid(id).getTasks());
		model.addAttribute("comments", ProRepos.findByid(id).getComments());
		model.addAttribute("managers", managers());
		model.addAttribute("budgetTotal", budgetTotal(id));
		model.addAttribute("budgetToDate", budgetToDate(id));
		model.addAttribute("progress", getProjectPercentage(id));
		model.addAttribute("ProjectTaskNames", getAllProjectTaskNames(id));
		model.addAttribute("ProjectTaskDates", getAllProjectTaskDates(id));
		model.addAttribute("ProjectTaskMonths", getProjectTaskMonths(id));
		return new ModelAndView("projects/viewProject", "", model);
	}

	// Methods for returning data for use in models

	// Returns a list of tasks excluding the one given, and all others
	// that don't end before this task must start
	public ArrayList<Task> taskDependencies(Long id) {
		Task task = TaskRepos.findByid(id);
		Long projectID = task.getProject().getId();
		ArrayList<Task> taskDependencies = new ArrayList<Task>();
		for (Task t : TaskRepos.findAll()) {
			if (t.getId() != id && t.getProject().getId() == projectID) {
				String taskDateParts[] = task.getMinStartDate().split("-", 3);
				String tDateParts[] = t.getMinStartDate().split("-", 3);
				int taskYear = Integer.parseInt(taskDateParts[0]);
				int taskMonth = Integer.parseInt(taskDateParts[1]);
				int taskDay = Integer.parseInt(taskDateParts[2]);
				int tYear = Integer.parseInt(tDateParts[0]);
				int tMonth = Integer.parseInt(tDateParts[1]);
				int tDay = Integer.parseInt(tDateParts[2]);
				if (tMonth == 9 || tMonth == 4 || tMonth == 6 || tMonth == 11) {
					tDay = tDay + t.getDuration();
					if (tDay > 30) {
						tDay = tDay - 30;
						tMonth = tMonth + 1;
						if (tMonth > 12) {
							tMonth = tMonth - 12;
							tYear = tYear + 1;
						}
					}
				}
				if (tMonth == 2) {
					tDay = tDay + t.getDuration();
					if (tDay > 28) {
						tDay = tDay - 28;
						tMonth = tMonth + 1;
						if (tMonth > 12) {
							tMonth = tMonth - 12;
							tYear = tYear + 1;
						}
					}
				} else {
					tDay = tDay + t.getDuration();
					if (tDay > 31) {
						tDay = tDay - 31;
						tMonth = tMonth + 1;
						if (tMonth > 12) {
							tMonth = tMonth - 12;
							tYear = tYear + 1;
						}
					}
				}
				if (tYear < taskYear || tYear == taskYear && tMonth < taskMonth
						|| tYear == taskYear && tMonth == taskMonth && tDay < taskDay) {
					taskDependencies.add(t);
				} else {

				}
			}
		}
		return taskDependencies;
	}

	// Returns a list of all employees with required skill for task
	public ArrayList<Employee> empsRequiredSkill(Long id) {
		ArrayList<Employee> empsWithSkillRequired = new ArrayList<Employee>();
		if (TaskRepos.findByid(id).getSkillRequired() != null) {
			for (Employee e : EmpRepos.findAll()) {
				if (e.getSkill() != null) {
					if (e.getSkill().getName().equalsIgnoreCase(TaskRepos.findByid(id).getSkillRequired().getName())) {
						empsWithSkillRequired.add(e);
					}
				}
			}
		}
		return empsWithSkillRequired;
	}

	// returns project budget total
	public int budgetTotal(Long id) {
		Project project = ProRepos.findByid(id);
		int total = 0;
		if (!project.getTasks().isEmpty()) {
			for (Task t : project.getTasks()) {
				if (t.getSkillRequired() != null) {
					total = total + (t.getDuration() * t.getSkillRequired().getDailyRate());
				}
			}
		}
		return total;
	}

	// returns project budget total to date
	public int budgetToDate(Long id) {
		Project project = ProRepos.findByid(id);
		int toDate = 0;
		if (!project.getTasks().isEmpty()) {
			for (Task t : project.getTasks()) {
				if (t.isCompleted()) {
					toDate = toDate + (t.getDuration() * t.getSkillRequired().getDailyRate());
				}
			}
		}
		return toDate;
	}

	// Returns a list of managers
	public ArrayList<Employee> managers() {
		ArrayList<Employee> managers = new ArrayList<Employee>();
		for (Employee e : EmpRepos.findAll()) {
			if (e.isManager()) {
				managers.add(e);
			}
		}
		return managers;
	}

	// Returns employee projects list
	public ArrayList<Project> getEmployeeProjects(Long id) {
		ArrayList<Project> list = new ArrayList<Project>();
		Employee e = EmpRepos.findByid(id);
		if (e.getTasks() != null) {
			for (Task t : e.getTasks()) {
				list.add(t.getProject());
			}
		}
		return list;
	}

	// Returns list of employee tasks
	public ArrayList<Task> getEmployeeTasks(Long id) {
		ArrayList<Task> list = new ArrayList<Task>();
		Employee e = EmpRepos.findByid(id);
		if (e.getTasks() != null) {
			for (Task t : e.getTasks()) {
				list.add(t);
			}
		}
		return list;
	}

	// Returns Project percentage completed
	public String getProjectPercentage(Long id) {
		Project project = ProRepos.findByid(id);
		double totalDays = 0;
		double daysCompleted = 0;
		double percentage = 0;
		String value = "";
		if (!(project.getTasks().isEmpty())) {
			for (Task t : project.getTasks()) {
				totalDays = totalDays + t.getDuration();
				if (t.isCompleted()) {
					daysCompleted = daysCompleted + t.getDuration();
				}
			}
			percentage = (daysCompleted / totalDays) * 100;
			value = String.valueOf(percentage);
			value = value + "%";
		}

		return value;
	}

	// Returns list of data points to be used as pie chart info
	public ArrayList<DataPoints> getEmployeeInfo(Long id) {
		ArrayList<DataPoints> dataPoints = new ArrayList<DataPoints>();
		int monthDays = Integer.parseInt(GSetRepos.findAll().get(0).getValue());
		int empDays = 0;
		Employee employee = EmpRepos.findByid(id);
		if (!employee.getTasks().isEmpty()) {
			for (Task t : employee.getTasks()) {
				if (!t.isCompleted()) {
					DataPoints dataPoint = new DataPoints();
					dataPoint.setY(t.getDuration());
					dataPoint.setLabel(t.getName());
					dataPoints.add(dataPoint);
					empDays = empDays + t.getDuration();
				}
			}
		}
		if (empDays < monthDays) {
			empDays = monthDays - empDays;
			DataPoints dataPoint = new DataPoints();
			dataPoint.setLabel("Free Days");
			dataPoint.setY(empDays);
			dataPoints.add(dataPoint);
		}
		return dataPoints;
	}

	// Returns a list of Employee tasks
	public ArrayList<Task> getCurrentEmployeeTasks(Long id) {
		ArrayList<Task> list = new ArrayList<Task>();
		Employee e = EmpRepos.findByid(id);
		if (e.getTasks() != null) {
			for (Task t : e.getTasks()) {
				if (!(t.isCompleted())) {
					list.add(t);
				}
			}
		}
		return list;
	}

	// Returns all project names as long as they have at least one task
	public ArrayList<ProjectNames> getAllProjectNames() {
		ArrayList<ProjectNames> projectNames = new ArrayList<ProjectNames>();
		for (Project project : ProRepos.findAll()) {
			if (!(project.getTasks().isEmpty())) {
				ProjectNames name = new ProjectNames();
				name.setLabel(project.getName());
				projectNames.add(name);
			}
		}
		return projectNames;
	}

	// Returns the start and end date of all projects
	public ArrayList<ProjectDates> getAllProjectDates() {
		ArrayList<ProjectDates> projectDates = new ArrayList<ProjectDates>();
		for (Project project : ProRepos.findAll()) {
			if (!(project.getTasks().isEmpty())) {
				ProjectDates projectDate = new ProjectDates();

				String endDate = project.getStartDate();

				for (Task t : project.getTasks()) {
					String endDateParts[] = endDate.split("-", 3);
					int endYear = Integer.parseInt(endDateParts[0]);
					int endMonth = Integer.parseInt(endDateParts[1]);
					int endDay = Integer.parseInt(endDateParts[2]);

					String taskDateParts[] = t.getMinStartDate().split("-", 3);
					int tYear = Integer.parseInt(taskDateParts[0]);
					int tMonth = Integer.parseInt(taskDateParts[1]);
					int tDay = Integer.parseInt(taskDateParts[2]);

					if (tMonth == 9 || tMonth == 4 || tMonth == 6 || tMonth == 11) {
						tDay = tDay + t.getDuration();
						if (tDay > 30) {
							tDay = tDay - 30;
							tMonth = tMonth + 1;
							if (tMonth > 12) {
								tMonth = tMonth - 12;
								tYear = tYear + 1;
							}
						}
					}
					if (tMonth == 2) {
						tDay = tDay + t.getDuration();
						if (tDay > 28) {
							tDay = tDay - 28;
							tMonth = tMonth + 1;
							if (tMonth > 12) {
								tMonth = tMonth - 12;
								tYear = tYear + 1;
							}
						}
					}
					if (tMonth == 1 || tMonth == 3 || tMonth == 5 || tMonth == 7 || tMonth == 8 || tMonth == 10
							|| tMonth == 12) {
						tDay = tDay + t.getDuration();
						if (tDay > 31) {
							tDay = tDay - 31;
							tMonth = tMonth + 1;
							if (tMonth > 12) {
								tMonth = tMonth - 12;
								tYear = tYear + 1;
							}
						}
					}

					if (tYear > endYear || tYear == endYear && tMonth > endMonth
							|| tYear == endYear && tMonth == endMonth && tDay > endDay) {
						endDate = (tYear + "-" + tMonth + "-" + tDay);
						String end = "";
						if (tDay < 10) {
							end = ("0" + tMonth + "/0" + tDay + "/" + tYear);
						} else {
							end = ("0" + tMonth + "/" + tDay + "/" + tYear);
						}
						String start = "";
						String startDateParts[] = project.getStartDate().split("-", 3);
						start = (startDateParts[1] + "/" + startDateParts[2] + "/" + startDateParts[0]);

						projectDate.setStart(start);
						projectDate.setEnd(end);
					}
				}
				projectDates.add(projectDate);
			}
		}
		return projectDates;
	}

	// Returns all project task names as long as they have at least one task
	public ArrayList<ProjectNames> getAllProjectTaskNames(Long id) {
		ArrayList<ProjectNames> projectTaskNames = new ArrayList<ProjectNames>();
		Project project = ProRepos.findByid(id);
		if (!(project.getTasks().isEmpty())) {
			for (Task t : project.getTasks()) {
				ProjectNames taskName = new ProjectNames();
				taskName.setLabel(t.getName());
				projectTaskNames.add(taskName);
			}
		}
		return projectTaskNames;
	}

	// Returns the start and end date of a projects tasks
	public ArrayList<ProjectDates> getAllProjectTaskDates(Long id) {
		ArrayList<ProjectDates> projectTaskDates = new ArrayList<ProjectDates>();
		Project project = ProRepos.findByid(id);
		if (!(project.getTasks().isEmpty())) {
			for (Task t : project.getTasks()) {
				ProjectDates projectTaskDate = new ProjectDates();
				String startDate = t.getMinStartDate();
				String startDateParts[] = startDate.split("-", 3);
				int Year = Integer.parseInt(startDateParts[0]);
				int Month = Integer.parseInt(startDateParts[1]);
				int Day = Integer.parseInt(startDateParts[2]);

				if (Month == 9 || Month == 4 || Month == 6 || Month == 11) {
					Day = Day + t.getDuration();
					if (Day > 30) {
						Day = Day - 30;
						Month = Month + 1;
						if (Month > 12) {
							Month = Month - 12;
							Year = Year + 1;
						}
					}
				}
				if (Month == 2) {
					Day = Day + t.getDuration();
					if (Day > 28) {
						Day = Day - 28;
						Month = Month + 1;
						if (Month > 12) {
							Month = Month - 12;
							Year = Year + 1;
						}
					}
				}
				if (Month == 1 || Month == 3 || Month == 5 || Month == 7 || Month == 8 || Month == 10 || Month == 12) {
					Day = Day + t.getDuration();
					if (Day > 31) {
						Day = Day - 31;
						Month = Month + 1;
						if (Month > 12) {
							Month = Month - 12;
							Year = Year + 1;
						}
					}
				}
				String end = "";
				if (Day < 10) {
					end = ("0" + Month + "/0" + Day + "/" + Year);
				} else {
					end = ("0" + Month + "/" + Day + "/" + Year);
				}
				String start = (startDateParts[1] + "/" + startDateParts[2] + "/" + startDateParts[0]);
				projectTaskDate.setStart(start);
				projectTaskDate.setEnd(end);
				projectTaskDates.add(projectTaskDate);
			}
		}
		return projectTaskDates;
	}
	
	//Get Months for all projects Gantt chart
	public ArrayList<ProjectMonths> getProjectMonths() {
		ArrayList<ProjectMonths> projectMonths = new ArrayList<ProjectMonths>();
		if (!(ProRepos.findAll().isEmpty())) {
			int lowestMonth = 0;
			int highestMonth = 0;
			for (Project project : ProRepos.findAll()) {
				String[] startDateParts = project.getStartDate().split("-");
				int projectStartMonth = Integer.parseInt(startDateParts[1]);
				
				if (lowestMonth == 0 && highestMonth == 0) {
					lowestMonth = projectStartMonth;
					highestMonth = projectStartMonth;
				}
				if (lowestMonth > projectStartMonth) {
					lowestMonth = projectStartMonth;
				}
				if (highestMonth < projectStartMonth) {
					highestMonth = projectStartMonth;
				}
			}
			
			for (int i = lowestMonth - 1; i <= highestMonth + 1; i++) {
				ProjectMonths projectMonth = new ProjectMonths();
				if (i == 1) {projectMonth.setStart("01/01/2018"); projectMonth.setEnd("01/31/2018"); projectMonth.setLabel("January");} 
				if (i == 2) {projectMonth.setStart("02/01/2018"); projectMonth.setEnd("02/28/2018"); projectMonth.setLabel("February");}
				if (i == 3) {projectMonth.setStart("03/01/2018"); projectMonth.setEnd("03/31/2018"); projectMonth.setLabel("March");} 
				if (i == 4) {projectMonth.setStart("04/01/2018"); projectMonth.setEnd("04/30/2018"); projectMonth.setLabel("April");}
				if (i == 5) {projectMonth.setStart("05/01/2018"); projectMonth.setEnd("05/31/2018"); projectMonth.setLabel("May");} 
				if (i == 6) {projectMonth.setStart("06/01/2018"); projectMonth.setEnd("06/30/2018"); projectMonth.setLabel("June");}
				if (i == 7) {projectMonth.setStart("07/01/2018"); projectMonth.setEnd("07/31/2018"); projectMonth.setLabel("July");} 
				if (i == 8) {projectMonth.setStart("08/01/2018"); projectMonth.setEnd("08/31/2018"); projectMonth.setLabel("August");}
				if (i == 9) {projectMonth.setStart("09/01/2018"); projectMonth.setEnd("09/30/2018"); projectMonth.setLabel("September");} 
				if (i == 10) {projectMonth.setStart("10/01/2018"); projectMonth.setEnd("10/31/2018"); projectMonth.setLabel("October");}
				if (i == 11) {projectMonth.setStart("11/01/2018"); projectMonth.setEnd("11/30/2018"); projectMonth.setLabel("November");} 
				if (i == 12) {projectMonth.setStart("12/01/2018"); projectMonth.setEnd("12/31/2018"); projectMonth.setLabel("December");}
				projectMonths.add(projectMonth);
			}
		}
		return projectMonths;
	}
	
	//Get Months for a project Gantt chart
	public ArrayList<ProjectMonths> getProjectTaskMonths(Long id) {
		ArrayList<ProjectMonths> projectTaskMonths = new ArrayList<ProjectMonths>();
		Project project = ProRepos.findByid(id);
		int lowestMonth = 0;
		int highestMonth = 0;
		if (!(project.getTasks().isEmpty())) {
			for (Task task : project.getTasks()) {
				String[] startDateParts = task.getMinStartDate().split("-");
				int taskStartMonth = Integer.parseInt(startDateParts[1]);
				
				if (lowestMonth == 0 && highestMonth == 0) {
					lowestMonth = taskStartMonth;
					highestMonth = taskStartMonth;
				}
				if (lowestMonth > taskStartMonth) {
					lowestMonth = taskStartMonth;
				}
				if (highestMonth < taskStartMonth) {
					highestMonth = taskStartMonth;
				}
			}
			for (int i = lowestMonth - 1; i <= highestMonth + 1; i++) {
				ProjectMonths projectTaskMonth = new ProjectMonths();
				if (i == 1) {projectTaskMonth.setStart("01/01/2018"); projectTaskMonth.setEnd("01/31/2018"); projectTaskMonth.setLabel("January");} 
				if (i == 2) {projectTaskMonth.setStart("02/01/2018"); projectTaskMonth.setEnd("02/28/2018"); projectTaskMonth.setLabel("February");}
				if (i == 3) {projectTaskMonth.setStart("03/01/2018"); projectTaskMonth.setEnd("03/31/2018"); projectTaskMonth.setLabel("March");} 
				if (i == 4) {projectTaskMonth.setStart("04/01/2018"); projectTaskMonth.setEnd("04/30/2018"); projectTaskMonth.setLabel("April");}
				if (i == 5) {projectTaskMonth.setStart("05/01/2018"); projectTaskMonth.setEnd("05/31/2018"); projectTaskMonth.setLabel("May");} 
				if (i == 6) {projectTaskMonth.setStart("06/01/2018"); projectTaskMonth.setEnd("06/30/2018"); projectTaskMonth.setLabel("June");}
				if (i == 7) {projectTaskMonth.setStart("07/01/2018"); projectTaskMonth.setEnd("07/31/2018"); projectTaskMonth.setLabel("July");} 
				if (i == 8) {projectTaskMonth.setStart("08/01/2018"); projectTaskMonth.setEnd("08/31/2018"); projectTaskMonth.setLabel("August");}
				if (i == 9) {projectTaskMonth.setStart("09/01/2018"); projectTaskMonth.setEnd("09/30/2018"); projectTaskMonth.setLabel("September");} 
				if (i == 10) {projectTaskMonth.setStart("10/01/2018"); projectTaskMonth.setEnd("10/31/2018"); projectTaskMonth.setLabel("October");}
				if (i == 11) {projectTaskMonth.setStart("11/01/2018"); projectTaskMonth.setEnd("11/30/2018"); projectTaskMonth.setLabel("November");} 
				if (i == 12) {projectTaskMonth.setStart("12/01/2018"); projectTaskMonth.setEnd("12/31/2018"); projectTaskMonth.setLabel("December");}
				projectTaskMonths.add(projectTaskMonth);
			}
		}
		return projectTaskMonths;
	}
	
}
