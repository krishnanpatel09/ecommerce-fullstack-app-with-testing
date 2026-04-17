package service;

import com.TDD.Ecom.dto.OrderDto;
import com.TDD.Ecom.dto.OrderItemDto;
import com.TDD.Ecom.service.OrderService;

import java.util.ArrayList;
import java.util.List;

public class TestOrderService extends OrderService {
    public TestOrderService() {
        super(null, null, null, null); // Bypass real dependencies
    }

    @Override
    public OrderDto createOrder(List<OrderItemDto> orderItems) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(1L);
        orderDto.setStatus("PENDING");
        orderDto.setTotalAmount(100.0);
        orderDto.setOrderItems(new ArrayList<>());
        return orderDto;
    }
}
