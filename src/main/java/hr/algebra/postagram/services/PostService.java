package hr.algebra.postagram.services;

import hr.algebra.postagram.models.Hashtag;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.models.dtos.PostSearchForm;
import hr.algebra.postagram.repositories.PostRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.*;

@Service
@SessionScope
public class PostService extends GeneralCrudService<Post, PostRepo>{
    private final Set<Long> seenPosts = new HashSet<>();

    public PostService(PostRepo repository) {
        super(repository);
    }

    public Page<Post> findByUserPaged(User user, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return repository.findByUser(user, pageable);
    }

    public List<Post> getByHashtag(HashSet<Hashtag> hashtags) {
        return repository.findByHashtag(hashtags);
    }

    public List<Post> getRandomPosts(int limit) {
        List<Long> exclude = new ArrayList<>(seenPosts);
        List<Post> posts = repository.findRandomPostsExcluding(exclude, limit);
        posts.forEach(p -> seenPosts.add(p.getId()));
        return posts;
    }

    public List<Post> getSeenPosts() {
        return seenPosts.stream().map(x -> findById(x).orElse(null)).filter(Objects::nonNull).toList();
    }

    public void addToSeenPosts(Post post) {
        seenPosts.add(post.getId());
    }

    public List<Post> getLatest(int limit) {
        return repository.getLatest(limit);
    }

    public List<Post> getPostsBySearchForm(PostSearchForm form) {
        if (!form.getHashtags().isEmpty()){
            return repository.getBySearchParams(form.getRangeStart(), form.getRangeEnd(), form.getAuthorName(), form.getAspectRatio(), form.getHashtags());
        }
        return repository.getBySearchParams(form.getRangeStart(), form.getRangeEnd(), form.getAuthorName(), form.getAspectRatio());
    }
}
