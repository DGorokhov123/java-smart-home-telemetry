package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.dal.Booking;
import ru.yandex.practicum.dal.BookingRepository;
import ru.yandex.practicum.dal.WarehouseProduct;
import ru.yandex.practicum.dal.WarehouseProductRepository;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.exception.warehouse.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.warehouse.NotFoundBookingException;
import ru.yandex.practicum.exception.warehouse.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.exception.warehouse.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.logging.Logging;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final TransactionTemplate transactionTemplate;
    private final WarehouseProductRepository warehouseProductRepository;
    private final BookingRepository bookingRepository;
    private final AddressService addressService;

    // Добавить новый товар на склад.
    @Logging
    @Transactional(readOnly = false)
    public String createProduct(NewProductInWarehouseRequest request) {
        if (warehouseProductRepository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException("Product already exists: id = " + request.getProductId());
        }
        WarehouseProduct newProduct = WarehouseMapper.toNewEntity(request);
        warehouseProductRepository.save(newProduct);
        return "true";
    }

    // Предварительно проверить что количество товаров на складе достаточно для данной корзины продуктов.
    @Logging
    @Transactional(readOnly = true)
    public BookedProductsDto checkShoppingCart(ShoppingCartDto shoppingCartDto) {
        return checkProducts(shoppingCartDto.getProducts());
    }

    // Принять товар на склад. (изменить кол-во товара)
    @Logging
    @Transactional(readOnly = false)
    public String changeQuantity(AddProductToWarehouseRequest request) {
        WarehouseProduct product = warehouseProductRepository.findById(request.getProductId()).orElseThrow(
                () -> new NoSpecifiedProductInWarehouseException("Product is not found: id = " + request.getProductId())
        );
        product.setQuantity(product.getQuantity() + request.getQuantity());
        warehouseProductRepository.save(product);
        return "true";
    }

    // Предоставить адрес склада для расчёта доставки.
    @Logging
    public AddressDto getAddress() {
        return addressService.getAddress();
    }

    // переводит Booking в статус ON_DELIVERY
    @Logging
    @Transactional(readOnly = false)
    public String sendToDelivery(ShippedToDeliveryRequest request) {
        Booking booking = bookingRepository.findByOrderId(request.getOrderId()).orElseThrow(
                () -> new NotFoundBookingException("Not found active booking for order " + request.getOrderId())
        );
        booking.setDeliveryId(request.getDeliveryId());
        booking.setBookingState(BookingState.ON_DELIVERY);
        bookingRepository.save(booking);
        return "true";
    }

    // возвращает товар обратно на склад
    // не используется нигде так как имеет неудобную сигнатуру
    // вместо него сделан метод rollbackBooking(String orderId), который откатывает бронирование по orderId
    @Logging
    @Transactional(readOnly = false)
    public String returnProducts(Map<String, Long> productMap) {
        List<WarehouseProduct> products = warehouseProductRepository.findAllById(productMap.keySet());
        for (WarehouseProduct product : products) {
            Long newQuantity = product.getQuantity() + productMap.get(product.getProductId());
            product.setQuantity(newQuantity);
        }
        warehouseProductRepository.saveAll(products);
        return "true";
    }

    // Бронирование товаров для заказа. уменьшает остатки на складе.
    // вызывается сервисом order при создании заказа автоматически
    @Logging
    @Transactional(readOnly = false)
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        Optional<Booking> optionalBooking = bookingRepository.findByOrderId(request.getOrderId());
        if (optionalBooking.isPresent()) return optionalBooking.get().toBookedProductsDto();     // idempotency

        BookedProductsDto bookedProductsDto = checkProducts(request.getProducts());

        Booking booking = new Booking();
        booking.setOrderId(request.getOrderId());
        booking.setDeliveryWeight(bookedProductsDto.getDeliveryWeight());
        booking.setDeliveryVolume(bookedProductsDto.getDeliveryVolume());
        booking.setFragile(bookedProductsDto.getFragile());
        booking.setProducts(request.getProducts());
        booking.setBookingState(BookingState.ACTIVE);
        booking.setCreatedAt(Instant.now());
        bookingRepository.save(booking);

        List<WarehouseProduct> products = warehouseProductRepository.findAllById(request.getProducts().keySet());
        for (WarehouseProduct product : products) {
            Long newQuantity = product.getQuantity() - request.getProducts().get(product.getProductId());
            product.setQuantity(newQuantity);
        }
        warehouseProductRepository.saveAll(products);
        return bookedProductsDto;
    }

    // Откатить бронирование товаров
    // вызывается из сервиса order при отмене заказа
    @Logging
    @Transactional(readOnly = false)
    public String rollbackBooking(String orderId) {
        Optional<Booking> optionalBooking = bookingRepository.findByOrderId(orderId);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            returnProducts(booking.getProducts());
            bookingRepository.delete(booking);
        }
        return "true";
    }

    // списание товаров со склада
    // вызывается из сервиса order при завершении заказа после проверки службой контроля
    // переводит Booking в финальный статус COMPLETED, что означает что товары ушли и никогда не вернутся
    @Transactional(readOnly = false)
    public String writeOffBookedProducts(String orderId) {
        Booking booking = bookingRepository.findByOrderId(orderId).orElseThrow(
                () -> new NotFoundBookingException("Not found booking for order " + orderId)
        );
        booking.setBookingState(BookingState.COMPLETED);
        bookingRepository.save(booking);
        return "true";
    }

    // PRIVATE METHODS

    private BookedProductsDto checkProducts(Map<String, Long> productMap) {
        List<WarehouseProduct> warehouseProducts = warehouseProductRepository.findAllById(productMap.keySet());

        String lackMsg = "";
        String notEnoughMsg = "";
        boolean lackOfProducts = warehouseProducts.size() < productMap.size();
        boolean notEnoughProducts = warehouseProducts.stream()
                .anyMatch(p -> p.getQuantity() < productMap.get(p.getProductId()));
        if (lackOfProducts) {
            Set<String> lackProductIds = new HashSet<>(productMap.keySet());
            for (WarehouseProduct wp : warehouseProducts) lackProductIds.remove(wp.getProductId());
            lackMsg = lackProductIds.stream()
                    .map(id -> id + "; ")
                    .reduce(new StringBuilder("Lack of products - "), StringBuilder::append, StringBuilder::append)
                    .toString();
        }
        if (notEnoughProducts) {
            notEnoughMsg = warehouseProducts.stream()
                    .filter(p -> p.getQuantity() < productMap.get(p.getProductId()))
                    .map(p -> p.getProductId() + ": " + productMap.get(p.getProductId()) + " in order, " + p.getQuantity() + " in stock; ")
                    .reduce(new StringBuilder("Not enough products - "), StringBuilder::append, StringBuilder::append)
                    .toString();
        }
        if (lackOfProducts || notEnoughProducts) {
            throw new ProductInShoppingCartLowQuantityInWarehouseException(lackMsg + notEnoughMsg);
        }

        BigDecimal deliveryWeight = warehouseProducts.stream()
                .map(WarehouseProduct::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.UP);

        BigDecimal deliveryVolume = warehouseProducts.stream()
                .map(
                        p -> BigDecimal.valueOf(productMap.get(p.getProductId()))
                                .multiply(p.getWidth())
                                .multiply(p.getHeight())
                                .multiply(p.getDepth())
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.UP);

        Boolean fragile = warehouseProducts.stream()
                .map(WarehouseProduct::getFragile)
                .anyMatch(Boolean.TRUE::equals);

        BookedProductsDto bookedProductsDto = new BookedProductsDto();
        bookedProductsDto.setDeliveryWeight(deliveryWeight);
        bookedProductsDto.setDeliveryVolume(deliveryVolume);
        bookedProductsDto.setFragile(fragile);
        return bookedProductsDto;
    }

}
