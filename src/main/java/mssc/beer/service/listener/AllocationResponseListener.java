package mssc.beer.service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mssc.beer.config.JmsConfig;
import mssc.beer.service.AllocationService;
import mssc.model.event.AllocateOrderRequest;
import mssc.model.event.AllocateOrderResult;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationResponseListener
{
    private final AllocationService allocationService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateOrderRequest allocateOrderRequest)
    {
        AllocateOrderResult.AllocateOrderResultBuilder builder = AllocateOrderResult.builder();
        builder.beerOrderDto(allocateOrderRequest.getBeerOrderDto());

        try
        {
            Boolean allocationResult = allocationService.allocateOrder(allocateOrderRequest.getBeerOrderDto());
            builder.pendingInventory(!allocationResult);
        }
        catch (Exception e)
        {
            log.debug("Allocation failed for orderId: " + allocateOrderRequest.getBeerOrderDto().getId());
            builder.allocationError(true);
        }

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESULT_QUEUE, builder.build());
    }
}
