package hr.algebra.postagram.services;

import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.PostContext;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.models.dtos.PostForm;
import hr.algebra.postagram.models.dtos.PostSearchForm;
import hr.algebra.postagram.models.events.PostEvent;
import hr.algebra.postagram.models.events.UserPostUpdate;
import hr.algebra.postagram.models.specifications.PostSpecification;
import hr.algebra.postagram.repositories.PostRepo;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@SessionScope
public class PostService extends GeneralCrudService<Post, PostRepo>{
    private final Counter postCounter;
    private final Set<Long> seenPosts = new HashSet<>();
    private final PostRepo postRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationEventPublisher publisher;
    private final ImageStorageRouter imageStorageRouter;
    private final HashtagService hashtagService;

    public PostService(PostRepo repository, PostRepo postRepo, ApplicationEventPublisher eventPublisher, MeterRegistry registry, ApplicationEventPublisher publisher, ImageStorageRouter imageStorageRouter, HashtagService hashtagService) {
        super(repository);
        this.postCounter = registry.counter("post-counter");
        this.postRepo = postRepo;
        this.eventPublisher = eventPublisher;
        this.publisher = publisher;
        this.imageStorageRouter = imageStorageRouter;
        this.hashtagService = hashtagService;
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

    @Override
    public Post save(Post post) {
        Post save = super.save(post);
        eventPublisher.publishEvent(new PostEvent(post));
        postCounter.increment();
        return save;
    }


    @SneakyThrows
    public void updateWithPipeline(PostForm postForm){
        Optional<Post> byId = findById(postForm.getId());
        if(byId.isPresent()){
            Post post = byId.get();

            PostContext ctx = new PostContext();
            ctx.setPost(post);
            ctx.setForm(postForm);

            Stream.<Function<PostContext, PostContext>>of(
                            this::storeImage,
                            this::updatePost,
                            this::savePost,
                            this::publishEvent
                    )
                    .reduce(Function.identity(), Function::andThen)
                    .apply(ctx);
        }
    }

    private PostContext storeImage(PostContext context) {
        if (context.getForm().getImage() != null) {
            String store = null;
            try {
                store = imageStorageRouter.storeImage(
                        context.getForm().getImage().getBytes(),
                        context.getForm().getImage().getContentType()
                );
            } catch (IOException e) {
                // ignore failed
            }

            context.getPost().setStorageType(imageStorageRouter.getStorageType().name());
            imageStorageRouter.deleteImage(context.getPost());
            context.setImageId(store);
        }
        return context;
    }

    private PostContext updatePost(PostContext context) {
        context.getPost().setImageId(context.getImageId());
        context.getPost().updateImageData(context.getForm().getImage());
        context.getPost().setDescription(context.getForm().getDescription());

        context.getPost().setHashtags(
                context.getForm().getHashtags().stream()
                        .map(x -> hashtagService.findByNameOrCreate(x, context.getPost().getUser()))
                        .collect(Collectors.toSet())
        );

        return context;
    }

    @Transactional
    protected PostContext savePost(PostContext context) {
        save(context.getPost());
        return context;
    }

    private PostContext publishEvent(PostContext context) {
        publisher.publishEvent(new UserPostUpdate(context.getPost()));
        return context;
    }
}
