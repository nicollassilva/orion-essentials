package dev.thewarrior.MiniGames.Gaming.Systems;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.MiniGames.Gaming.GameManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.List;

public class PlayerDeathSystem extends DeathSystems.OnDeathSystem {
    private final GameManager gameManager;

    private final List<ItemStack> EMPTY_LOOT = new ObjectArrayList<>();

    public PlayerDeathSystem(final GameManager gameManager) {
        super();

        this.gameManager = gameManager;
    }

    @Override
    public void onComponentAdded(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl DeathComponent deathComponent,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer
    ) {
        // this means the player not is in a game managed by us
        if(!this.gameManager.handlePlayerDeath(ref, deathComponent, store, commandBuffer)) return;

        deathComponent.setShowDeathMenu(false);
        deathComponent.setItemsLostOnDeath(EMPTY_LOOT);
        deathComponent.setItemsAmountLossPercentage(0d);
        deathComponent.setItemsDurabilityLossPercentage(0d);
        deathComponent.setDeathMessage(ColorUtil.colorize("&cVocê morreu e perdeu a partida!\n &f Obrigado por jogar na Orion Network."));
        deathComponent.setItemsLossMode(DeathConfig.ItemsLossMode.NONE);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
