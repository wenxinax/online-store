package com.example.onlinestore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@Valid
public class PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -8445831075808655383L;
    @Min(value = 1)
    private int pageNum = 1;

    @Min(value = 1)
    @Max(value = 100)
    private int pageSize = 10;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        PageRequest that = (PageRequest) o;
        return pageNum == that.pageNum && pageSize == that.pageSize;
    }

    @Override
    public int hashCode() {
        int result = pageNum;
        result = 31 * result + pageSize;
        return result;
    }

    @Override
    public String toString() {
        return "PageRequest{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                '}';
    }
}