package mssc.beer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mssc.beer.domain.BeerInventory;
import mssc.beer.repositories.BeerInventoryRepository;
import mssc.model.BeerOrderDto;
import mssc.model.BeerOrderLineDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationServiceImpl implements AllocationService
{
    private final BeerInventoryRepository beerInventoryRepository;

    @Override
    public Boolean allocateOrder(BeerOrderDto beerOrderDto)
    {
        log.debug("Allocating orderId: " + beerOrderDto.getId());

        AtomicInteger totalOrdered = new AtomicInteger();
        AtomicInteger totalAllocated = new AtomicInteger();

        beerOrderDto.getBeerOrderLines()
                .forEach(beerOrderLineDto ->
                {
                    if(((beerOrderLineDto.getOrderQuantity() != null ? beerOrderLineDto.getOrderQuantity() : 0)
                        - (beerOrderLineDto.getQuantityAllocated() != null ? beerOrderLineDto.getQuantityAllocated() : 0)) > 0)
                    {
                        allocateBeerOrderLine(beerOrderLineDto);
                    }

                    totalOrdered.set(totalOrdered.get() + beerOrderLineDto.getOrderQuantity());
                    totalAllocated.set(totalAllocated.get() + (beerOrderLineDto.getQuantityAllocated() != null
                            ? beerOrderLineDto.getQuantityAllocated() : 0));
                });
        return totalOrdered.get() == totalAllocated.get();
    }

    private void allocateBeerOrderLine(BeerOrderLineDto beerOrderLineDto)
    {
        List<BeerInventory> beerInventoryList = beerInventoryRepository.findAllByUpc(beerOrderLineDto.getUpc());
        beerInventoryList
                .forEach(beerInventory ->
                {
                    int inventory = (beerInventory.getQuantityOnHand() == null ? 0 : beerInventory.getQuantityOnHand());
                    int orderQuantity = (beerOrderLineDto.getOrderQuantity() == null ? 0 : beerOrderLineDto.getOrderQuantity());
                    int allocatedQuantity = (beerOrderLineDto.getQuantityAllocated() == null ? 0 : beerOrderLineDto.getQuantityAllocated());
                    int quantityToAllocate = orderQuantity - allocatedQuantity;

                    if (inventory >= quantityToAllocate)
                    {
                        inventory = inventory - quantityToAllocate;
                        beerOrderLineDto.setQuantityAllocated(orderQuantity);
                        beerInventory.setQuantityOnHand(inventory);
                        beerInventoryRepository.save(beerInventory);
                    }

                    if (inventory > 0)
                    {
                        beerOrderLineDto.setOrderQuantity(allocatedQuantity + inventory);
                        beerInventory.setQuantityOnHand(0);

                    }

                    if (beerInventory.getQuantityOnHand() == 0)
                    {
                        beerInventoryRepository.delete(beerInventory);
                    }
                });
    }

    @Override
    public void deallocateOrder(BeerOrderDto beerOrderDto)
    {
        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto ->
        {
            BeerInventory beerInventory = BeerInventory.builder()
                    .beerId(beerOrderLineDto.getBeerId())
                    .upc(beerOrderLineDto.getUpc())
                    .quantityOnHand(beerOrderLineDto.getQuantityAllocated())
                    .build();

            BeerInventory savedBeerInventory = beerInventoryRepository.save(beerInventory);

            log.debug("Saved Inventory for upc: " + savedBeerInventory.getUpc() + " inventory id: " + savedBeerInventory.getId());
        });
    }
}
