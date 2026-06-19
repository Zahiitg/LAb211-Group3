package controller;

import model.FlashSaleEvent;
import repository.FlashSaleEventRepository;
import java.util.List;

public class FlashSaleController {
    private final FlashSaleEventRepository eventRepo = new FlashSaleEventRepository("data/flash_events.csv");

    public List<FlashSaleEvent> getAllEvents() {
        return eventRepo.getAll();
    }
}
