package org.broadleafcommerce.core.web.controller.cart;

import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.exception.ItemNotFoundException;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.broadleafcommerce.core.web.order.CartState;
import org.broadleafcommerce.core.web.order.model.AddToCartItem;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BroadleafCartController extends AbstractCartController {
	
	/**
	 * Renders the cart page.
	 * 
	 * If the method was invoked via an AJAX call, it will render the "ajax/cart" template.
	 * Otherwise, it will render the "cart" template.
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @throws PricingException
	 */
	public String cart(HttpServletRequest request, HttpServletResponse response, Model model) throws PricingException {
		return ajaxRender("cart", request, model);
	}
	
	/**
	 * Takes in an item request, adds the item to the customer's current cart, and returns.
	 * 
	 * If the method was invoked via an AJAX call, it will render the "ajax/cart" template.
	 * Otherwise, it will perform a 302 redirect to "/cart"
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param itemRequest
	 * @throws IOException
	 * @throws PricingException
	 */
	public String add(HttpServletRequest request, HttpServletResponse response, Model model,
			AddToCartItem itemRequest) throws IOException, PricingException {
		Order cart = CartState.getCart(request);
		
		// If the cart is currently empty, it will be the shared, "null" cart. We must detect this
		// and provision a fresh cart for the current customer upon the first cart add
		if (cart == null || cart.equals(orderService.getNullOrder())) {
			cart = orderService.createNewCartForCustomer(CustomerState.getCustomer(request));
		}
		
		cart = orderService.addItem(cart, itemRequest, false);
		cart = orderService.save(cart,  true);
		CartState.setCart(request, cart);
		
    	return isAjaxRequest(request) ? "ajax/cart" : "redirect:/cart";
	}
	
	/**
	 * Takes in an item request and updates the quantity of that item in the cart. If the quantity
	 * was passed in as 0, it will remove the item.
	 * 
	 * If the method was invoked via an AJAX call, it will render the "ajax/cart" template.
	 * Otherwise, it will perform a 302 redirect to "/cart"
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param itemRequest
	 * @throws IOException
	 * @throws PricingException
	 * @throws ItemNotFoundException
	 */
	public String updateQuantity(HttpServletRequest request, HttpServletResponse response, Model model,
			AddToCartItem itemRequest) throws IOException, PricingException, ItemNotFoundException {
		Order cart = CartState.getCart(request);
		
		cart = orderService.updateItem(cart, itemRequest, false);
		cart = orderService.save(cart, true);
		CartState.setCart(request, cart);
		
		if (isAjaxRequest(request)) {
			Map<String, Object> extraData = new HashMap<String, Object>();
			extraData.put("productId", itemRequest.getProductId());
			extraData.put("cartItemCount", cart.getItemCount());
			model.addAttribute("blcextradata", new ObjectMapper().writeValueAsString(extraData));
			return "ajax/cart";
		} else {
			return "redirect:/cart";
		}
	}
	
	/**
	 * Takes in an item request, updates the quantity of that item in the cart, and returns
	 * 
	 * If the method was invoked via an AJAX call, it will render the "ajax/cart" template.
	 * Otherwise, it will perform a 302 redirect to "/cart"
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param nonAjaxSuccessUrl
	 * @param itemRequest
	 * @throws IOException
	 * @throws PricingException
	 * @throws ItemNotFoundException
	 */
	public String remove(HttpServletRequest request, HttpServletResponse response, Model model,
			AddToCartItem itemRequest) throws IOException, PricingException, ItemNotFoundException {
		Order cart = CartState.getCart(request);
		
		cart = orderService.removeItem(cart, itemRequest, false);
		cart = orderService.save(cart, true);
		CartState.setCart(request, cart);
		
		if (isAjaxRequest(request)) {
			Map<String, Object> extraData = new HashMap<String, Object>();
			extraData.put("cartItemCount", cart.getItemCount());
			extraData.put("productId", itemRequest.getProductId());
			model.addAttribute("blcextradata", new ObjectMapper().writeValueAsString(extraData));
			return "ajax/cart";
		} else {
			return "redirect:/cart";
		}
	}
	
	/**
	 * Cancels the current cart and redirects to the homepage
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @throws PricingException
	 */
	public String empty(HttpServletRequest request, HttpServletResponse response, Model model) throws PricingException {
		Order cart = CartState.getCart(request);
    	orderService.cancelOrder(cart);
		CartState.setCart(request, null);
		
    	return "redirect:/";
	}
	
}
