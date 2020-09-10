package mssc.beer.service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mssc.beer.config.JmsConfig;
import mssc.beer.service.AllocationService;
import mssc.model.event.DeallocateOrderRequest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeallocateResponseListener
{
    private final AllocationService allocationService;

    @JmsListener(destination = JmsConfig.DEALLOCATE_ORDER_QUEUE)
    public void listen(DeallocateOrderRequest deallocateOrderResult)
    {
        allocationService.deallocateOrder(deallocateOrderResult.getBeerOrderDto());
    }
}
