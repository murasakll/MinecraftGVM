package br.com.mura.betaminecraftmod.world;

import java.util.*;
import java.security.MessageDigest;
import java.nio.ByteBuffer;
import com.seedfinding.mccore.version.MCVersion;

/** Original 1.16.5 path and stone-workspace raster snapshots; no original classes required at build. */
final class AquaticCarverChecks {
    private static String hash(byte[] bytes)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}
    static void run()throws Exception{
        String[] expected={
            "8788fab678192dfbc78d3d1d686ba0ea319402b1a57f14c7295e0bad1df3d8f3",
            "6bfb0607c4c56c302320372751ea5d747c4d5bd940403a490492ce45d2a45985",
            "c573e2460666e815b0cad707a2dd9d36929d5a434f24b3567db175b33eebca44",
            "6583ecd8beabebf526119b43873bd7fbfddc412838ed0c8c608987ed4953fbe2",
            "42e9d268933a828ac2ee7b3cda2a65b41fb008e85a7da77519fd0bc9649a792c",
            "529b52bc8f945b57e077a5447c49c28b4a73d10223a5056c448aa00ab94f8a42",
            "966b08d4dac34dba98bcff128ef6c20edddc01044446c364d6b6c53b14f89f72",
            "c0354bad7363d3a9c15136894fdd83297ba834583babbef185b11f7c9c679db7",
            "ef3b327a5923053b5643f031c1769b1a28359a09537cc9803500209e9a91272b",
            "43f42d9c4b87a5936acc3afd3f4c278e533485605b668ea91af873d4de6a4077",
            "e34c4a8d915a6440b9d787a9258f0e135d2acb55454082560e7647bbd557b750",
            "72084bfae57d83ce9744108979de557d448e767a7db0d43248cc49e44833df18",
            "8788fab678192dfbc78d3d1d686ba0ea319402b1a57f14c7295e0bad1df3d8f3",
            "6bfb0607c4c56c302320372751ea5d747c4d5bd940403a490492ce45d2a45985",
            "c573e2460666e815b0cad707a2dd9d36929d5a434f24b3567db175b33eebca44",
            "6583ecd8beabebf526119b43873bd7fbfddc412838ed0c8c608987ed4953fbe2",
            "e5666af22e4fc7db30c08e6bf129283f5ee83b869e123ba2c214467bfe370837",
            "62b3d172a2ac26f48eedd132f089020639231df8fae943970e6fe6cc27274fa1",
            "8a8c4e0f630c6ca7cebbe2b5790b152031a406fc5f3505e47523ba736f92c21b",
            "4d165e93d20c4effd33ea0c981eba174ca07fb61a39b09c656952598316a1494",
            "20c7d89b5b2b8c23950ad4b34241b02d7691401141e18bf83e70b1dd8fe2c0ba",
            "a12d7060cd6327b6fb95410ebb562413ea7ca848fb8288de4045c405a8d30c13",
            "726094e18e081d491348ff9dfeba55a4d78c31e2fdc1b5921d18bffe9a561501",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "e2b4798b70a3beb10d68271879f222f1b0167d944ce580c73ee1021042b3e30e",
            "79297b44effd4814cfd73ed7d240a505265062b0ce4c81220f8c88c050ab139d",
            "e584451336d5635f1addb7d0199d585f88caf7977d55496a28658c8d78e7ea5f",
            "301e247301e4e3879336132fff1cdfce7ff8ee35504527dd6e0953ce186c6e0b",
            "cd1bb3b82c38a933e6a5ff2777d0ecc127d661bc13c9c60581702381ac5e7930",
            "a587041040565aef84cb256ca126eaca23c1716a89b517c32edc77c4dc0aca28",
            "fb7e478229f64a710c46a82e19b945f2574a97723db24e09cd6c1e4d8b938b15",
            "434d35e7f2ea485ae2e2a423ac5be67f906302fc1ca6bc70ff069dae7273c18e",
            "20c7d89b5b2b8c23950ad4b34241b02d7691401141e18bf83e70b1dd8fe2c0ba",
            "a12d7060cd6327b6fb95410ebb562413ea7ca848fb8288de4045c405a8d30c13",
            "726094e18e081d491348ff9dfeba55a4d78c31e2fdc1b5921d18bffe9a561501",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "73eb89ca1f53bf36fda64b6058bb7e09176fe1aa10fb67b3b2804cbf8ea53ee6",
            "27a581f771edf37fa952b540ac43bd02c1baada26f6e9f52d1133858cfb676fc",
            "cd5f98a8b7f2b1709a2ca6dde1f7a29b14a971baeae70e51557e93b15f890c0c",
            "b828ac3967cc9c4552831d0bfe0ec2e3055d5ac59573cfb6793e312f110a3525",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "dd640e2ba70cf03a7a25365d8f8b5f4ac3921d5018b8a05e90bd9e44503beb1b",
            "18002a5bc7841e9970b9a1bdade3818eb429cd52fed001bbe0d2af5a77ab10ac",
            "8f265e85d0ca379816c35a985b3b97272230c515408aa3ea96d22ad630717094",
            "acb85f4c6d5954107f23ae07a814de076386261bb2359fa8629c7b352e9d3b45",
            "dabcdf95051bff14c89d8beacddd2ad809ab8861c713134a9cccacd066075c39",
            "3893ea59c04372c65029df307e06aa9a807df0a23761c15621ef041d79b96226",
            "db6c1fc52a79f4eef4721b54c0c89ba40b0ddc0feacdbb3811583383fe8ff0a1",
            "cd14e67dedb1f5748bff1424e6523fc5e58b25bf7f65d3648716ed1cda41304c",
            "e9547fa65ca8086cd0328551e8dae22136f3c42ca8180398c00982c0abdc852d",
            "3f9ee902ea9a15c16c6ba0c97c35f6301d11906b9bc6ab8c8982ebc41d15f70f",
            "4c2c1c1bde40c8e0e2cd5995efca2707b799e0ab6758e19ea7f3489ba66d3b5c",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "dd640e2ba70cf03a7a25365d8f8b5f4ac3921d5018b8a05e90bd9e44503beb1b",
            "18002a5bc7841e9970b9a1bdade3818eb429cd52fed001bbe0d2af5a77ab10ac",
            "8f265e85d0ca379816c35a985b3b97272230c515408aa3ea96d22ad630717094",
            "4625ef9ded4d168a3c419752fbba98daaca09cab76a24f0e29b4185c37950fa4",
            "e17d17463993d18f6a67c9f18ae0d7a5d6b60c791d548bd55a2fa25b2d2181c6",
            "5f2c52bdbe390cdb6898126b5e69ad37f2173e25f134bb3ef87e25a2b700d1fc",
            "244015bba3e3395b4ea4efc9b11c82697765c11cb14977e44e2cdcf2c2b89771",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "87c7508d36832877cb72ac481d9c6aadb940ada91314d22611bba36619af5437",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "a6e4be88f23269d61aa48d566c8130e594135772162af33951fd0feaccd3f18f",
            "2157dcad5b12447e6a9eea24b1b37fdebd0646eeb9aead088d4bc79de8444f7e",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "60b710831fad74152040a544e005633466d54bf7a92ae969b1ba4ec1bfb04447",
            "f309f87a91dda6c210fdec0d018d6ca9f65675178ab57de0249cba0358b5ecce",
            "bd309d2a3355e17aa9301bad60e16a377a3c8ad36e7cf230d721493f3a334a57",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "87c7508d36832877cb72ac481d9c6aadb940ada91314d22611bba36619af5437",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "6aa953d08babf5c7ae94be425d7e146eb72bc3dce07021a0c5e045652abac21a",
            "fc36bc2f1a38703e22e912b4bdeb1b41ac9d8b5c64859798179ea6571c6ba9cf",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "0b4996b4127903375e2da7be6d93b7b5a8d498e8e58f6a2a1d1623e615a7fa89",
        };
        int at=0;
        for(boolean maskMode:new boolean[]{false,true})for(boolean canyon:new boolean[]{false,true})
        for(long seed:new long[]{0,123456789,-987654321,Long.MIN_VALUE,Long.MAX_VALUE})for(int[] target:new int[][]{{0,0},{1,-1},{-7,12},{100,100}}){
            var digest=MessageDigest.getInstance("SHA-256");var mask=new BitSet(65536);
            var paths=new AquaticCarverPaths(target[0],target[1],(s,x,y,z,r,v,w)->{
                if(maskMode)AquaticCarverMask.carve(target[0],target[1],x,y,z,r,v,w,mask,null,255,(bx,by,bz)->{});
                else digest.update(ByteBuffer.allocate(48).putLong(s).putDouble(x).putDouble(y).putDouble(z).putDouble(r).putDouble(v).array());
            });
            for(int x=target[0]-8;x<=target[0]+8;x++)for(int z=target[1]-8;z<=target[1]+8;z++){
                long salt=seed+(canyon?1:0);var r=new Random(salt);long a=r.nextLong(),b=r.nextLong();r.setSeed(x*a^z*b^salt);
                if(r.nextFloat()>(canyon?.02f:.14285715f))continue;
                if(canyon)paths.canyon(r,x,z);else paths.caves(r,x,z);
            }
            String actual=maskMode?hash(mask.toByteArray()):HexFormat.of().formatHex(digest.digest());
            if(!actual.equals(expected[at++]))throw new AssertionError("Configured carver original mismatch: mask="+maskMode+", canyon="+canyon+", seed="+seed);
        }
        var mask=new BitSet(65536);
        AquaticCarverMask.carve(0,0,8,40,8,4,3,null,mask,(x,y,z)->x==3&&y==40&&z==3,255,(x,y,z)->{});
        if(!mask.isEmpty())throw new AssertionError("Carver crossed water shell");
        AquaticCarverMask.carve(0,0,8,40,8,4,3,null,mask,null,255,(x,y,z)->{});
        if(mask.isEmpty())throw new AssertionError("Dry carver produced no cavity");
        if(AquaticCarvers.replaceable("minecraft:bedrock",false,"minecraft:air")||AquaticCarvers.replaceable("minecraft:diamond_ore",true,"minecraft:water"))throw new AssertionError("Protected materials carveable");
        if(AquaticCarvers.replaceable("minecraft:sand",false,"minecraft:water")||!AquaticCarvers.replaceable("minecraft:sand",false,"minecraft:air"))throw new AssertionError("Sand water protection missing");
        short[] cells=new short[65536];for(int i=0;i<cells.length;i++)if(i%256==0)cells[i]=1;else if(i%541==0)cells[i]=2;
        var input=new TerrainVolume(0,256,List.of("minecraft:stone","minecraft:bedrock","minecraft:diamond_ore"),cells);
        for(var version:new MCVersion[]{MCVersion.v1_13_2,MCVersion.v1_15_2,MCVersion.v1_16_5}){
            var carved=ReleaseCarvers.carve(input,0,version,1,-1);int changes=0;
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<256;y++){
                String before=input.block(x,y,z),after=carved.block(x,y,z);
                if(!before.equals("minecraft:stone")&&!before.equals(after))throw new AssertionError("Configured carver destroys protected voxel");
                if(!before.equals(after))changes++;
            }
            if(changes==0)throw new AssertionError("Configured carvers not integrated "+version);
        }
        var source=new com.seedfinding.mcbiome.source.OverworldBiomeSource(MCVersion.v1_16_5,0);
        short[] flooded=new short[65536];
        for(int i=0;i<flooded.length;i++)if(i%256==0)flooded[i]=1;else if(i%256>=63)flooded[i]=2;
        var oceanInput=new TerrainVolume(0,256,List.of("minecraft:stone","minecraft:bedrock","minecraft:water"),flooded);
        int oceans=0,submergedChanges=0;
        search:for(int cx=-16;cx<=16;cx+=4)for(int cz=-16;cz<=16;cz+=4){
            if(source.getBiomeForNoiseGen(cx*4,0,cz*4).getCategory()!=com.seedfinding.mcbiome.biome.Biome.Category.OCEAN)continue;
            var output=ReleaseCarvers.carve(oceanInput,0,MCVersion.v1_16_5,cx,cz);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=1;y<63;y++){
                String block=output.block(x,y,z);
                if(block.equals("minecraft:water"))submergedChanges++;
                if((block.equals("minecraft:magma_block")||block.equals("minecraft:obsidian"))&&y!=10)throw new AssertionError("Incorrect underwater transition height");
            }
            if(++oceans==4)break search;
        }
        if(oceans==0||submergedChanges==0)throw new AssertionError("Ocean step did not carve submerged cavities");
        System.out.println("AQUATIC_CARVERS SUCCESS: 40 original paths + 40 original stone masks, water shell, material preservation and 1.13/1.15/1.16 dispatch");
    }
}
