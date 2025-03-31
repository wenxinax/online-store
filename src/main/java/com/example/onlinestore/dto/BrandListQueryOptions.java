package com.example.onlinestore.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Valid
public class BrandListQueryOptions extends PageRequest{
    @Serial
    private static final long serialVersionUID = 1406567832771578631L;

    private Integer showStatus;
    private String orderBy;
    private List<Long> brandIds;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BrandListQueryOptions that = (BrandListQueryOptions) o;
        return Objects.equals(showStatus, that.showStatus) && Objects.equals(orderBy, that.orderBy) && Objects.equals(brandIds, that.brandIds);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(showStatus);
        result = 31 * result + Objects.hashCode(orderBy);
        result = 31 * result + Objects.hashCode(brandIds);
        return result;
    }

    @Override
    public String toString() {
        return "BrandListQueryOptions{" +
                "showStatus=" + showStatus +
                ", orderBy='" + orderBy + '\'' +
                ", brandIds=" + brandIds +
                "} " + super.toString();
    }
}
