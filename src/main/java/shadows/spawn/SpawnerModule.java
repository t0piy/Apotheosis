package shadows.spawn;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisInit;
import shadows.Apotheosis.ApotheosisPreInit;
import shadows.ApotheosisObjects;

public class SpawnerModule {

	public static final Logger LOG = LogManager.getLogger("Apotheosis : Spawner");

	public static Configuration config;
	public static int spawnerSilkLevel = 1;

	@SubscribeEvent
	public void preInit(ApotheosisPreInit e) {
		config = new Configuration(new File(Apotheosis.configDir, "spawner.cfg"));
		if (Apotheosis.enableSpawner) {
			TileEntity.register("mob_spawner", TileSpawnerExt.class);
		}
	}

	@SubscribeEvent
	public void init(ApotheosisInit e) {
		spawnerSilkLevel = config.getInt("Spawner Silk Level", "general", 1, -1, 127, "The level of silk touch needed to harvest a spawner.  Set to -1 to disable, 0 to always drop.  The enchantment module can increase the max level of silk touch.");
		SpawnerModifiers.init(config);
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		Apotheosis.registerOverrideBlock(e.getRegistry(), new BlockSpawnerExt(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		e.getRegistry().register(new EnchantmentCapturing().setRegistryName(Apotheosis.MODID, "capturing"));
	}

	@SubscribeEvent
	public void handleCapturing(LivingDropsEvent e) {
		Entity killer = e.getSource().getTrueSource();
		if (killer instanceof EntityLivingBase) {
			int level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.CAPTURING, ((EntityLivingBase) killer).getHeldItemMainhand());
			if (e.getEntityLiving().world.rand.nextFloat() < level / 250F) {
				ItemStack egg = new ItemStack(Items.SPAWN_EGG);
				EntityLivingBase killed = e.getEntityLiving();
				ItemMonsterPlacer.applyEntityIdToItemStack(egg, EntityList.getKey(killed));
				e.getDrops().add(new EntityItem(killed.world, killed.posX, killed.posY, killed.posZ, egg));
			}
		}
	}

	@SubscribeEvent
	public void handleUseItem(RightClickBlock e) {
		if (e.getWorld().getBlockState(e.getPos()).getBlock() == Blocks.MOB_SPAWNER) {
			ItemStack s = e.getItemStack();
			for (SpawnerModifier sm : SpawnerModifiers.MODIFIERS)
				if (sm.matches(s)) e.setUseBlock(Result.ALLOW);
		}
	}

}
