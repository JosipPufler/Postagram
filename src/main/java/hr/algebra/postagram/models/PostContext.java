package hr.algebra.postagram.models;

import hr.algebra.postagram.models.dtos.PostForm;
import lombok.Data;

@Data
public class PostContext {
    Post post;
    PostForm form;
    String imageId;
}
