package projectManager.ProjectManager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import projectManager.ProjectManager.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>{
	Comment findByid(Long id);
}
