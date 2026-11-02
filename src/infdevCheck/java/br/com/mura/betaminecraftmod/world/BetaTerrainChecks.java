package br.com.mura.betaminecraftmod.world;
import java.security.MessageDigest;
import java.util.*;
final class BetaTerrainChecks {
 static void run() throws Exception {
  String[] reference={"0 0 0 f3012aa4577f527dabe501675e98a292e5eadba8ed4611ea1a23286a6951ae61",
"0 1 -1 0198d19bf900419b8c33ef74915e95bc03559118929883ca725f99586ad6d8d2",
"0 -7 12 e8bb9d4ed15c32f9e130ab9007762d295f4f09ba7341055e322f6c56a8fc956b",
"0 100 100 fbb265de56798d36196c70f1720b5d812a3a7809fa112d75717f55ae2fa46adc",
"123456789 0 0 4766df3fd80cc59d7c76b98d5c84977e53eef3685204ba57ee9a094937af5836",
"123456789 1 -1 097e49c7a641858969d4e1787ab7b86be00ee3e9d054605507657b386cd197a0",
"123456789 -7 12 11a4dee67d1c0b37ad122e81a5387913deb81251d62cba36082824a3f1b95b72",
"123456789 100 100 b5e2bdb3f2e963e6db9ce221409a24d8529dd22d102497c73c47b97118c6f6be",
"-987654321 0 0 c952eec5ca8551eaa4a867255dc9b3490f3651e17ee320e524079198b00df052",
"-987654321 1 -1 ce843eea2308e3e4351918bd1b790de62a8b84bbe4d395a09bbdae9aadf89a6f",
"-987654321 -7 12 fd8e8782009f55ea81a1a27c3bcdfe9c8d2d3c663d91870e6d0285f178a81341",
"-987654321 100 100 d5be621143edc815f3b477479f10e55bd2fec9a406a941e0889a5b0f1e334d22"};
  for(String sample:reference){
   var row=sample.split(" ");var sampler=new TerrainBackends(Long.parseLong(row[0])).sampler(4);
   int x=Integer.parseInt(row[1]),z=Integer.parseInt(row[2]);byte[] first=sampler.generate(x,z);
   if(!HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(first)).equals(row[3]))throw new AssertionError("Beta 1.7.3 reference mismatch");
   sampler.generate(z+1,x-1);
   if(!Arrays.equals(first,sampler.generate(x,z)))throw new AssertionError("Beta depends on generation order");
  }
  if(!br.com.mura.betaminecraftmod.progression.EraCatalog.get(4).questsReady())throw new AssertionError("Beta campaign quest catalog missing");
  System.out.println("Beta 1.7.3: 12 original terrain references and generation order passed.");
 }
}
