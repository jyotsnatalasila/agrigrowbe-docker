package com.example.agrigrowbe.controller;

import com.example.agrigrowbe.model.CartItem;
import com.example.agrigrowbe.repository.CartItemRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = {"http://const res = await axios.post(
  `http://ec2-13-62-223-118.eu-north-1.compute.amazonaws.com:1010/agrigrowbe/api/password/reset?token=${encodeURIComponent(token)}`,
  { password: newPassword }
);
:4000", "http://const res = await axios.post(
  `http://ec2-13-62-223-118.eu-north-1.compute.amazonaws.com:1010/agrigrowbe/api/password/reset?token=${encodeURIComponent(token)}`,
  { password: newPassword }
);
:8080"}) // ✅ UPDATED
public class CartController {

    private final CartItemRepository cartItemRepository;

    public CartController(CartItemRepository cartItemRepository){
        this.cartItemRepository = cartItemRepository;
    }

    @GetMapping("/{userId}")
    public List<CartItem> getCartItemsByUserId(@PathVariable UUID userId){
        return cartItemRepository.findByUserId(userId);
    }

    @PostMapping
    public CartItem addCartItem(@RequestBody CartItem cartItem){
        return cartItemRepository.save(cartItem);
    }

    @DeleteMapping("/{cartItemId}")
    public void deleteCartItem(@PathVariable UUID cartItemId){
        cartItemRepository.deleteById(cartItemId);
    }
}
