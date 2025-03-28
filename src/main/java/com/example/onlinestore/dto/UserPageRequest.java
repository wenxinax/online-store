package com.example.onlinestore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserPageRequest {
    @Min(value = 1, message = "error.page.number.min")
    private int pageNum = 1;

    @Min(value = 1, message = "error.page.size.min")
    @Max(value = 100, message = "error.page.size.max")
    private int pageSize = 10;

}