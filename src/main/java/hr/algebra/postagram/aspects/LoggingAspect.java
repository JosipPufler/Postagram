package hr.algebra.postagram.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(hr.algebra.postagram.controllers..*)")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        long start = System.currentTimeMillis();

        System.out.println("Entering: " + methodName);
        System.out.println("Arguments: " + Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - start;

            System.out.println("Exiting: " + methodName);
            System.out.println("Execution time: " + duration + " ms");

            return result;

        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;

            System.out.println("Exception in: " + methodName);
            System.out.println("Execution time before failure: " + duration + " ms");
            System.out.println("Error: " + ex.getMessage());

            throw ex;
        }
    }
}
