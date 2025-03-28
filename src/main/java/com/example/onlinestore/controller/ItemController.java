package com.example.onlinestore.controller;

import com.example.onlinestore.bean.Item;
import com.example.onlinestore.dto.Response;
import com.example.onlinestore.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 创建商品
     */
    @PostMapping
    public Response<Long> createItem(@RequestBody Item item, @RequestHeader("X-User-Id") String userId) {
        itemService.addItem(userId, item);
        return Response.success(item.getId());
    }


    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Response<Void> updateItem(@PathVariable("id") Long id, @RequestBody Item item) {
        item.setId(id);
        itemService.updateItem(item);
        return Response.success();
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Response<Void> deleteItem(@PathVariable("id") Long id) {
        itemService.deleteItem(id);
        return Response.success();
    }

}
