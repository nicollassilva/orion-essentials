package dev.thewarrior.Adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.thewarrior.Managers.Data.Permission.PermissionData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.util.List;

public class PermissionDataAdapter extends TypeAdapter<PermissionData> {
    @Override
    public void write(JsonWriter writer, PermissionData data) throws IOException {
        writer.beginArray();

        for (String perm : data.getPermissions()) {
            writer.value(perm);
        }

        writer.endArray();
    }

    @Override
    public PermissionData read(JsonReader reader) throws IOException {
        final List<String> permissions = new ObjectArrayList<>();

        reader.beginArray();

        while (reader.hasNext()) {
            permissions.add(reader.nextString());
        }

        reader.endArray();

        return new PermissionData(permissions);
    }
}
