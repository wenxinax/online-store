package com.example.onlinestore.handler;

import com.example.onlinestore.dto.Response;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.MessageFormat;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Response<String> handleException(Exception e) {
        logger.error("Internal server error", e);
        return Response.fail("INTERNAL ERROR");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<String> handleException(MethodArgumentNotValidException e) {
        logger.error("Invalid request", e);
        BindingResult exceptions = e.getBindingResult();
        if (exceptions.hasErrors()) {
            List<ObjectError> errors = exceptions.getAllErrors();

            // 选第一个参数错误，进行返回
            if (CollectionUtils.isNotEmpty(errors)) {
                FieldError fieldError = (FieldError) errors.get(0);
                return Response.fail(MessageFormat.format("Parameter:{0}, error:{1}", fieldError.getField(), fieldError.getDefaultMessage()));
            }

        }
        return Response.fail("Invalid request");
    }

}
