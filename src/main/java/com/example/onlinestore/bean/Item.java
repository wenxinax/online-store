package com.example.onlinestore.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@Setter
@Getter
public class Item implements Serializable {
    @Serial
    private static final long serialVersionUID = 8328093958488219106L;
    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private String image;
    private Long skuId;
    private String secondaryName;
    private String pingJia;
    private Map<String, Object> extraProperties;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;
        return Objects.equals(id, item.id) && Objects.equals(categoryId, item.categoryId) && Objects.equals(name, item.name) && Objects.equals(description, item.description) && Objects.equals(image, item.image) && Objects.equals(skuId, item.skuId) && Objects.equals(secondaryName, item.secondaryName) && Objects.equals(pingJia, item.pingJia) && Objects.equals(extraProperties, item.extraProperties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(categoryId);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Objects.hashCode(image);
        result = 31 * result + Objects.hashCode(skuId);
        result = 31 * result + Objects.hashCode(secondaryName);
        result = 31 * result + Objects.hashCode(pingJia);
        result = 31 * result + Objects.hashCode(extraProperties);
        return result;
    }
}
