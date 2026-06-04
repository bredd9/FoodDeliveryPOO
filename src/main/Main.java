package main;

import services.OrderService;
import services.RestaurantService;
import services.UserService;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();
        RestaurantService restaurantService = new RestaurantService();
        OrderService orderService = new OrderService();

        // launch the interactive terminal menu
        new Meniu(userService, restaurantService, orderService).start();
    }
}
