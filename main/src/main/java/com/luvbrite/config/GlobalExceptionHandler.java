package com.luvbrite.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
	
    
    @ExceptionHandler
    public String handleException(HttpServletRequest request, Exception ex){
        System.out.println("GlobalExceptionHandler - ");
        ex.printStackTrace();
        
        return "generic-error";
    }
}