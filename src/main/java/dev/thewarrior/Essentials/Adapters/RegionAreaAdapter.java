package dev.thewarrior.Essentials.Adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.math.vector.Vector3d;
import dev.thewarrior.Essentials.Managers.Data.Region.Data.RegionArea;

import java.io.IOException;

public class RegionAreaAdapter extends TypeAdapter<RegionArea> {
    @Override
    public void write(JsonWriter writer, RegionArea area) throws IOException {
        if (area == null || isEmpty(area)) {
            writeVector3d(writer, Vector3d.ZERO);
            return;
        }

        writer.beginObject();

        if (area.getMin() != null) {
            writer.name("min");
            writeVector3d(writer, area.getMin());
        }

        if (area.getMax() != null) {
            writer.name("max");
            writeVector3d(writer, area.getMax());
        }

        if (area.getCenter() != null) {
            writer.name("center");
            writeVector3d(writer, area.getCenter());
        }

        if (area.getRadius() != 0) {
            writer.name("radius").value(area.getRadius());
        }

        writer.endObject();
    }

    @Override
    public RegionArea read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        RegionArea area = new RegionArea();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "min":
                    area.setMin(readVector3d(reader));
                    break;
                case "max":
                    area.setMax(readVector3d(reader));
                    break;
                case "center":
                    area.setCenter(readVector3d(reader));
                    break;
                case "radius":
                    area.setRadius(reader.nextInt());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        area.recompute();
        return area;
    }

    private boolean isEmpty(RegionArea area) {
        return area.getMin() == null
            && area.getMax() == null
            && area.getCenter() == null
            && area.getRadius() == 0;
    }

    private void writeVector3d(JsonWriter writer, Vector3d vec) throws IOException {
        writer.beginObject();
        writer.name("x").value(vec.getX());
        writer.name("y").value(vec.getY());
        writer.name("z").value(vec.getZ());
        writer.endObject();
    }

    private Vector3d readVector3d(JsonReader reader) throws IOException {
        double x = 0, y = 0, z = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "x":
                    x = reader.nextDouble();
                    break;
                case "y":
                    y = reader.nextDouble();
                    break;
                case "z":
                    z = reader.nextDouble();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new Vector3d(x, y, z);
    }
}

