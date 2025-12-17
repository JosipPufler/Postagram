package hr.algebra.postagram.services;

import hr.algebra.postagram.models.Hashtag;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.models.dtos.PostSearchForm;
import hr.algebra.postagram.models.specifications.PostSpecification;
import hr.algebra.postagram.repositories.PostRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.*;

@Service
@SessionScope
public class PostService extends GeneralCrudService<Post, PostRepo>{
    private final Set<Long> seenPosts = new HashSet<>();
    private final PostRepo postRepo;

    public PostService(PostRepo repository, PostRepo postRepo) {
        super(repository);
        this.postRepo = postRepo;
    }

    public Page<Post> findByUserPaged(User user, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return repository.findByUser(user, pageable);
    }

    public Page<Post> getLatestPaged(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return repository.getLatest(pageable);
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

    public List<Post> filterPosts(PostSearchForm form) {
        Specification<Post> spec = Specification.unrestricted();

        if(form.getAuthorName() != null && !form.getAuthorName().isEmpty()) {
            spec = spec.and(PostSpecification.isAuthorLike(form.getAuthorName()));
        }

        if(form.getAspectRatio() != null && !form.getAspectRatio().isEmpty()) {
            spec = spec.and(PostSpecification.isAspectRatioLike(form.getAspectRatio()));
        }

        if(form.getRangeStart() != null && form.getRangeEnd() != null) {
            spec = spec.and(PostSpecification.wasPostedBetween(form.getRangeStart(), form.getRangeEnd()));
        }

        List<Post> specFiltered = postRepo.findAll(spec);

        if (form.getHashtags() != null && !form.getHashtags().isEmpty()) {
            Set<Post> filtered = new HashSet<>(
                    postRepo.findPostsByHashtags(form.getHashtags().stream().map(hashtag -> hashtag.replaceAll("[^A-Za-z0-9]","")).toList(), form.getHashtags().size())
            );

            specFiltered = specFiltered.stream()
                    .filter(filtered::contains)
                    .toList();
        }

        return specFiltered;
    }
}
