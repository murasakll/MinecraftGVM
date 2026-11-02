package br.com.mura.betaminecraftmod.world;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.*;
/** Reference hashes obtained independently from original Alpha 1.2.6, SHA1 a68c817afd6c05c253ba5462287c2c19bbb57935. */
final class HalloweenTerrainChecks {
 static void run() throws Exception {
 String[] climate={"0 0 0 93697a6f4614c3715a9e6f9daf067bc8485c6da0b9a0abe65d60d2aabf1f6701",
"0 16 -16 9f112532f0b42eaee2d0da75bc69fb8d31f08e85c8f5e5c16c93c0a5cc6916b2",
"0 -112 192 e1b9aedeca5e3dac00c6ee4e87abcb53829fa330a87158a44ad729fe98e8bf52",
"0 1600 1600 a7c9f6c8d310c23b4db6ba7aef4ce5835c3b3af9cc5e4d512b1f44ce4b99dfa4",
"0 -16000 -16000 d26c0240e77d6f8d578adceb6902bca28ea80ba58e828a55409797b524d9134e",
"0 1536 0 28edfd02a16a182d8774cbc49baaff529f2f80086aac1cb3b187d7d9cfa49b01",
"123456789 0 0 b90752545181c2423117ed2ed4e2582c0f6c828f4568942e77210683fa619533",
"123456789 16 -16 910a5760138b39d04a21e2aa4d8c135292ea15a212808fa50ffecedf07bf9261",
"123456789 -112 192 b51f0fe1564875e6bbd19e3c20be9829d24a0e3f74d1b5518ead5145fa5f1e04",
"123456789 1600 1600 d5da304baf19106b96b96cd060e91d222b0e9ba8cf8e6b544601395f968491ea",
"123456789 -16000 -16000 11eb499714b6cf8a4fd258e303331b779633aeb05daaa48e002961a59e161698",
"123456789 1536 0 5e9faeb38a486a75c92709027c3d6b5e63a5803b426b084f48646ceb2e123d2f",
"-987654321 0 0 c1d67d148e6c3042fafce666dfd809d1db046b3fd5aaeced52a9875e0939dd0f",
"-987654321 16 -16 31b8ecea4361cf4d933be6d9656fce600c94d9cb884afb21158adfabf3478033",
"-987654321 -112 192 e525723d28ca3febec82770877c3dfc77af7e51ed793d838529f4a82a9922890",
"-987654321 1600 1600 512f4353ca24bb81545ae9841287c275eb5d3b6d655f7dbaa6eb77f8cca2c5cd",
"-987654321 -16000 -16000 7c85fbc4cdbcdebd4b6e98dbfb2131ce1643ca5634943a7520269d1bd30c876c",
"-987654321 1536 0 4ff0893938e7f0119c82e09a368d15df4a0ea5447018e9f68c1950014d7c653c"};
 String[] terrain={"0 0 0 d33bcd254a873d4c4cbbcd88247608b13e74519a8e023cc45da0c85fd7877d17",
"0 1 -1 07d94c3d6edba61e0cc89553e4bc477bd364fe931395532ba311609ed7e63242",
"0 -7 12 51a7e81d239a9f8f21c1a75ba7adee18ba49f932138517d1f78cea6d26f87c79",
"0 100 100 736075dbd29d248b53bcf72944c0fc447b27f3ad9139049ba7a628881dfdb5e4",
"123456789 0 0 041cc4cc4d79c7da3f04d767565aee7dd715a5d4e93ba61399108f86daa81c58",
"123456789 1 -1 2bb0360242d9d0b9674e5d1e8146237a1808f495ff2da8bd72cf5024c6913bf6",
"123456789 -7 12 0695fab4bbb5f46d2c8d541ba0cd907e66e7dc5dea89220dc7d33b2d727a3690",
"123456789 100 100 c2205f9ea0e5db72213811f980be085b1ef534d982753aab043f76db9cdf7590",
"-987654321 0 0 9635ac922b71f04bba97ac3810c3d636c19b85d653e7326e1b78bdce67fe17d5",
"-987654321 1 -1 6831c3bf9972e773d631132102d7f06a105a3dd512d0a5a90d2d5161501abc0c",
"-987654321 -7 12 3f7bdcb45fad164401ecca358e1849efef64c9b0e7ac8525b6da3c6577e72690",
"-987654321 100 100 44d0d4260750c267b40affa6c34cb8d525d5b214860786c93465d771481a30bf"};
 for(String sample:climate){
  var row=sample.split(" ");var sampler=new HalloweenClimate(Long.parseLong(row[0]));
  var grid=sampler.sample(Integer.parseInt(row[1]),Integer.parseInt(row[2]),16,16);
  var bytes=ByteBuffer.allocate(4096);
  for(int i=0;i<256;i++)bytes.putDouble(grid.temperature()[i]).putDouble(grid.rainfall()[i]);
  hash(bytes.array(),row[3]);
 }
 for(String sample:terrain){
  var row=sample.split(" ");var registry=new TerrainBackends(Long.parseLong(row[0]));
  int x=Integer.parseInt(row[1]),z=Integer.parseInt(row[2]);byte[] first=registry.sampler(3).generate(x,z);hash(first,row[3]);
  registry.sampler(3).generate(z+1,x-1);
  if(!Arrays.equals(first,registry.sampler(3).generate(x,z)))throw new AssertionError("Halloween depends on chunk order");
 }
 var registry=new TerrainBackends(0);
 if(!br.com.mura.betaminecraftmod.progression.EraCatalog.get(3).questsReady())throw new AssertionError("Halloween campaign quest catalog missing");
 for(int pending=6;pending<TerrainBackends.all().size();pending++){
  try{registry.sampler(pending);throw new AssertionError("Pending terrain fell back");}catch(IllegalStateException expected){}
 }
 System.out.println("Halloween base: 12 original terrain hashes, 18 climate hashes, generation order and pending isolation passed.");
 }
 private static void hash(byte[] data,String expected)throws Exception{
  if(!HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)).equals(expected))throw new AssertionError("Alpha 1.2.6 reference mismatch");
 }
}

