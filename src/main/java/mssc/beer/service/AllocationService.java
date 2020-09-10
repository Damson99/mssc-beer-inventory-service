package mssc.beer.service;

import mssc.model.BeerOrderDto;

public interface AllocationService
{
    Boolean allocateOrder(BeerOrderDto beerOrderDto);

    void deallocateOrder(BeerOrderDto beerOrderDto);
}
