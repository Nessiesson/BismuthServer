package si.bismuth.mixins;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.XpOrbEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(XpOrbEntity.class)
public abstract class XpOrbEntityMixin extends Entity implements si.bismuth.utils.IXpOrbEntity {
	@Unique
	private static final double LIFETIME_CONSTANT = 1D / Math.log(16D);
	@Unique
	private IntArrayList xpValues = new IntArrayList();
	@Unique
	private int delayBeforeCombine = 25;
	@Unique
	private int maxAge;

	public XpOrbEntityMixin(World world) {
		super(world);
	}

	@Shadow
	public int pickupDelay;

	@Shadow
	private int xp;

	@Shadow
	protected abstract int damageToXp(int durability);

	@Shadow
	protected abstract int xpToDamage(int xp);

	@Inject(method = "<init>(Lnet/minecraft/world/World;DDDI)V", at = @At("RETURN"))
	private void onInit(World worldIn, double x, double y, double z, int expValue, CallbackInfo ci) {
		this.xpValues.add(expValue);
	}

	/**
	 * @author nessie
	 * @reason pretty invasive mixin so much simpler to overwrite
	 */
	@Overwrite
	public void onPlayerCollision(PlayerEntity player) {
		if (this.pickupDelay == 0 && player.xpCooldown == 0) {
			player.xpCooldown = 2;
			final ItemStack stack = EnchantmentHelper.getFirstArmorStackWithEnchantment(Enchantments.MENDING, player);
			int xp = 0;
			// hackfix to prevent OOB exception
			if (!this.xpValues.isEmpty()) {
				xp = this.xpValues.removeInt(this.xpValues.size() - 1);
			}

			if (!stack.isEmpty() && stack.isDamaged()) {
				final int i = Math.min(this.xpToDamage(xp), stack.getDamage());
				xp -= this.damageToXp(i);
				stack.setDamage(stack.getDamage() - i);
			}

			if (xp > 0) {
				player.increaseXp(xp);
			}

			if (this.xpValues.isEmpty()) {
				player.sendPickup(this, 1);
				this.remove();
			} else {
				this.world.playSound(null, this.getSourceBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F, (this.random.nextFloat() - this.random.nextFloat()) * 0.35F + 0.9F);
				this.resetAge();
			}
		}
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onOrbUpdate(CallbackInfo ci) {
		if (--delayBeforeCombine > 0) {
			this.searchAndCombine();
		}
	}

	@Unique
	private void searchAndCombine() {
		this.delayBeforeCombine = 50;
		for (XpOrbEntity orb : this.world.getEntities(XpOrbEntity.class, this.getShape().expand(0.5D))) {
			this.combineOrbs(orb);
		}
	}

	@Unique
	private void combineOrbs(XpOrbEntity orb) {
		if ((XpOrbEntity) (Object) this == orb) {
			return;
		}

		if (orb.isAlive()) {
			final si.bismuth.utils.IXpOrbEntity iorb = ((si.bismuth.utils.IXpOrbEntity) orb);
			this.xp += orb.getXp();
			this.xpValues.addAll(iorb.getXpValues());
			orb.remove();
			this.resetAge();
		}
	}

	@Unique
	private void resetAge() {
		((IXpOrbEntity) this).setOrbAge(0);
		this.maxAge = (int) (6000 * (1 + LIFETIME_CONSTANT * Math.log(this.xpValues.size())));
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 6000))
	private int getMaxAge(int value) {
		return this.maxAge;
	}

	@Inject(method = "writeEntityNbt", at = @At("RETURN"))
	private void onWriteEntityToNBT(NbtCompound compound, CallbackInfo ci) {
		compound.putIntArray("xpValues", this.xpValues.toIntArray());
		compound.putInt("maxAge", this.maxAge);
	}

	@Inject(method = "readEntityNbt", at = @At("RETURN"))
	private void readEntityFromNBT(NbtCompound compound, CallbackInfo ci) {
		this.xpValues = new IntArrayList(compound.getIntArray("xpValues"));
		this.maxAge = compound.getInt("maxAge");
	}

	@Override
	public IntArrayList getXpValues() {
		return this.xpValues;
	}
}
