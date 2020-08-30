package mssc.model.event;

import mssc.model.BeerDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NewInventoryEvent extends BeerEvent
{
    public NewInventoryEvent(BeerDto beerDto)
    {
        super(beerDto);
    }
}
