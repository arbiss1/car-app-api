package car.app.api.controller;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static net.logstash.logback.marker.Markers.append;

@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Pointcut that matches all repositories, services and Web REST endpoints.
     */
    @Pointcut("within(@org.springframework.stereotype.Repository *)" +
            " || within(@org.springframework.stereotype.Component *)" +
            " || within(@org.springframework.stereotype.Service *)" +
            " || within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {
    }

    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut("within(car.app.api.client..*)" +
            " || within(car.app.api.service..*)" +
            " || within(car.app.api.controller..*)")
    public void applicationPackagePointcut() {
    }

    /**
     * Pointcut that matches all repositories, services and Web REST endpoints.
     */
    @Pointcut("within(car.app.api.service..*)")
    public void springBeanPointcutService() {
    }

    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut("within(car.app.api.service..*)")
    public void applicationPackagePointcutService() {
    }

    /**
     * Advice that logs methods throwing exceptions.
     *
     * @param joinPoint join point for advice
     * @param e         exception
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        StackTraceElement relevantElement = findRelevantStackTraceElement(stackTrace);

        if (relevantElement != null) {
            log.error("Exception in {}.{}() at {}:{} with message {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    relevantElement.getFileName(),
                    relevantElement.getLineNumber(),
                    e.getMessage() != null ? e.getMessage() : "NULL");
        } else {
            log.error("Exception in {}.{}() with cause = {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getMessage() != null ? e.getMessage() : "NULL");
        }
    }

    private StackTraceElement findRelevantStackTraceElement(StackTraceElement[] stackTrace) {
        // Loop through the stack trace elements and find the first one
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().startsWith("car.app.api")) {
                return element;
            }
        }
        return null;
    }

    /**
     * Advice that logs when a method is entered and exited.
     *
     * @param joinPoint join point for advice
     * @return result
     * @throws Throwable throws IllegalArgumentException
     */
    @Around("applicationPackagePointcut() && springBeanPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        }
        try {
            Object result = joinPoint.proceed();
            if (log.isDebugEnabled()) {
                log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(), result);
            }
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            throw e;
        }
    }

    @Around("applicationPackagePointcutService() && springBeanPointcutService()")
    public Object logOutputInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        if (log.isInfoEnabled()) {
            log.info(append("action", joinPoint.getSignature().getName())
                            .and(append("step", "input"))
                            .and(append("payload", joinPoint.getArgs())),
                    "INCOMING_REQUEST_STARTED");
        }
        try {
            var startTime = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            var endtime = System.currentTimeMillis();
            if (log.isInfoEnabled()) {
                log.info(append("action", joinPoint.getSignature().getName())
                                .and(append("step", "output"))
                                .and(append("exec_time", endtime - startTime)),
                        "INCOMING_REQUEST_SUCCESS");
            }
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            throw e;
        }
    }
}
