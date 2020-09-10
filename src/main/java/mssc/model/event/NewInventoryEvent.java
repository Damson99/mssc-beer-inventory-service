package mssc.model.event;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NewInventoryEvent extends BeerEvent
{
    public NewInventoryEvent(BeerDto beerDto)
    {
        super(beerDto);
    }
}
