package com.example.onlinestore.handler;

import com.example.onlinestore.dto.Response;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Response<String> handleException(Exception e) {
        return Response.fail(e.getMessage());
    }
}
