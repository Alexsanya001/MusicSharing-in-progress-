package com.example.musicsharing.aspect.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionLoggingAspect.class);

    @AfterThrowing(pointcut = "within (@org.springframework.stereotype.Service *) || " +
            "within (@org.springframework.web.bind.annotation.RestController *) ||" +
            "within (@org.springframework.stereotype.Repository *)",
            throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        logger.error("Exception in method {} with message {}\n", joinPoint.getSignature(), ex.getMessage(), ex);
    }
}
