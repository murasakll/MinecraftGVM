package br.com.mura.betaminecraftmod.world;
import java.security.MessageDigest;
import java.util.*;
final class AdventureTerrainChecks {
 static void run() throws Exception {
  String[] reference={"0 0 0 27c6cec67996e01c0609b2f5928aa0361c291fb349c948be4a4ef45d0984b33a",
"0 1 -1 181b2d65e2c2bffd3f3baefb7787a83d4319d789ba67e6ec7724dcfce402b30e",
"0 -7 12 5afc7f9a2614a4a3eb999e1de1149e249328447ff0de22321f0a2f8ba6dbeea8",
"0 100 100 5680f04fd594412a993750cf7e6d98af6c15dccd1735dce96f92ba56388ac742",
"123456789 0 0 e5af0f2890d384e599e192c74cec5f24e335182b72ed6a66af7f2b7c1f42ec07",
"123456789 1 -1 f55500490ad1d301b9f9c6fa0974a9e6f982c38754dbe70d806117c56c32c432",
"123456789 -7 12 35bbf20c9a6561f3cc9271dfb2fa3c0361fdff5b458c91975a36567d5e5d8c26",
"123456789 100 100 57d115be4681b68bbc93aec9812664cc91e7a4e821084c956410c8432feaddc8",
"-987654321 0 0 54f06d737024887287a71b6f1336dba58369849d7c02c3cb8fd7d212c0ebab38",
"-987654321 1 -1 5c0b8d93ae1e8031c1a5aa38944c93e738bb46ad1ed86b6d91b09348c83c4206",
"-987654321 -7 12 ed0082ad3f4c199067bbf1826c14c971393f1cf21a498b4bdcfbefcd54b24474",
"-987654321 100 100 ece8cffb6330e5155e90482ce22f8e22e2a383ed154d98772b69724fe846d604"};
  for(String sample:reference){
   var row=sample.split(" ");var sampler=new TerrainBackends(Long.parseLong(row[0])).sampler(5);
   int x=Integer.parseInt(row[1]),z=Integer.parseInt(row[2]);byte[] first=sampler.generate(x,z);
   if(!HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(first)).equals(row[3]))throw new AssertionError("Beta 1.8.1 reference mismatch");
   sampler.generate(z+1,x-1);
   if(!Arrays.equals(first,sampler.generate(x,z)))throw new AssertionError("Beta depends on generation order");
  }
  if(!br.com.mura.betaminecraftmod.progression.EraCatalog.get(5).questsReady())throw new AssertionError("Adventure campaign quest catalog missing");
  System.out.println("Beta 1.8.1: 12 original terrain references and generation order passed.");
 }
}
