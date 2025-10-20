package ru.yandex.practicum;

import net.datafaker.Faker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.client.*;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.store.ProductCategory;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.ProductState;
import ru.yandex.practicum.dto.store.QuantityState;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.DimensionDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Выполнять только при всех 9 запущенных сервисах")
@SpringBootTest
public class OrderTest {

    private final Faker faker = new Faker();
    private final Random random = new Random();
    private final String username = faker.credentials().username();

    @Autowired
    private ShoppingStoreClient shoppingStoreClient;
    @Autowired
    private ShoppingCartClient shoppingCartClient;
    @Autowired
    private WarehouseClient warehouseClient;
    @Autowired
    private OrderClient orderClient;
    @Autowired
    private DeliveryClient deliveryClient;
    @Autowired
    private PaymentClient paymentClient;

    @Test
    public void createOrderAndDeliverSuccessfully() throws InterruptedException {
        ShoppingCartDto shoppingCartDto = createShoppingCartData();

        System.out.print("\n\nСоздаем заказ: \n");
        OrderDto orderDto = createNewOrderByShoppingCartDto(shoppingCartDto);
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.NEW, orderDto.getState());

        Thread.sleep(500);

        System.out.print("\nЖдем немного ... и проверяем заказ:\n");
        orderDto = orderClient.getOrdersByUsername(username).getFirst();
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.ON_PAYMENT, orderDto.getState());
        assertNotNull(orderDto.getDeliveryId());
        assertNotNull(orderDto.getPaymentId());

        Thread.sleep(500);

        System.out.print("\nКлиент оплачивает заказ и платежный шлюз вызывает POST /api/v1/payment/success ... ");
        paymentClient.successfulPaymentForOrder(orderDto.getOrderId());
        System.out.print("[OK]\nЖдем немного ... ");

        Thread.sleep(1000);

        System.out.print("и проверяем заказ: \n");
        orderDto = orderClient.getOrdersByUsername(username).getFirst();
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.SENT_TO_DELIVERY, orderDto.getState());
        assertNotNull(orderDto.getDeliveryId());
        assertNotNull(orderDto.getPaymentId());

        Thread.sleep(500);

        System.out.print("\nСлужба доставки забрала товары со склада и вызывает POST /api/v1/delivery/start ");
        deliveryClient.pickedProductsToDelivery(orderDto.getOrderId());
        System.out.print("[OK]\nЖдем немного ... ");

        Thread.sleep(500);

        System.out.print("и проверяем заказ: \n");
        orderDto = orderClient.getOrdersByUsername(username).getFirst();
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.ON_DELIVERY, orderDto.getState());
        assertNotNull(orderDto.getDeliveryId());
        assertNotNull(orderDto.getPaymentId());

        Thread.sleep(500);

        System.out.print("\nДоставка успешно завершена, вызывается POST /api/v1/delivery/successful ");
        deliveryClient.successfulDeliveryForOrder(orderDto.getOrderId());
        System.out.print("[OK]\nЖдем немного ... ");

        Thread.sleep(500);

        System.out.print("и проверяем заказ: \n");
        orderDto = orderClient.getOrdersByUsername(username).getFirst();
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.DELIVERED, orderDto.getState());
        assertNotNull(orderDto.getDeliveryId());
        assertNotNull(orderDto.getPaymentId());

        Thread.sleep(500);

        System.out.print("\nСлужба контроля получила подтверждение клиента, вызывает POST /api/v1/order/completed ");
        orderClient.completedOrder(orderDto.getOrderId());
        System.out.print("[OK]\nЖдем немного ... ");

        Thread.sleep(500);

        System.out.print("и проверяем заказ: \n");
        orderDto = orderClient.getOrdersByUsername(username).getFirst();
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.COMPLETED, orderDto.getState());
        assertNotNull(orderDto.getDeliveryId());
        assertNotNull(orderDto.getPaymentId());

        System.out.println("\n");
    }

    @Test
    public void createOrderAndFailDelivery() throws InterruptedException {
        ShoppingCartDto shoppingCartDto = createShoppingCartData();

        System.out.print("\n\nСоздаем заказ: \n");
        OrderDto orderDto = createNewOrderByShoppingCartDto(shoppingCartDto);
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.NEW, orderDto.getState());

        Thread.sleep(500);

        System.out.print("\nЖдем немного ... и проверяем заказ:\n");
        orderDto = orderClient.getOrdersByUsername(username).getFirst();
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.ON_PAYMENT, orderDto.getState());
        assertNotNull(orderDto.getDeliveryId());
        assertNotNull(orderDto.getPaymentId());

        Thread.sleep(500);

        System.out.print("\nКлиент оплачивает заказ и платежный шлюз вызывает POST /api/v1/payment/success ... ");
        paymentClient.successfulPaymentForOrder(orderDto.getOrderId());
        System.out.print("[OK]\nЖдем немного ... ");

        Thread.sleep(1000);

        System.out.print("и проверяем заказ: \n");
        orderDto = orderClient.getOrdersByUsername(username).getFirst();
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.SENT_TO_DELIVERY, orderDto.getState());
        assertNotNull(orderDto.getDeliveryId());
        assertNotNull(orderDto.getPaymentId());

        Thread.sleep(500);

        System.out.print("\nСлужба доставки забрала товары со склада и вызывает POST /api/v1/delivery/start ");
        deliveryClient.pickedProductsToDelivery(orderDto.getOrderId());
        System.out.print("[OK]\nЖдем немного ... ");

        Thread.sleep(500);

        System.out.print("и проверяем заказ: \n");
        orderDto = orderClient.getOrdersByUsername(username).getFirst();
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.ON_DELIVERY, orderDto.getState());
        assertNotNull(orderDto.getDeliveryId());
        assertNotNull(orderDto.getPaymentId());

        Thread.sleep(500);

        System.out.print("\nДоставка отменилась, клиент отказался, вызывается POST /api/v1/delivery/failed ");
        deliveryClient.failedDeliveryForOrder(orderDto.getOrderId());
        System.out.print("[OK]\nЖдем немного ... ");

        Thread.sleep(500);

        System.out.print("и проверяем заказ: \n");
        orderDto = orderClient.getOrdersByUsername(username).getFirst();
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.DELIVERY_FAILED, orderDto.getState());
        assertNotNull(orderDto.getDeliveryId());
        assertNotNull(orderDto.getPaymentId());

        Thread.sleep(500);

        System.out.print("\nСлужба контроля подтверждает отмену заказа, вызывает POST /api/v1/order/return ");
        orderClient.returnProductsInOrder(orderDto.getOrderId());
        System.out.print("[OK]\nЖдем немного ... ");

        Thread.sleep(500);

        System.out.print("и проверяем заказ: \n");
        orderDto = orderClient.getOrdersByUsername(username).getFirst();
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.RETURNED, orderDto.getState());
        assertNotNull(orderDto.getDeliveryId());
        assertNotNull(orderDto.getPaymentId());

        System.out.println("\n");
    }

    @Test
    public void createOrderAndFailPayment() throws InterruptedException {
        ShoppingCartDto shoppingCartDto = createShoppingCartData();

        System.out.print("\n\nСоздаем заказ: \n");
        OrderDto orderDto = createNewOrderByShoppingCartDto(shoppingCartDto);
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.NEW, orderDto.getState());

        Thread.sleep(500);

        System.out.print("\nЖдем немного ... и проверяем заказ:\n");
        orderDto = orderClient.getOrdersByUsername(username).getFirst();
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.ON_PAYMENT, orderDto.getState());
        assertNotNull(orderDto.getDeliveryId());
        assertNotNull(orderDto.getPaymentId());

        Thread.sleep(500);

        System.out.print("\nКлиент НЕ оплачивает заказ и платежный шлюз вызывает POST /api/v1/payment/failed ... ");
        paymentClient.failedPaymentForOrder(orderDto.getOrderId());
        System.out.print("[OK]\nЖдем немного ... ");

        Thread.sleep(500);

        System.out.print("и проверяем заказ: \n");
        orderDto = orderClient.getOrdersByUsername(username).getFirst();
        printOrderDtoData(orderDto);
        assertEquals(shoppingCartDto.getShoppingCartId(), orderDto.getShoppingCartId());
        assertEquals(shoppingCartDto.getProducts(), orderDto.getProducts());
        assertEquals(OrderState.PAYMENT_FAILED, orderDto.getState());
        assertNotNull(orderDto.getDeliveryId());
        assertNotNull(orderDto.getPaymentId());

        System.out.println();
    }

    // ==================== PRIVATE METHODS ========================================================================

    private ShoppingCartDto createShoppingCartData() {
        System.out.println(" ");
        System.out.println("======================================= STARTING TESTS ======================================================");
        System.out.println("username = " + username);
        System.out.println(" ");

        System.out.print("добавляем продукты в магазин: ");
        ProductDto productDto1 = addRandomProductToShoppingStore();
        System.out.print(productDto1.getProductName());
        ProductDto productDto2 = addRandomProductToShoppingStore();
        System.out.print(", " + productDto2.getProductName());
        ProductDto productDto3 = addRandomProductToShoppingStore();
        System.out.println(", " + productDto3.getProductName());

        System.out.print("проверяем продукты: ");
        ProductDto checkProductDto1 = shoppingStoreClient.getById(productDto1.getProductId());
        if (Objects.equals(checkProductDto1, productDto1)) {
            System.out.print(productDto1.getProductName() + " [OK], ");
        } else {
            System.out.print(productDto1.getProductName() + " != " + checkProductDto1.getProductName() + ", ");
        }
        ProductDto checkProductDto2 = shoppingStoreClient.getById(productDto2.getProductId());
        if (Objects.equals(checkProductDto2, productDto2)) {
            System.out.print(productDto2.getProductName() + " [OK], ");
        } else {
            System.out.print(productDto2.getProductName() + " != " + checkProductDto2.getProductName() + ", ");
        }
        ProductDto checkProductDto3 = shoppingStoreClient.getById(productDto3.getProductId());
        if (Objects.equals(checkProductDto3, productDto3)) {
            System.out.print(productDto3.getProductName() + " [OK], ");
        } else {
            System.out.print(productDto3.getProductName() + " != " + checkProductDto3.getProductName() + ", ");
        }
        System.out.println("\n");

        System.out.print("добавляем продукты на склад: ");
        System.out.print(productDto1.getProductName() + ": [" + addProductToWarehouse(productDto1.getProductId()) + "], ");
        System.out.print(productDto2.getProductName() + ": [" + addProductToWarehouse(productDto2.getProductId()) + "], ");
        System.out.print(productDto3.getProductName() + ": [" + addProductToWarehouse(productDto3.getProductId()) + "], ");
        System.out.println();

        System.out.print("увеличиваем количество продуктов: ");
        System.out.print(productDto1.getProductName() + ": [" + addProductQuantityToWarehouse(productDto1.getProductId()) + "], ");
        System.out.print(productDto2.getProductName() + ": [" + addProductQuantityToWarehouse(productDto2.getProductId()) + "], ");
        System.out.print(productDto3.getProductName() + ": [" + addProductQuantityToWarehouse(productDto3.getProductId()) + "], ");
        System.out.println("\n");

        System.out.print("создаем корзину с продуктами: ");
        Map<String, Long> productsMap = Map.of(
                productDto1.getProductId(), random.nextLong(100) + 1,
                productDto2.getProductId(), random.nextLong(50) + 1
        );
        ShoppingCartDto shoppingCartDto = shoppingCartClient.addProductsToCart(username, productsMap);
        for (Map.Entry<String, Long> entry : shoppingCartDto.getProducts().entrySet()) {
            System.out.print(entry.getKey() + ": " + entry.getValue() + " шт.; ");
            if (Objects.equals(entry.getValue(), productsMap.get(entry.getKey()))) {
                System.out.print("[OK], ");
            } else {
                System.out.print("[ERR], ");
            }
        }
        System.out.println();

        System.out.print("добавляем еще продукты в корзину: ");
        Map<String, Long> productsMap2 = Map.of(
                productDto2.getProductId(), random.nextLong(50) + 1,
                productDto3.getProductId(), random.nextLong(100) + 1
        );
        Map<String, Long> productsMap3 = Stream.concat(
                productsMap.entrySet().stream(),
                productsMap2.entrySet().stream()
        ).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                Long::sum
        ));
        shoppingCartDto = shoppingCartClient.addProductsToCart(username, productsMap2);
        for (Map.Entry<String, Long> entry : shoppingCartDto.getProducts().entrySet()) {
            System.out.print(entry.getKey() + ": " + entry.getValue() + " шт.; ");
            if (Objects.equals(entry.getValue(), productsMap3.get(entry.getKey()))) {
                System.out.print("[OK], ");
            } else {
                System.out.print("[ERR], ");
            }
        }

        System.out.print("\n\nПолучаем актуальную корзину пользователя " + username + " : ");
        shoppingCartDto = shoppingCartClient.getCartByUsername(username);
        System.out.print("[OK] id = " + shoppingCartDto.getShoppingCartId() + " :\n>> продукты: ");
        shoppingCartDto.getProducts().forEach((key, value) -> System.out.print(key + " - " + value + "шт.; "));
        return shoppingCartDto;
    }

    private OrderDto createNewOrderByShoppingCartDto(ShoppingCartDto shoppingCartDto) {
        AddressDto addressDto = AddressDto.builder()
                .country(faker.address().country())
                .city(faker.address().cityName())
                .street(faker.address().streetName())
                .house(faker.address().buildingNumber())
                .flat(String.valueOf(random.nextInt(200) + 1))
                .build();
        CreateNewOrderRequest createNewOrderRequest = CreateNewOrderRequest.builder()
                .shoppingCart(shoppingCartDto)
                .deliveryAddress(addressDto)
                .build();
        return orderClient.createNewOrder(username, createNewOrderRequest);
    }

    private String addProductQuantityToWarehouse(String productId) {
        AddProductToWarehouseRequest addProductToWarehouseRequest = AddProductToWarehouseRequest.builder()
                .productId(productId)
                .quantity(random.nextLong(500) + 100L)
                .build();
        return warehouseClient.changeQuantity(addProductToWarehouseRequest);
    }

    private String addProductToWarehouse(String productId) {
        DimensionDto dimensionDto = DimensionDto.builder()
                .width(new BigDecimal(faker.number().numberBetween(0.01, 1.99)).setScale(2, RoundingMode.UP))
                .height(new BigDecimal(faker.number().numberBetween(0.01, 1.99)).setScale(2, RoundingMode.UP))
                .depth(new BigDecimal(faker.number().numberBetween(0.01, 1.99)).setScale(2, RoundingMode.UP))
                .build();
        NewProductInWarehouseRequest newProductInWarehouseRequest = NewProductInWarehouseRequest.builder()
                .productId(productId)
                .fragile(random.nextBoolean())
                .dimension(dimensionDto)
                .weight(new BigDecimal(faker.number().numberBetween(0.1, 9.99)).setScale(2, RoundingMode.UP))
                .build();
        return warehouseClient.createProduct(newProductInWarehouseRequest);
    }

    private ProductDto addRandomProductToShoppingStore() {
        ProductDto productDto = ProductDto.builder()
                .productName(faker.commerce().productName())
                .description(faker.lorem().sentence())
                .imageSrc(faker.internet().image())
                .quantityState(QuantityState.ENOUGH)
                .productState(ProductState.ACTIVE)
                .productCategory(ProductCategory.values()[random.nextInt(ProductCategory.values().length)])
                .price(new BigDecimal(faker.commerce().price()))
                .build();
        return shoppingStoreClient.createProduct(productDto);
    }

    private void printOrderDtoData(OrderDto dto) {
        System.out.print(">> orderId = " + dto.getOrderId() + ", shoppingCartId = " + dto.getShoppingCartId());
        System.out.print(", paymentId = " + dto.getPaymentId() + ", deliveryId = " + dto.getDeliveryId());
        System.out.print("\n>> state = " + dto.getState() + ", productPrice = " + dto.getProductPrice());
        System.out.print(", deliveryPrice = " + dto.getDeliveryPrice() + ", totalPrice = " + dto.getTotalPrice());
        System.out.println(", products: " + dto.getProducts().size() + " штуки ");
    }

}
