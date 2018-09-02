package projectManager.ProjectManager.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import projectManager.ProjectManager.DTO.*;
import projectManager.ProjectManager.graphData.DataPoints;
import projectManager.ProjectManager.model.*;
import projectManager.ProjectManager.repository.*;

@Controller
@RequestMapping("/")
public class LoginController extends HttpServlet {

	@Autowired
	EmployeeRepository EmpRepos;
	@Autowired
	GlobalSettingsRepository GSetRepos;

	CurrentEmployee currentEmployee = CurrentEmployee.getInstance();

	// Main login page
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String Welcome(HttpServletRequest request, HttpServletResponse response, Model model)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			model.addAttribute("overAllocated", overAllocated());
			Employee employee = (Employee) session.getAttribute("employee");
			currentEmployee.setCurrentEmployee(employee);
			model.addAttribute("chartPoints", getEmployeeInfo(currentEmployee.getCurrentEmployee().getId()));
			model.addAttribute("employeeTasks", getCurrentEmployeeTasks(currentEmployee.getCurrentEmployee().getId()));
			model.addAttribute("overLappingTasks", CheckAllEmployeeTasks());
			return "home";
		}
		return "login";
	}

	// If login details are found, go to home page, else, go to login page
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ModelAndView Login(Model model, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		for (Employee e : EmpRepos.findAll()) {
			if (e.getEmail().equals(email) && e.getPassword().equals(password)) {
				currentEmployee.setCurrentEmployee(e);
				HttpSession session = request.getSession();
				session.setAttribute("employee", currentEmployee.getCurrentEmployee());
				model.addAttribute("session", session);
				model.addAttribute("overAllocated", overAllocated());
				model.addAttribute("chartPoints", getEmployeeInfo(currentEmployee.getCurrentEmployee().getId()));
				model.addAttribute("employeeTasks",
						getCurrentEmployeeTasks(currentEmployee.getCurrentEmployee().getId()));
				model.addAttribute("overLappingTasks", CheckAllEmployeeTasks());
				return new ModelAndView("home", "", model);
			}
		}
		model.addAttribute("error", "Email or password entered was incorrect!");
		return new ModelAndView("login", "", model);
	}

	// logout
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public ModelAndView Logout(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		currentEmployee.resetCurrentEmployee();
		return new ModelAndView("redirect:/");
	}

	// Security page
	@RequestMapping(value = "/security", method = RequestMethod.GET)
	public String Secuirty(Model model) {
		if (currentEmployee.getCurrentEmployee() == null) {
			return "login";
		}
		model.addAttribute("employee", currentEmployee.getCurrentEmployee());
		return "security";
	}

	// update Password
	@RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
	public ModelAndView updatePassword(@RequestParam("password") String password,
			@RequestParam("newPass") String newPass, @RequestParam("confirmPass") String confirmPass,
			@RequestParam("id") long id, Model model) {
		Employee employee = EmpRepos.findByid(id);
		if (employee.getPassword().equals(password)) {
			if (newPass.equals(confirmPass)) {
				employee.setPassword(newPass);
				EmpRepos.save(employee);
				return new ModelAndView("redirect:/logout");
			}
		}
		model.addAttribute("employee", currentEmployee.getCurrentEmployee());
		model.addAttribute("error", "Please make sure you have entered your passwords correctly!");
		return new ModelAndView("security", "", model);
	}

	// User home page
	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String Home(Model model) {
		if (currentEmployee.getCurrentEmployee() == null) {
			return "login";
		}
		model.addAttribute("overAllocated", overAllocated());
		model.addAttribute("chartPoints", getEmployeeInfo(currentEmployee.getCurrentEmployee().getId()));
		model.addAttribute("employeeTasks", getCurrentEmployeeTasks(currentEmployee.getCurrentEmployee().getId()));
		model.addAttribute("overLappingTasks", CheckAllEmployeeTasks());
		return "home";
	}

	// Registration page
	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public String showRegForm(Model model) {
		EmployeeDTO employeeDTO = new EmployeeDTO();
		model.addAttribute("employee", employeeDTO);
		return "register";
	}

	// Registration page logic
	@RequestMapping(value = "/registration", method = RequestMethod.POST)
	public String create(@RequestParam("name") String name, @RequestParam("email") String email,
			@RequestParam("password") String password, @RequestParam("confirm") String confirm,
			@RequestParam("code") String code, Model model) {
		if (!(code.equals("22Md"))) {
			EmployeeDTO employeeDTO = new EmployeeDTO();
			model.addAttribute("employee", employeeDTO);
			model.addAttribute("error", "Incorrect registration code, account not created!");
			return "register";
		}
		for (Employee e : EmpRepos.findAll()) {
			if (e.getEmail().equalsIgnoreCase(email)) {
				EmployeeDTO employeeDTO = new EmployeeDTO();
				model.addAttribute("employee", employeeDTO);
				model.addAttribute("error", "Email is already taken!");
				return "register";
			}
			if (!password.equals(confirm)) {
				EmployeeDTO employeeDTO = new EmployeeDTO();
				model.addAttribute("employee", employeeDTO);
				model.addAttribute("error", "Password must match confirm password!");
				return "register";
			}
			if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
				EmployeeDTO employeeDTO = new EmployeeDTO();
				model.addAttribute("employee", employeeDTO);
				model.addAttribute("error", "All fields must be filled out!");
				return "register";
			}
		}
		EmpRepos.save(new Employee(email, name, password, false));
		return "registrationSuccess";
	}
	// End of login, logout and registration methods

	// Employee allocation
	public ArrayList<Employee> overAllocated() {
		ArrayList<Employee> overAllocated = new ArrayList<Employee>();
		if (!(GSetRepos.findAll().isEmpty())) {
			int days = Integer.parseInt(GSetRepos.findAll().get(0).getValue());
			if (!(EmpRepos.findAll().isEmpty())) {
				for (Employee e : EmpRepos.findAll()) {
					int employeeDays = 0;
					if (!(e.getTasks().isEmpty())) {
						for (Task t : e.getTasks()) {
							if (!(t.isCompleted())) {
								String month = t.getMinStartDate();
								String[] dateParts = month.split("-");
								month = dateParts[1];
								int monthInt = Integer.parseInt(month);
								int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
								if (monthInt == currentMonth + 1) {
									employeeDays = employeeDays + t.getDuration();
								}
							}
						}
						if (employeeDays > days) {
							overAllocated.add(e);
						}
					}
				}
			}
		}
		return overAllocated;
	}

	// Returns list of data points to be used as bar chart info
	public ArrayList<DataPoints> getEmployeeInfo(Long id) {
		ArrayList<DataPoints> dataPoints = new ArrayList<DataPoints>();
		if (!(GSetRepos.findAll().isEmpty())) {
			int monthDays = Integer.parseInt(GSetRepos.findAll().get(0).getValue());
			int empDays = 0;
			Employee employee = EmpRepos.findByid(id);
			if (!employee.getTasks().isEmpty()) {
				for (Task t : employee.getTasks()) {
					if (!t.isCompleted()) {
						String month = t.getMinStartDate();
						String[] dateParts = month.split("-");
						month = dateParts[1];
						int monthInt = Integer.parseInt(month);
						int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
						if (monthInt == currentMonth + 1) {
							DataPoints dataPoint = new DataPoints();
							dataPoint.setY(t.getDuration());
							dataPoint.setLabel(t.getName());
							dataPoints.add(dataPoint);
							empDays = empDays + t.getDuration();
						}
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

	// In progress, check for overlapping tasks for employees
	public ArrayList<Employee> CheckAllEmployeeTasks() {
		ArrayList<Employee> employees = new ArrayList<Employee>();
		for (Employee employee : EmpRepos.findAll()) {
			if (!(employee.getTasks().isEmpty())) {
				ArrayList<Task> notCompletedTasks = new ArrayList<Task>();
				for (Task notCompletedTask : employee.getTasks()) {
					if (!(notCompletedTask.isCompleted())) {
						notCompletedTasks.add(notCompletedTask);
					}
				}
				
				outerloop:
				for (Task task : notCompletedTasks) {
					ArrayList<Task> employeeTasks = new ArrayList<Task>();
					employeeTasks.addAll(notCompletedTasks);

					String[] startDate = task.getMinStartDate().split("-");

					int endYear = Integer.parseInt(startDate[0]);
					int endMonth = Integer.parseInt(startDate[1]);
					int endDay = Integer.parseInt(startDate[2]);
					endDay = endDay + task.getDuration();
					if (endMonth == 9 || endMonth == 4 || endMonth == 6 || endMonth == 11) {
						if (endDay > 30) {
							endDay = endDay - 30;
						}
						if (endMonth > 12) {
							endMonth = endMonth - 12;
							endYear = endYear + 1;
						}
					}
					else if (endMonth == 2) {
						if (endDay > 28) {
							endDay = endDay - 28;
						}
						if (endMonth > 12) {
							endMonth = endMonth - 12;
							endYear = endYear + 1;
						}
					}
					else if (endMonth == 1 || endMonth == 3 || endMonth == 5 || endMonth == 7 || endMonth == 8
							|| endMonth == 10 || endMonth == 12) {
						if (endDay > 31) {
							endDay = endDay - 31;
						}
						if (endMonth > 12) {
							endMonth = endMonth - 12;
							endYear = endYear + 1;
						}
					}

					int startYear = Integer.parseInt(startDate[0]);
					int startMonth = Integer.parseInt(startDate[1]);
					int startDay = Integer.parseInt(startDate[2]);

					employeeTasks.remove(task);
					for (Task t : employeeTasks) {
						String[] tStartDate = t.getMinStartDate().split("-");

						int tEndYear = Integer.parseInt(tStartDate[0]);
						int tEndMonth = Integer.parseInt(tStartDate[1]);
						int tEndDay = Integer.parseInt(tStartDate[2]);

						tEndDay = tEndDay + t.getDuration();

						if (tEndMonth == 9 || tEndMonth == 4 || tEndMonth == 6 || tEndMonth == 11) {
							if (tEndDay > 30) {
								tEndDay = tEndDay - 30;
							}
							if (tEndMonth > 12) {
								tEndMonth = tEndMonth - 12;
								tEndYear = tEndYear + 1;
							}
						}
						if (tEndMonth == 2) {
							if (tEndDay > 28) {
								tEndDay = tEndDay - 28;
							}
							if (tEndMonth > 12) {
								tEndMonth = tEndMonth - 12;
								tEndYear = tEndYear + 1;
							}
						}
						if (tEndMonth == 1 || tEndMonth == 3 || tEndMonth == 5 || tEndMonth == 7 || tEndMonth == 8
								|| tEndMonth == 10 || tEndMonth == 12) {
							if (tEndDay > 31) {
								tEndDay = tEndDay - 31;
							}
							if (tEndMonth > 12) {
								tEndMonth = tEndMonth - 12;
								tEndYear = tEndYear + 1;
							}
						}

						int tStartYear = Integer.parseInt(tStartDate[0]);
						int tStartMonth = Integer.parseInt(tStartDate[1]);
						int tStartDay = Integer.parseInt(tStartDate[2]);

						if (tEndYear == startYear && tEndMonth == endMonth) {

							if (tEndMonth != startMonth) {
								if (tEndMonth != tStartMonth) {
									for (int i = 1; i < tEndDay; i++) {
										if (tStartDay < i && tEndDay > i) {
											employees.add(employee);
											break outerloop;
										}
									}
								} else {
									for (int i = startDay; i < endDay; i++) {
										if (tStartDay < i && tEndDay > i) {
											employees.add(employee);
											break outerloop;
										}
									}
								}

							} else {
								if (tEndMonth != tStartMonth) {
									if (startDay < tEndDay) {
										employees.add(employee);
										break outerloop;
									}
								} else {
									for (int i = startDay; i < endDay; i++) {
										if (tStartDay < i && tEndDay > i) {
											employees.add(employee);
											break outerloop;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return employees;
	}
}
