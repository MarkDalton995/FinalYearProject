package projectManager.ProjectManager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import projectManager.ProjectManager.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long>{
	Task findByid(Long id);
}
