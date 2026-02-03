package dev.thewarrior.SkyBlock.Pages.Utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConfirmDialog extends InteractiveCustomUIPage<ConfirmDialogData> {
    private final Message dialogMessage;
    private final String dialogItemId;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public ConfirmDialog(
            @Nonnull PlayerRef playerRef,
            Message dialogMessage,
            String dialogItemId,
            Runnable onConfirm,
            @Nullable Runnable onCancel
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, ConfirmDialogData.CODEC);

        this.dialogMessage = dialogMessage;
        this.dialogItemId = dialogItemId;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    public void build(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl UICommandBuilder commandBuilder,
            @NonNullDecl UIEventBuilder eventBuilder,
            @NonNullDecl Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/Utils/ConfirmDialog.ui");

        // Set message
        commandBuilder.set("#DialogMessage.TextSpans", dialogMessage);

        // Set item if not null
        if (dialogItemId != null && !dialogItemId.isEmpty()) {
            commandBuilder.set("#DialogItem.ItemId", dialogItemId);
            commandBuilder.set("#ItemGroup.Visible", true);
        } else {
            commandBuilder.set("#ItemGroup.Visible", false);
        }

        // Bind events
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ConfirmButton",
                EventData.of("Action", "Confirm"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CancelButton",
                EventData.of("Action", "Cancel"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                EventData.of("Action", "Close"),
                false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ConfirmDialogData data) {
        switch (data.action) {
            case "Confirm" -> {
                if (onConfirm != null) {
                    onConfirm.run();
                }
                onClose(ref, store);
            }
            case "Cancel" -> {
                if (onCancel != null) {
                    onCancel.run();
                } else {
                    onClose(ref, store);
                }
            }
            case "Close" -> onClose(ref, store);
        }
    }

    private void onClose(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) return;

        player.getPageManager().setPage(ref, store, Page.None);
    }
}
