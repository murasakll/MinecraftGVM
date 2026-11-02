package br.com.mura.betaminecraftmod.world;

import java.util.*;
import java.security.MessageDigest;
import com.seedfinding.mccore.version.MCVersion;

/** Reference hashes are from the official 1.7.10 carvers over a solid-stone workspace. */
final class ReleaseCarverChecks {
    static void run() throws Exception {
        String[] hashes={
        "a1e15b44d602ad1c721864feaf0b5963addf5dcc9ace2600a0dd3e12f6a1b4ff",
        "f5c3a0386850482fe241478976f9c6227c7f45e5ff789942c03eac8f4384dda3",
        "b9d0256f8504b8c76672726d5908e6acf51b6060b873d0ca760e98157ce55410",
        "23d6c9d25f198e0d35949787103e0c60374824b67dbf6b1eee0b4fe1a27c781c",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "f2b15715abbf648e688a8d07e0fdce847216297f77fdb6c850ff8f5060b1c406",
        "12e52766b4c76007c4eed8eaa40449e7c91274319d1f89fb000daeda075b91e8",
        "78e9c2838192937534ef07582ebb851720256b50e21b15527161539cc0cdd304",
        "5fcf6a5ee4e16608b339bd0d37daa770dc676f465fd6203dc894269e24c456f7",
        "e0b85dc28d4bc9e8c23d1f345400d555745e1db2b2ff52316bce42c9b083fa41",
        "e6740d31d5b4e6579f63f54a6723547bd1dd1fb2e99f23ce6a1013c4eb24f42b",
        "bbb5da53612a7a0ed8e9719f554c38539fa5425376117098f130df114e57f6f4",
        "a1e15b44d602ad1c721864feaf0b5963addf5dcc9ace2600a0dd3e12f6a1b4ff",
        "f5c3a0386850482fe241478976f9c6227c7f45e5ff789942c03eac8f4384dda3",
        "b9d0256f8504b8c76672726d5908e6acf51b6060b873d0ca760e98157ce55410",
        "23d6c9d25f198e0d35949787103e0c60374824b67dbf6b1eee0b4fe1a27c781c",
        "da13db4191c11f12880a9acb92d758adee8eba90107bddb935de02f5ab084ad2",
        "a07258d8e76762ef05ca96a4b17c9ce8de7a4212a6da006a5f0e4303ebf018f9",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "08b22f502e8ceca5be45143b1ae52c4f66168dfd400ed83c46e770d8c26ddd1b",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "6eeb71b8c06d4a4269170832c2aee5c084e888dead54950d8539f1d996f5e47b",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "bc4b1e81455d206a51694b7885a35b91e8b7837de10f0d1a5261ed58d7c5da30",
        "4eff61df628938d522b94a8f3f3e1526b54a84784a07fac70324db08024e0d76",
        "5906efeac56c38b577f9b3c23b6313a9825ae72361e237a99aa56d7118e48653",
        "b939c85e2e9f16f295754c4dccde446592ef2007e9bd65894126ad77cfb572d4",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "6eeb71b8c06d4a4269170832c2aee5c084e888dead54950d8539f1d996f5e47b",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        "916b144867c340614f515c7b0e5415c74832d899c05264ded2a277a6e81d81ff",
        };
        int at=0;
        for(int kind=0;kind<2;kind++)for(long seed:new long[]{0,123456789,-987654321,Long.MIN_VALUE,Long.MAX_VALUE})for(int[] p:new int[][]{{0,0},{1,-1},{-7,12},{100,100}}){
            byte[] blocks=new byte[65536];Arrays.fill(blocks,(byte)1);
            if(kind==0)new AdventureCaves(seed,256,15,7,248).carve(blocks,p[0],p[1]);
            else new AdventureRavines(seed,256,248).carve(blocks,p[0],p[1]);
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(blocks));
            if(!hash.equals(hashes[at++]))throw new AssertionError("Release carver reference mismatch");
        }
        short[] blocks=new short[65536];
        for(int i=0;i<blocks.length;i++)if(i%256==0)blocks[i]=1;else if(i%541==0)blocks[i]=2;
        var input=new TerrainVolume(0,256,List.of("minecraft:stone","minecraft:bedrock","minecraft:diamond_ore"),blocks);
        var carved=ReleaseCarvers.carve(input,0,MCVersion.v1_7_10,0,0);
        int changed=0;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<256;y++){
            String before=input.block(x,y,z),after=carved.block(x,y,z);
            if(!before.equals("minecraft:stone")&&!before.equals(after))throw new AssertionError("Carver destroys protected material");
            if(!before.equals(after))changed++;
        }
        if(changed==0)throw new AssertionError("Carvers not applied to volume");
        if(ReleaseCarvers.carve(input,0,MCVersion.v1_17,0,0)!=input)throw new AssertionError("Old carvers applied to 1.17");
        System.out.println("RELEASE_CARVERS SUCCESS: 40 original 1.7.10 stone-workspace snapshots, palette preservation and version isolation");
    }
}
