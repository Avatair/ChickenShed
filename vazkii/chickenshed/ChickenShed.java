package vazkii.chickenshed;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = "ChickenShed", name = "Chicken Shed")
public class ChickenShed {
	
	public static final boolean MASTER_DISABLE = false, // Master switch to disable the mod
								CHICKS_DROP_FEATHERS = true, // Do baby chickens drop feathers?
								TIMED_WITH_EGG = false, // Is the feather drop timed with making an egg?
								MODIFY_DEATH_DROPS = true; // Modify feather drops on death?
	
	// Average time between each feather drop, if the drops
	// aren't timed with the eggs
	public static final int AVG_TIME_FOR_EACH_FEATHER = 32000;
	
	public static final byte FEATHER_DROP_MIN = 1, // Min and max feather drops for when the chicken
	    	 				 FEATHER_DROP_MAX = 3, // is killed, only applies if that feature is enabled
	    	 				 
	    	 			     // Percentage chance to drop a feather when an egg is dropped,
	    	 				 // obviosly only applies if that feature is enabled.
	    	 				 EGG_TIMED_DROP_CHANCE = 100; 
	
	@Instance("ChickenShed")
	public static ChickenShed instance; // Instance of the mod, only one of this mod exists
	
	@Init
	public void init(FMLInitializationEvent event) {
		if(!MASTER_DISABLE) // Check if the entire mod is enabled, if so register the forge hooks
			MinecraftForge.EVENT_BUS.register(instance);
	}
	
	
	@ForgeSubscribe
	@SuppressWarnings("unused")
	public void onLivingUpdate(LivingUpdateEvent event) {
		if(event.entity.worldObj.isRemote) 
			return; // Break the method if the world is a client
		
		if(event.entity instanceof EntityChicken) {
			EntityChicken chicken = (EntityChicken)event.entity;

			if(chicken.isChild() && !CHICKS_DROP_FEATHERS)
				return; // If the chicken is a baby chicken and that feature is disable, breaks the method
			
			if(!TIMED_WITH_EGG) { // If the drops are supposed to be when it's not timed with the egg
				if(chicken.worldObj.rand.nextInt(AVG_TIME_FOR_EACH_FEATHER) == 0) // Psuedorandom method of timing the feather drops
					chicken.dropItem(Item.feather.itemID, 1);
			} else { // Otherwise...
				if(chicken.timeUntilNextEgg == 0 && chicken.worldObj.rand.nextInt(100) < EGG_TIMED_DROP_CHANCE)
					chicken.dropItem(Item.feather.itemID, 1);
			}
		}
	}
	
	@ForgeSubscribe
	@SuppressWarnings("unused")
	public void onLivingDrops(LivingDropsEvent event) {
		if(event.entity.worldObj.isRemote)
			return; // Break the method if the world is a client
		
		if(event.entity instanceof EntityChicken && MODIFY_DEATH_DROPS) {
			EntityChicken chicken = (EntityChicken)event.entity;
			
			if(chicken.isChild() && !CHICKS_DROP_FEATHERS)
				return; // If the chicken is a baby chicken and that feature is disable, breaks the method
			
			boolean setFeather = false; // Do the drops have a feather?
			for(EntityItem item : event.drops) {
				if(item != null) {
					// The watchable object 10 is the itemstack of the item entity
					ItemStack originalStack = item.getDataWatcher().getWatchableObjectItemStack(10);
					ItemStack stack = originalStack.copy();
					if(stack != null && stack.itemID == Item.feather.itemID) {
						stack.stackSize = MathHelper.getRandomIntegerInRange(item.worldObj.rand, FEATHER_DROP_MIN, FEATHER_DROP_MIN);
						item.getDataWatcher().updateObject(10, stack); // Update the object with the new stack
						setFeather = true; // A feather was found
					}
				}
			}
			
			if(!setFeather && FEATHER_DROP_MIN > 0) { // If a feather wasn't found, it adds one, if the mininum isn't 0 already that is
				EntityItem item = new EntityItem(chicken.worldObj, chicken.posX, chicken.posY, chicken.posZ);
				int stackSize = MathHelper.getRandomIntegerInRange(item.worldObj.rand, FEATHER_DROP_MIN, FEATHER_DROP_MIN);
				ItemStack stack = new ItemStack(Item.feather.itemID, stackSize, 0);
				item.getDataWatcher().updateObject(10, stack);
				event.drops.add(item);
			}
		}
	}
}
