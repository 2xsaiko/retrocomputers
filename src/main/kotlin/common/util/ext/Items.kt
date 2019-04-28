package therealfarfetchd.retrocomputers.common.util.ext

import net.minecraft.item.ItemProvider
import net.minecraft.item.ItemStack

fun ItemProvider.makeStack(count: Int = 1) = ItemStack(this, count)