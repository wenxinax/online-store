package com.example.onlinestore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Valid
public class PageRequest {
    @Min(value = 1)
    private int pageNum = 1;

    @Min(value = 1)
    @Max(value = 100, message = "error.page.size.max")
    private int pageSize = 10;

}