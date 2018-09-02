package projectManager.ProjectManager.controllers;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import projectManager.ProjectManager.graphData.DataPoints;
import projectManager.ProjectManager.model.CurrentEmployee;
import projectManager.ProjectManager.model.Employee;
import projectManager.ProjectManager.model.GlobalSettings;
import projectManager.ProjectManager.model.Project;
import projectManager.ProjectManager.model.Skill;
import projectManager.ProjectManager.model.Task;
import projectManager.ProjectManager.repository.EmployeeRepository;
import projectManager.ProjectManager.repository.GlobalSettingsRepository;
import projectManager.ProjectManager.repository.ProjectRepository;
import projectManager.ProjectManager.repository.SkillRepository;
import projectManager.ProjectManager.repository.TaskRepository;

@Controller
public class SettingsController {

	CurrentEmployee currentEmployee = CurrentEmployee.getInstance();

	private boolean daysSet;

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

	// Returns the settings page
	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	public String Settings(Model model) {
		if (currentEmployee.getCurrentEmployee().isManager()) {
			if (!GSetRepos.findAll().isEmpty()) {
				model.addAttribute("daysMonth", GSetRepos.findAll().get(0));
				daysSet = true;
				model.addAttribute("daysSet", daysSet);
			} else {
				daysSet = false;
				model.addAttribute("daysSet", daysSet);
			}
			return "settings/settings";
		}
		model.addAttribute("employee", currentEmployee.getCurrentEmployee());
		model.addAttribute("chartPoints", getEmployeeInfo(currentEmployee.getCurrentEmployee().getId()));
		model.addAttribute("employeeTasks", getCurrentEmployeeTasks(currentEmployee.getCurrentEmployee().getId()));
		return "home";
	}

	// Gets the selected days per month
	@RequestMapping(value = "/getDays", method = RequestMethod.POST)
	public String getDays(@RequestParam("days") String days, Model model) {
		GSetRepos.deleteAll();
		GlobalSettings gSettings = new GlobalSettings();
		gSettings.setName("Days per month");
		gSettings.setValue(days);
		GSetRepos.save(gSettings);
		model.addAttribute("daysMonth", GSetRepos.findAll().get(0));
		daysSet = true;
		model.addAttribute("daysSet", daysSet);
		return "settings/settings";
	}
	// End of General Settings

	// Returns the users settings page
	@RequestMapping(value = "/usersSettings", method = RequestMethod.GET)
	public String usersSettings(Model model) {
		if (currentEmployee.getCurrentEmployee().isManager()) {
			model.addAttribute("employees", EmpRepos.findAll());
			model.addAttribute("Employee", EmpRepos.findAll().get(0));
			model.addAttribute("skills", SkillRepos.findAll());
			return "settings/usersSettings";
		}
		model.addAttribute("employee", currentEmployee.getCurrentEmployee());
		model.addAttribute("chartPoints", getEmployeeInfo(currentEmployee.getCurrentEmployee().getId()));
		model.addAttribute("employeeTasks", getCurrentEmployeeTasks(currentEmployee.getCurrentEmployee().getId()));
		return "home";
	}

	// Returns selected employee details
	@RequestMapping(value = "/usersSettingsSelection", method = RequestMethod.POST)
	public String usersSettingsSelection(@RequestParam("employee") String email, Model model) {
		if (currentEmployee.getCurrentEmployee().isManager()) {
			model.addAttribute("employees", EmpRepos.findAll());
			model.addAttribute("Employee", EmpRepos.findByEmail(email));
			model.addAttribute("skills", SkillRepos.findAll());
			return "settings/usersSettings";
		}
		model.addAttribute("employee", currentEmployee.getCurrentEmployee());
		model.addAttribute("chartPoints", getEmployeeInfo(currentEmployee.getCurrentEmployee().getId()));
		model.addAttribute("employeeTasks", getCurrentEmployeeTasks(currentEmployee.getCurrentEmployee().getId()));
		return "home";
	}

	// Updates an employee
	@RequestMapping(value = "/updateEmployee", method = RequestMethod.POST)
	public ModelAndView updateEmployee(@RequestParam("id") long id, @RequestParam("name") String name,
			@RequestParam("email") String email) {
		Employee employee = EmpRepos.findByid(id);
		employee.setName(name);
		employee.setEmail(email);
		EmpRepos.save(employee);
		return new ModelAndView("redirect:/usersSettings");
	}

	// Updates an employee to a manager
	@RequestMapping(value = "/setManager", method = RequestMethod.POST)
	public ModelAndView updateEmployee(@RequestParam("id") long id) {
		Employee employee = EmpRepos.findByid(id);
		employee.setManager(true);
		EmpRepos.save(employee);
		return new ModelAndView("redirect:/usersSettings");
	}

	// Updates an employee to a manager
	@RequestMapping(value = "/setStandardEmp", method = RequestMethod.POST)
	public ModelAndView updateManager(@RequestParam("id") long id) {
		Employee employee = EmpRepos.findByid(id);
		employee.setManager(false);
		for (Project project : ProRepos.findAll()) {
			if (project.getProjectManager().getId() == id) {
				project.setProjectManager(null);
			}
		}
		EmpRepos.save(employee);
		return new ModelAndView("redirect:/usersSettings");
	}

