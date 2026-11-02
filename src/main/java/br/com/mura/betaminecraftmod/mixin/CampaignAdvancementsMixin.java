package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.client.QuestScreen;
import java.util.stream.StreamSupport;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AdvancementsScreen.class)
public abstract class CampaignAdvancementsMixin {
    @Redirect(method = "onAdvancementsUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementTree;roots()Ljava/lang/Iterable;"))
    private Iterable<AdvancementNode> campaign$roots(AdvancementTree tree) {
        if (!((Object)this instanceof QuestScreen)) return tree.roots();
        var player = net.minecraft.client.Minecraft.getInstance().player;
        var chapter = player == null ? null : br.com.mura.betaminecraftmod.quests.QuestChapter.get(br.com.mura.betaminecraftmod.progression.CampaignRules.era(player));
        if (chapter == null) return java.util.List.of();
        return StreamSupport.stream(tree.roots().spliterator(), false)
            .filter(node -> node.holder().id().getNamespace().equals("betaminecraftmod")
                && node.holder().id().getPath().startsWith(chapter.path() + "/"))
            .toList();
    }
}
