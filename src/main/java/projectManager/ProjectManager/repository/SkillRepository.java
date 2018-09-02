package projectManager.ProjectManager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import projectManager.ProjectManager.model.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long>{
	Skill findByid(Long id);
	Skill findByName(String name);
}
