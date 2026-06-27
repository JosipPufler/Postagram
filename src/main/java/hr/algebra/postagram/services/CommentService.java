package hr.algebra.postagram.services;

import hr.algebra.postagram.models.Comment;
import hr.algebra.postagram.repositories.CommentRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService extends GeneralCrudService<Comment, CommentRepo> {

    protected CommentService(CommentRepo repository) {
        super(repository);
    }

    public List<Comment> getCommentsByPostId(long postId) {
        return repository.findByPostId(postId);
    }
}
