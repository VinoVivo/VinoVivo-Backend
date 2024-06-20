package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.cart.CartDTO;
import com.mscommerce.models.DTO.cart.CartDTORequest;
import com.mscommerce.models.DTO.cart.CartDTOUpdate;

import java.util.List;

public interface ICartService {

    List<CartDTO> adminGetAllCarts();

    List<CartDTO> getAllCarts();

    CartDTO adminGetCartById(Integer cartId) throws ResourceNotFoundException;

    CartDTO adminCreateCart(CartDTO cartDTO) throws BadRequestException, ResourceNotFoundException;

    CartDTO createCart(CartDTORequest cartDTORequest) throws BadRequestException, ResourceNotFoundException;

    CartDTO adminUpdateCart(CartDTO cartDTO) throws BadRequestException, ResourceNotFoundException;

    CartDTO updateCart(CartDTOUpdate cartDTOUpdate) throws BadRequestException, ResourceNotFoundException;

    void adminDeleteCart(Integer cartId) throws ResourceNotFoundException;

    void deleteCart(Integer cartId) throws ResourceNotFoundException;

    void cleanCart();
}
