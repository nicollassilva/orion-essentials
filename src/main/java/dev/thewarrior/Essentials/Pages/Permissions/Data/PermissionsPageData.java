package dev.thewarrior.Essentials.Pages.Permissions.Data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class PermissionsPageData {
    public static final BuilderCodec<PermissionsPageData> CODEC = BuilderCodec.builder(PermissionsPageData.class, PermissionsPageData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                    (d, v) -> d.action = v,
                    (d) -> d.action).add()
            .append(new KeyedCodec<>("@SearchValue", Codec.STRING),
                    (d, v) -> d.searchValue = v,
                    (d) -> d.searchValue).add()
            .append(new KeyedCodec<>("@PermissionNodeName", Codec.STRING),
                    (d, v) -> d.permissionNodeName = v,
                    (d) -> d.permissionNodeName).add()
            .append(new KeyedCodec<>("Target", Codec.STRING),
                    (d, v) -> d.target = v,
                    (d) -> d.target).add()
            .append(new KeyedCodec<>("@PermissionName", Codec.STRING),
                    (d, v) -> d.name = v,
                    (d) -> d.name).add()
            .append(new KeyedCodec<>("@PermissionPrefix", Codec.STRING),
                    (d, v) -> d.prefix = v,
                    (d) -> d.prefix).add()
            .append(new KeyedCodec<>("@PermissionSuffix", Codec.STRING),
                    (d, v) -> d.suffix = v,
                    (d) -> d.suffix).add()
            .append(new KeyedCodec<>("@PermissionPriority", Codec.INTEGER),
                    (d, v) -> d.priority = v,
                    (d) -> d.priority).add()
            .build();

    public String action;
    public String target;
    public String searchValue;

    // Used when adding/removing permission nodes
    public String permissionNodeName;

    // Used when updating permission
    public String name;
    public String prefix;
    public String suffix;
    public int priority;
}
