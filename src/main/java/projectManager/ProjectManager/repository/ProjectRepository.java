package projectManager.ProjectManager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import projectManager.ProjectManager.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long>{
	Project findByid(Long id);
	Project findByName(String name);
}
