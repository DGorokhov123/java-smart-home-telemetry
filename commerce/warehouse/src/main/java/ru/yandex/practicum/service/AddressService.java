package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.warehouse.AddressDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class AddressService {

    private final List<AddressDto> addresses = new ArrayList<>();
    private final Random random = new Random();

    public AddressService() {
        AddressDto addressDto1 = AddressDto.builder()
                .country("Russia")
                .city("Moscow")
                .street("Stroiteley")
                .house("25")
                .flat("12")
                .build();
        addresses.add(addressDto1);

        AddressDto addressDto2 = AddressDto.builder()
                .country("USA")
                .city("New-York")
                .street("Wall Street")
                .house("1")
                .flat("1")
                .build();
        addresses.add(addressDto2);
    }

    public AddressDto getAddress() {
        return addresses.get(random.nextInt(addresses.size()));
    }

}
