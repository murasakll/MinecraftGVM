package br.com.mura.betaminecraftmod.progression;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public final class ReleaseEnchantingNames {
    private static final String[] WORDS=load();
    private ReleaseEnchantingNames(){}
    private static String[] load(){
        try(var stream=ReleaseEnchantingNames.class.getResourceAsStream("/assets/betaminecraftmod/enchanting_words_1_0.txt")) {
            if(stream==null)throw new IllegalStateException("Missing original enchanting words");
            return new String(stream.readAllBytes(),StandardCharsets.UTF_8).trim().split(" ");
        }catch(IOException e){throw new IllegalStateException(e);}
    }
    public static String next(Random random){
        int count=3+random.nextInt(2);var result=new StringBuilder();
        for(int i=0;i<count;i++){if(i>0)result.append(' ');result.append(WORDS[random.nextInt(WORDS.length)]);}
        return result.toString();
    }
}
