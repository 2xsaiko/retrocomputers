package therealfarfetchd.retrocomputers.common.util.ext

import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack

fun ItemConvertible.makeStack(count: Int = 1) = ItemStack(this, count)