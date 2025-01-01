package com.javacademy.new_york_times.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Aspect
@Slf4j
public class LoggingAspect {
    @Pointcut("execution(* com.javacademy.new_york_times..*(..))")
    public void findAll() {
    }

    @Before("findAll()")
    public void loggingBefore(JoinPoint joinPoint) {
        log.info("Вызов в методе {}, аргументы: {}", joinPoint.getSignature(), Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "findAll()", returning = "result")
    public void loggingAfter(JoinPoint joinPoint, Object result) {
        log.info("После вызова метода: {} результат: {}", joinPoint.getSignature(),
                result == null ? "void метод" : result.toString());
    }

    @AfterThrowing(pointcut = "findAll()", throwing = "ex")
    public void loggingAfterThrowing(JoinPoint joinPoint, Exception ex) {
        log.info("Выброс исключения: {} - в методе: {}", ex.toString(), joinPoint.getSignature());
    }
}
