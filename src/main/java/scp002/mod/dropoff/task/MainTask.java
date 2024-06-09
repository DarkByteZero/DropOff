package scp002.mod.dropoff.task;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityFurnace;
import scp002.mod.dropoff.config.DropOffConfig;
import scp002.mod.dropoff.inventory.DropOffHandler;
import scp002.mod.dropoff.inventory.InventoryData;
import scp002.mod.dropoff.inventory.InventoryManager;
import scp002.mod.dropoff.inventory.SortingHandler;

import java.util.ArrayList;
import java.util.List;

public class MainTask implements Runnable {

    private final EntityPlayerMP player;
    private final InventoryManager inventoryManager;
    private final DropOffHandler dropOffHandler;
    private final SortingHandler sortingHandler;
    private List<InventoryData> inventoryDataList = new ArrayList<>();

    public MainTask(EntityPlayerMP player) {
        this.player = player;
        inventoryManager = new InventoryManager(player);
        dropOffHandler = new DropOffHandler(inventoryManager);
        sortingHandler = new SortingHandler(inventoryManager);
    }

    public EntityPlayerMP getPlayer() {
        return player;
    }

    public int getItemsCounter() {
        return dropOffHandler.getItemsCounter();
    }

    public List<InventoryData> getInventoryDataList() {
        return inventoryDataList;
    }

    @Override
    public void run() {
        dropOffHandler.setItemsCounter(0);

        List<InventoryData> inventoryDataList = inventoryManager.getNearbyInventories();

        for (InventoryData inventoryData : inventoryDataList) {
            IInventory inventory = inventoryData.getInventory();

            if (DropOffConfig.INSTANCE.dropOff) {
                dropOffHandler.setStartSlot(InventoryManager.Slots.FIRST);
                dropOffHandler.setEndSlot(InventoryManager.Slots.LAST);

                if (inventory instanceof TileEntityFurnace) {
                    if (inventory.getStackInSlot(InventoryManager.Slots.FIRST) == null) {
                        dropOffHandler.setStartSlot(InventoryManager.Slots.FURNACE_FUEL);
                    }

                    dropOffHandler.setEndSlot(InventoryManager.Slots.FURNACE_OUT);
                }

                dropOffHandler.dropOff(inventoryData);
            }

            if (DropOffConfig.INSTANCE.sortContainers) {
                if (sortingHandler.isSortRequired(inventoryData.getInventory())) {
                    sortingHandler.setStartSlot(InventoryManager.Slots.FIRST);
                    sortingHandler.setEndSlot(InventoryManager.Slots.LAST);

                    sortingHandler.sort(inventory);
                }
            }

            inventory.markDirty();
        }

        this.inventoryDataList = inventoryDataList;

        if (DropOffConfig.INSTANCE.sortPlayerInventory) {
            sortingHandler.setStartSlot(InventoryManager.Slots.PLAYER_INVENTORY_FIRST);
            sortingHandler.setEndSlot(InventoryManager.Slots.PLAYER_INVENTORY_LAST);

            sortingHandler.sort(player.inventory);
        }

        player.inventory.markDirty();

        player.inventoryContainer.detectAndSendChanges();
    }
}
