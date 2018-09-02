package projectManager.ProjectManager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import projectManager.ProjectManager.model.GlobalSettings;

public interface GlobalSettingsRepository extends JpaRepository<GlobalSettings, Long>{
	GlobalSettings findByid(Long id);
}
