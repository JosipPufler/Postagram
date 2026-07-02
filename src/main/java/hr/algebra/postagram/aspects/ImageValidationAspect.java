package hr.algebra.postagram.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Aspect
@Component
public class ImageValidationAspect {

    private static final Long MAX_SIZE = 2 * 1024 * 1024L;

    private final List<Function<MultipartFile, Optional<String>>> rules = List.of(
            this::notEmpty,
            this::notTooLarge,
            this::validType
    );

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Before("execution(* hr.algebra.postagram.controllers..*(..))")
    public void validateImageUploads(JoinPoint joinPoint) {
        inspect(joinPoint.getArgs(), new HashSet<>());
    }

    private void inspect(Object obj, Set<Object> visited) {
        if (obj == null) return;

        if (!visited.add(obj)) return;

        if (obj instanceof MultipartFile file) {
            validateFile(file);
            return;
        }

        Class<?> clazz = obj.getClass();

        if (isPrimitiveLike(clazz)) return;

        if (obj instanceof Iterable<?> iterable) {
            iterable.forEach(item -> inspect(item, visited));
            return;
        }

        if (obj.getClass().isArray()) {
            Object[] array = (Object[]) obj;
            for (Object item : array) {
                inspect(item, visited);
            }
            return;
        }

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                inspect(field.get(obj), visited);
            } catch (IllegalAccessException ignored) {}
        }
    }

    private void inspect(Object[] args, Set<Object> visited) {
        for (Object arg : args) {
            inspect(arg, visited);
        }
    }

    private void validateFile(MultipartFile file) {
        rules.stream()
                .map(rule -> rule.apply(file))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .ifPresent(error -> {
                    throw new IllegalArgumentException(error);
                });
    }

    private boolean isPrimitiveLike(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz.getPackageName().startsWith("java.")
                || clazz == String.class
                || Number.class.isAssignableFrom(clazz)
                || clazz.isEnum();
    }

    private Optional<String> notEmpty(MultipartFile file) {
        return file.isEmpty()
                ? Optional.of("Image file is empty")
                : Optional.empty();
    }

    private Optional<String> notTooLarge(MultipartFile file) {
        return file.getSize() > MAX_SIZE
                ? Optional.of("Image too large (max 2MB)")
                : Optional.empty();
    }

    private Optional<String> validType(MultipartFile file) {
        String type = file.getContentType();
        return (type == null || !ALLOWED_TYPES.contains(type))
                ? Optional.of("Invalid image type: " + type)
                : Optional.empty();
    }
}