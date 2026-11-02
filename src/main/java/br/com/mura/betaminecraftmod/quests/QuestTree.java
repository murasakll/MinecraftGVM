package br.com.mura.betaminecraftmod.quests;

import java.util.*;

/** Data shared by server criteria and the native advancement presentation. */
public final class QuestTree {
    public enum Fact { OPEN, MATERIAL, TOOL, STONE, TORCH, SWORD, KILLS, PLACED, SHELTER, FOOD, TRAVEL, COAL, BOW, FURNACE, REDSTONE, DIAMOND, ARROW, CIRCUIT, CHEST,
        IRON_ARMOR, NETHER_VISIT, NETHER_RETURN, NETHERRACK, SOUL_SAND, GLOWSTONE, GHAST_KILLS, NETHER_BUILD, NETHER_SHELTER, LAPIS, PISTON, EXTENDED_PISTON, BED, SKELETON_KILLS, SPIDER_KILLS,
        BREAD, WHEAT, FARMLAND, HARVEST, CRAFTED_BREAD, FENCE_GATE, ENDER_PEARL, ENDERMAN_KILLS, XP_LEVEL, DIAMOND_SWORD, BLAZE_ROD, BLAZE_KILLS, NETHER_WART, BREWING_STAND, POTION, ENCHANTED, EYES, PORTAL_ROOM, END_VISIT, END_RETURN, DRAGON, ENCHANTING_TABLE, BOOKSHELF, DIAMOND_PICKAXE }
    public record Goal(String id,String branch,String title,String description,String icon,Fact fact,int target,List<String> requires,boolean optional) { }
    private static Goal goal(String id,String branch,String title,String text,String icon,Fact fact,int count,String parent){return new Goal(id,branch,title,text,icon,fact,count,List.of(parent),false);}
    public static List<Goal> goals(int era) {
        if(era>=3&&era<=6)return advanced(era);
        if(era<0||era>2)return List.of();
        var result=new ArrayList<Goal>();
        result.add(new Goal("intro","intro","Sua jornada", "Quatro ramos preparam o cerco. Complete-os, escolha iniciar a horda e, após vencer, decida se quer avançar.","book",Fact.OPEN,1,List.of(),false));
        result.add(goal("prep_0","prep","Primeiros suprimentos",era==0?"Tenha 8 troncos de carvalho.":era==1?"Tenha 32 pedregulhos.":"Tenha 16 barras de ferro.",era==2?"iron_ingot":"oak_log",Fact.MATERIAL,era==0?8:era==1?32:16,"intro"));
        result.add(goal("prep_1","prep","Ferramenta de expedição",era==2?"Tenha uma picareta de ferro.":"Tenha uma picareta de pedra ou ferro.",era==2?"iron_pickaxe":"stone_pickaxe",Fact.TOOL,1,"prep_0"));
        result.add(goal("explore_0","explore",era==0?"Abaixo da superfície":"Reconhecer o território",era==0?"Tenha 16 pedregulhos obtidos durante sua exploração.":"Percorra a pé a região liberada; teletransportes não contam.","stone_pickaxe",era==0?Fact.STONE:Fact.TRAVEL,era==0?16:era==1?256:384,"intro"));
        result.add(goal("explore_1","explore",era==2?"A pedra vermelha":"Luz para voltar",era==2?"Tenha 16 pós de redstone.":"Tenha tochas para sua exploração e defesa.",era==2?"redstone":"torch",era==2?Fact.REDSTONE:Fact.TORCH,era==1?32:16,"explore_0"));
        result.add(goal("combat_0","combat",era==2?"Ataque à distância":"Uma arma preparada",era==2?"Tenha um arco.":"Tenha uma espada de pedra ou ferro.",era==2?"bow":"stone_sword",era==2?Fact.BOW:Fact.SWORD,1,"intro"));
        result.add(goal("combat_1","combat","Defender-se à noite","Derrote zumbis fora do cerco. A preparação de combate permanece após morrer.","iron_sword",Fact.KILLS,era==0?3:era==1?6:10,"combat_0"));
        result.add(goal("build_0","build","Seu território","Coloque blocos sólidos para preparar sua posição. Formato e material ficam a seu critério.","cobblestone",Fact.PLACED,era==0?12:era==1?32:48,"intro"));
        result.add(goal("build_1","build","Uma posição protegida","Entre em um abrigo com teto e proteção em ao menos três lados. Pode adaptar uma caverna.","oak_door",Fact.SHELTER,1,"build_0"));
        if(era>0){
            result.add(goal("prep_2","prep","Reserva de emergência","Tenha comida para repor sua vida e preparar outra tentativa.","bread",Fact.FOOD,era==1?12:16,"prep_1"));
            result.add(goal("explore_2","explore",era==2?"Brilho nas profundezas":"Combustível de reserva",era==2?"Tenha um diamante.":"Tenha 8 carvões.",era==2?"diamond":"coal",era==2?Fact.DIAMOND:Fact.COAL,era==2?1:8,"explore_1"));
            result.add(goal("combat_2","combat",era==2?"Aljava preparada":"Ampliar o alcance",era==2?"Tenha 16 flechas.":"Tenha um arco para defender sua posição.",era==2?"arrow":"bow",era==2?Fact.ARROW:Fact.BOW,era==2?16:1,"combat_1"));
            result.add(goal("build_2","build",era==2?"Defesa com sinal":"Oficina instalada",era==2?"Acione redstone perto de você.":"Tenha uma fornalha para instalar na base.",era==2?"redstone":"furnace",era==2?Fact.CIRCUIT:Fact.FURNACE,1,"build_1"));
        }
        result.add(new Goal("bonus_chest","bonus","Deixar tudo pronto","Tenha um baú de suprimentos. Opcional: não bloqueia o cerco.","chest",Fact.CHEST,1,List.of("build_1"),true));
        return List.copyOf(result);
    }
    private record Step(String title,String text,String icon,Fact fact,int count){}
    private static Step s(String title,String text,String icon,Fact fact,int count){return new Step(title,text,icon,fact,count);}
    private static List<Goal> advanced(int era){
        Step[][] rows=switch(era){
            case 3->new Step[][]{
                {s("Preparar ferro","Tenha 24 barras de ferro.","iron_ingot",Fact.MATERIAL,24),s("Ferramenta confiável","Tenha uma picareta de ferro.","iron_pickaxe",Fact.TOOL,1),s("Provisões para voltar","Tenha 16 alimentos entre pão, carne de porco e maçãs.","bread",Fact.FOOD,16),s("Blocos contra o fogo","Tenha 64 pedregulhos para preparar acessos e cobertura.","cobblestone",Fact.STONE,64)},
                {s("A travessia","Construa e atravesse um portal para o Nether.","flint_and_steel",Fact.NETHER_VISIT,1),s("Rocha infernal","Tenha 32 netherracks.","netherrack",Fact.NETHERRACK,32),s("Areia das almas","Tenha 8 areias das almas.","soul_sand",Fact.SOUL_SAND,8),s("Uma rota de volta","Retorne do Nether ao Overworld nesta era.","obsidian",Fact.NETHER_RETURN,1)},
                {s("Alcance seguro","Tenha um arco.","bow",Fact.BOW,1),s("Aljava de viagem","Tenha 32 flechas.","arrow",Fact.ARROW,32),s("A noite continua","Derrote 12 zumbis naturais nesta era.","iron_sword",Fact.KILLS,12),s("Caçador do além","Derrote um ghast com dano atribuído a você.","bow",Fact.GHAST_KILLS,1)},
                {s("Reforçar o território","Coloque 64 blocos sólidos nesta era.","cobblestone",Fact.PLACED,64),s("Posto no Nether","Coloque 16 blocos sólidos no Nether.","cobblestone",Fact.NETHER_BUILD,16),s("Abrigo infernal","Entre em um abrigo com teto e três lados protegidos no Nether.","oak_door",Fact.NETHER_SHELTER,1),s("Luz do outro lado","Tenha um bloco de pedra luminosa.","glowstone",Fact.GLOWSTONE,1)}
            };
            case 4->new Step[][]{
                {s("Estoque de ferro","Tenha 32 barras de ferro.","iron_ingot",Fact.MATERIAL,32),s("Proteção pessoal","Equipe quatro peças de armadura de ferro ou diamante.","iron_chestplate",Fact.IRON_ARMOR,4),s("Preparação prolongada","Tenha 20 alimentos de viagem.","bread",Fact.FOOD,20),s("Aljava reforçada","Tenha 48 flechas.","arrow",Fact.ARROW,48)},
                {s("Reconhecer a Beta","Caminhe 512 blocos nesta era, sem montarias.","compass",Fact.TRAVEL,512),s("Azul nas profundezas","Tenha 8 lápis-lazúlis.","lapis_lazuli",Fact.LAPIS,8),s("Reserva de combustível","Tenha 16 carvões.","coal",Fact.COAL,16),s("Rota interdimensional","Faça uma viagem ao Nether e retorne nesta era.","obsidian",Fact.NETHER_RETURN,1)},
                {s("Preparado à distância","Tenha um arco.","bow",Fact.BOW,1),s("Domínio da noite","Derrote 16 zumbis fora dos cercos.","iron_sword",Fact.KILLS,16),s("Entre as flechas","Derrote 4 esqueletos.","arrow",Fact.SKELETON_KILLS,4),s("Fios da escuridão","Derrote 4 aranhas.","string",Fact.SPIDER_KILLS,4)},
                {s("A força do pistão","Tenha um pistão.","piston",Fact.PISTON,1),s("Mecanismo acionado","Acione um pistão e fique perto dele enquanto estiver estendido.","redstone",Fact.EXTENDED_PISTON,1),s("Um descanso preparado","Coloque uma cama nesta era.","red_bed",Fact.BED,1),s("Ampliar a defesa","Coloque 80 blocos sólidos na era Beta.","cobblestone",Fact.PLACED,80)}
            };
            case 5->new Step[][]{
                {s("A fome chegou","Tenha 12 pães.","bread",Fact.BREAD,12),s("Reserva para explorar","Tenha 24 alimentos.","bread",Fact.FOOD,24),s("Proteção completa","Equipe quatro peças de ferro ou diamante.","iron_chestplate",Fact.IRON_ARMOR,4),s("Aljava completa","Tenha 64 flechas.","arrow",Fact.ARROW,64),s("Um novo corte","Tenha uma espada de diamante.","diamond_sword",Fact.DIAMOND_SWORD,1)},
                {s("Além do horizonte","Caminhe 768 blocos nesta era.","compass",Fact.TRAVEL,768),s("Pérolas misteriosas","Tenha 4 pérolas de Ender; ainda não é preciso usá-las.","ender_pearl",Fact.ENDER_PEARL,4),s("Conhecer a experiência","Alcance o nível 10 nesta era.","book",Fact.XP_LEVEL,10),s("Outra expedição infernal","Volte de uma viagem ao Nether nesta era.","obsidian",Fact.NETHER_RETURN,1),s("Provisões da colheita","Tenha 32 trigos.","wheat",Fact.WHEAT,32)},
                {s("Arco de aventura","Tenha um arco.","bow",Fact.BOW,1),s("Defesa persistente","Derrote 20 zumbis fora dos cercos.","iron_sword",Fact.KILLS,20),s("Caçador de esqueletos","Derrote 6 esqueletos.","arrow",Fact.SKELETON_KILLS,6),s("Enfrentar a sombra","Derrote 4 endermen.","ender_pearl",Fact.ENDERMAN_KILLS,4),s("Combate nas cavernas","Derrote 6 aranhas.","string",Fact.SPIDER_KILLS,6)},
                {s("Solo para produzir","Prepare ao menos 16 blocos de terra arada perto de você.","iron_hoe",Fact.FARMLAND,16),s("Colheita madura","Quebre 16 plantas de trigo maduro nesta era.","wheat",Fact.HARVEST,16),s("Da fazenda à mesa","Fabrique 16 pães nesta era; podem ser feitos em várias levas.","bread",Fact.CRAFTED_BREAD,16),s("Controle da passagem","Tenha um portão de cerca.","oak_fence_gate",Fact.FENCE_GATE,1),s("Preparar o perímetro","Coloque 96 blocos sólidos na era Adventure.","stone_bricks",Fact.PLACED,96)}
            };
            case 6->new Step[][]{
                {s("Exploração equipada","Tenha uma picareta de diamante.","diamond_pickaxe",Fact.DIAMOND_PICKAXE,1),s("Proteção para o fim","Equipe quatro peças de ferro ou diamante.","iron_chestplate",Fact.IRON_ARMOR,4),s("Aljava do End","Tenha 64 flechas.","arrow",Fact.ARROW,64),s("Provisões da expedição","Tenha 32 alimentos.","bread",Fact.FOOD,32),s("Olhos do fim","Tenha 12 olhos de Ender antes de abrir o portal.","ender_eye",Fact.EYES,12),s("A arma da jornada","Tenha uma espada de diamante.","diamond_sword",Fact.DIAMOND_SWORD,1)},
                {s("Regresso ao Nether","Visite o Nether na era 1.0.","netherrack",Fact.NETHER_VISIT,1),s("Recursos da fortaleza","Tenha 6 varas de blaze.","blaze_rod",Fact.BLAZE_ROD,6),s("Cultivo infernal","Tenha 8 fungos do Nether.","nether_wart",Fact.NETHER_WART,8),s("Sala do portal","Encontre uma sala com as doze molduras do portal do End.","ender_eye",Fact.PORTAL_ROOM,1),s("Outra margem","Entre no End pelo portal.","end_stone",Fact.END_VISIT,1),s("O retorno","Retorne do End após a derrota do dragão.","dragon_egg",Fact.END_RETURN,1)},
                {s("Arco para a ilha","Tenha um arco.","bow",Fact.BOW,1),s("Contra as chamas","Derrote 3 blazes.","blaze_rod",Fact.BLAZE_KILLS,3),s("A sombra enfrentada","Derrote 6 endermen na era 1.0.","ender_pearl",Fact.ENDERMAN_KILLS,6),s("Poder acumulado","Alcance o nível 15.","book",Fact.XP_LEVEL,15),s("Equipamento encantado","Tenha uma arma, ferramenta ou armadura encantada.","diamond_sword",Fact.ENCHANTED,1),s("O fim do começo","Derrote o dragão da campanha. A vitória é compartilhada pelo mundo.","dragon_egg",Fact.DRAGON,1)},
                {s("Defesa reconstruída","Coloque 128 blocos sólidos nesta era.","stone_bricks",Fact.PLACED,128),s("Posto de coleta infernal","Coloque 32 blocos sólidos no Nether.","nether_bricks",Fact.NETHER_BUILD,32),s("Bancada de alquimia","Tenha um suporte de poções.","brewing_stand",Fact.BREWING_STAND,1),s("Alquimia aplicada","Tenha uma poção com efeito, não apenas água.","potion",Fact.POTION,1),s("Uma mesa diferente","Tenha uma mesa de encantamentos.","enchanting_table",Fact.ENCHANTING_TABLE,1),s("Biblioteca preparada","Tenha 15 estantes antes de instalar sua biblioteca.","bookshelf",Fact.BOOKSHELF,15)}
            };
            default->throw new IllegalArgumentException("No advanced chapter");
        };
        var goals=new ArrayList<Goal>();goals.add(new Goal("intro","intro",QuestChapter.get(era).name(),"Conheça as novidades. Os quatro ramos levam à horda final; só depois da vitória você escolhe avançar.","book",Fact.OPEN,1,List.of(),false));
        String[] branches={"prep","explore","combat","build"};for(int b=0;b<4;b++)for(int n=0;n<rows[b].length;n++){var row=rows[b][n];String id=branches[b]+"_"+n;goals.add(goal(id,branches[b],row.title,row.text,row.icon,row.fact,row.count,n==0?"intro":branches[b]+"_"+(n-1)));}
        goals.add(new Goal("bonus_chest","bonus","Uma reserva sua","Tenha um baú. Opcional, sem bloquear o marco.","chest",Fact.CHEST,1,List.of("build_0"),true));
        goals.add(new Goal("bonus_glowstone","bonus","Uma lembrança da expedição","Tenha 4 blocos de pedra luminosa. Opcional.","glowstone",Fact.GLOWSTONE,4,List.of("explore_0"),true));
        if(goals.size()>31)throw new IllegalStateException("Quest mask needs a schema upgrade");return List.copyOf(goals);
    }
    public static Set<String> completed(int era,java.util.function.ToIntFunction<Fact> facts,Set<String> previous) {
        var result=new HashSet<>(previous);boolean changed;
        do{changed=false;for(var q:goals(era))if(!result.contains(q.id())&&result.containsAll(q.requires())&&facts.applyAsInt(q.fact())>=q.target()){result.add(q.id());changed=true;}}while(changed);
        return Set.copyOf(result);
    }
    public static boolean ready(int era,Set<String> completed){var goals=goals(era);return !goals.isEmpty()&&goals.stream().filter(q->!q.optional()).allMatch(q->completed.contains(q.id()));}
    public static int mask(int era,Set<String> completed){int mask=0;var goals=goals(era);for(int i=0;i<goals.size();i++)if(completed.contains(goals.get(i).id()))mask|=1<<i;return mask;}
    public static Set<String> fromMask(int era,int mask){var result=new HashSet<String>();var goals=goals(era);for(int i=0;i<goals.size();i++)if((mask&(1<<i))!=0)result.add(goals.get(i).id());return Set.copyOf(result);}
    public record SiegePlan(int waves,int total,int concurrent,int restTicks) {
        public int waveSize(int zeroBased){return total/waves+(zeroBased<total%waves?1:0);}
    }
    public static SiegePlan siege(int era){if(era<0||era>19)throw new IllegalArgumentException("Invalid era");return new SiegePlan(Math.min(8,2+era/3),8+era*6,Math.min(24,4+era),Math.max(12,30-era)*20);}
    private QuestTree(){}
}
