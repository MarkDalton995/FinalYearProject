package projectManager.ProjectManager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import projectManager.ProjectManager.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long>{
	Employee findByid(Long id);
	Employee findByEmail(String email);
}