	// Deletes an employee
	@RequestMapping(value = "/deleteEmployee", method = RequestMethod.POST)
	public ModelAndView deleteEmployee(@RequestParam("id") long id) {
		for (Skill s : SkillRepos.findAll()) {
			for (Employee e : s.getEmployees()) {
				if (e.getId() == id) {
					s.getEmployees().remove(e);
					break;
				}
			}
		}
		for (Task t : TaskRepos.findAll()) {
			if (t.getEmployee() != null) {
				if (t.getEmployee().getId() == id) {
					t.setEmployee(null);
				}
			}
		}
		for (Project p : ProRepos.findAll()) {
			if (p.getProjectManager() != null) {
				if (p.getProjectManager().getId() == id) {
					p.setProjectManager(null);
				}
			}
		}
		EmpRepos.deleteById(id);
		return new ModelAndView("redirect:/usersSettings");
	}

	// Add Skill to Employee
	@RequestMapping(value = "/assignEmployeeSkill", method = RequestMethod.POST)
	public ModelAndView assignEmployeeSkill(@RequestParam("skill") String name, @RequestParam("id") long id) {
		Employee employee = EmpRepos.findByid(id);
		employee.setSkill(SkillRepos.findByName(name));
		EmpRepos.save(employee);
		Skill skill = SkillRepos.findByName(name);
		skill.getEmployees().add(employee);
		SkillRepos.save(skill);
		return new ModelAndView("redirect:/usersSettings");
	}
	// End of User settings

	// Returns the skills settings page
	@RequestMapping(value = "/skillsSettings", method = RequestMethod.GET)
	public String skillsSettings(Model model) {
		if (currentEmployee.getCurrentEmployee().isManager()) {
			model.addAttribute("skills", SkillRepos.findAll());
			model.addAttribute("Skill", SkillRepos.findAll().get(0));
			return "settings/skillsSettings";
		}
		model.addAttribute("employee", currentEmployee.getCurrentEmployee());
		model.addAttribute("chartPoints", getEmployeeInfo(currentEmployee.getCurrentEmployee().getId()));
		model.addAttribute("employeeTasks", getCurrentEmployeeTasks(currentEmployee.getCurrentEmployee().getId()));
		return "home";
	}

	// Returns selected skill details
	@RequestMapping(value = "/skillsSettingsSelection", method = RequestMethod.POST)
	public String skillsSettingsSelection(@RequestParam("skill") String name, Model model) {
		if (currentEmployee.getCurrentEmployee().isManager()) {
			model.addAttribute("skills", SkillRepos.findAll());
			model.addAttribute("Skill", SkillRepos.findByName(name));
			return "settings/skillsSettings";
		}
		model.addAttribute("employee", currentEmployee.getCurrentEmployee());
		model.addAttribute("chartPoints", getEmployeeInfo(currentEmployee.getCurrentEmployee().getId()));
		model.addAttribute("employeeTasks", getCurrentEmployeeTasks(currentEmployee.getCurrentEmployee().getId()));
		return "home";
	}

	// Creates a new skill
	@RequestMapping(value = "/createSkill", method = RequestMethod.POST)
	public ModelAndView createSkill(@RequestParam("name") String name, @RequestParam("dailyRate") String dailyRate,
			Model model) {
		try {
			int dRate = Integer.parseInt(dailyRate);
			if (dRate <= 0) {
				model.addAttribute("createError", "This number must be a positive integer!");
				model.addAttribute("skills", SkillRepos.findAll());
				model.addAttribute("Skill", SkillRepos.findAll().get(0));
				return new ModelAndView("settings/skillsSettings", "", model);
			}
			Skill skill = new Skill();
			skill.setName(name);
			skill.setDailyRate(dRate);
			SkillRepos.save(skill);
			return new ModelAndView("redirect:/skillsSettings");
		} catch (NumberFormatException nfe) {
			model.addAttribute("createError", "This number must be a positive integer!");
			model.addAttribute("skills", SkillRepos.findAll());
			model.addAttribute("Skill", SkillRepos.findAll().get(0));
			return new ModelAndView("settings/skillsSettings", "", model);
		}
	}

	// Updates a skill
	@RequestMapping(value = "/updateSkill", method = RequestMethod.POST)
	public ModelAndView updateSkill(@RequestParam("id") long id, @RequestParam("name") String name,
			@RequestParam("dailyRate") String dailyRate, Model model) {
		try {
			int dRate = Integer.parseInt(dailyRate);
			if (dRate <= 0) {
				model.addAttribute("updateError", "This number must be a positive integer!");
				model.addAttribute("skills", SkillRepos.findAll());
				model.addAttribute("Skill", SkillRepos.findAll().get(0));
				return new ModelAndView("settings/skillsSettings", "", model);
			}
			Skill skill = SkillRepos.findByid(id);
			skill.setName(name);
			skill.setDailyRate(dRate);
			SkillRepos.save(skill);
			return new ModelAndView("redirect:/skillsSettings");
		} catch (NumberFormatException nfe) {
			model.addAttribute("updateError", "This number must be a positive integer!");
			model.addAttribute("skills", SkillRepos.findAll());
			model.addAttribute("Skill", SkillRepos.findAll().get(0));
			return new ModelAndView("settings/skillsSettings", "", model);
		}
	}

	// Deletes a skill
	@RequestMapping(value = "/deleteSkill", method = RequestMethod.POST)
	public ModelAndView deleteSkill(@RequestParam("id") long id) {
		Skill skill = SkillRepos.findByid(id);
		for (Task t : TaskRepos.findAll()) {
			if (t.getEmployee() != null) {
				if (t.getEmployee().getSkill().getId() == id) {
					t.setEmployee(null);
					t.setSkillRequired(null);
				}
			} else if (t.getSkillRequired().getId() == id) {
				t.setSkillRequired(null);
			}
		}
		for (Employee e : skill.getEmployees()) {
			e.setSkill(null);
			EmpRepos.save(e);
		}
		SkillRepos.deleteById(id);

		return new ModelAndView("redirect:/skillsSettings");
	}
	// End of Skill settings

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
}
