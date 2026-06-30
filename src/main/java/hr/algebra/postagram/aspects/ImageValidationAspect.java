package hr.algebra.postagram.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Aspect
@Component
public class ImageValidationAspect {

    private static final Long MAX_SIZE = 5 * 1024 * 1024L;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Before("execution(* hr.algebra.postagram.controllers..*(..))")
    public void validateImageUploads(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (arg instanceof MultipartFile file) {
                if (file.isEmpty()) {
                    throw new IllegalArgumentException("Image file is empty");
                }

                if (file.getSize() > MAX_SIZE) {
                    throw new IllegalArgumentException("Image too large (max 5MB)");
                }

                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
                    throw new IllegalArgumentException("Invalid image type: " + contentType);
                }
            }
        }
    }
}