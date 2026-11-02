package br.com.mura.betaminecraftmod.world;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

/** Validates the checked-in audit, without requiring archived clients on the build machine. */
final class HistoricalTextureReferenceChecks {
    static void run() throws Exception {
        var root=JsonParser.parseString(Files.readString(Path.of("docs/reference/appearance_hashes.json"))).getAsJsonObject();
        var references=root.getAsJsonArray("references");
        String[] clients={"d3eb1dce5a6c86dd0d6483ba56223276dcf32c30","88c1931650b0e5be349017e124a7785a745111e9","daa4b9f192d2c260837d3b98c39432324da28e86","a68c817afd6c05c253ba5462287c2c19bbb57935","43db9b498cb67058d2e12d394e6507722e71bb45","6b562463ccc2c7ff12ff350a2b04a67b3adcd37b","b679fea27f2284836202e9365e13a82552092e5d"};
        if(references.size()!=7)throw new AssertionError("Missing historical texture references");
        Map<String,String> previous=Map.of();
        for(int era=0;era<7;era++) {
            var ref=references.get(era).getAsJsonObject();
            if(ref.get("era").getAsInt()!=era||!ref.get("client_sha1").getAsString().equals(clients[era]))throw new AssertionError("Wrong reference client "+era);
            var images=ref.getAsJsonObject("images");
            if(images.size()!=ref.get("png_count").getAsInt()||!images.has("terrain.png")||!images.has("gui/items.png"))throw new AssertionError("Incomplete texture inventory");
            for(var entry:images.entrySet()) {
                var image=entry.getValue().getAsJsonObject();
                if(image.get("width").getAsInt()<1||image.get("height").getAsInt()<1||!image.get("argb_sha256").getAsString().matches("[0-9a-f]{64}"))throw new AssertionError("Invalid image fingerprint");
            }
            var tiles=new HashMap<String,String>();
            for(var entry:ref.getAsJsonObject("atlas_tiles").entrySet())tiles.put(entry.getKey(),entry.getValue().getAsString());
            if(tiles.size()!=512)throw new AssertionError("Missing atlas cells");
            var changed=new HashSet<String>();
            for(var entry:tiles.entrySet()) {
                if(!entry.getValue().matches("[0-9a-f]{64}"))throw new AssertionError("Invalid cell fingerprint");
                if(previous.containsKey(entry.getKey())&&!previous.get(entry.getKey()).equals(entry.getValue()))changed.add(entry.getKey());
            }
            var reported=new HashSet<String>();for(var entry:ref.getAsJsonArray("changed_atlas_tiles"))if(!reported.add(entry.getAsString()))throw new AssertionError("Duplicate changed cell");
            if(!changed.equals(reported))throw new AssertionError("Incorrect per-cell texture delta");
            previous=tiles;
        }
        if(!references.get(6).getAsJsonObject().get("checkpoint").getAsString().equals("1.0"))throw new AssertionError("Release 1.0 mistaken for full 1.2.5 chapter");
        System.out.println("HISTORICAL_TEXTURE_REFERENCES SUCCESS: seven client fingerprints, PNG metadata and 3584 atlas cells");
    }
}
