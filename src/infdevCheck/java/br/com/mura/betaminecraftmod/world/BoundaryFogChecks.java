package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.client.BoundaryFog;
final class BoundaryFogChecks {
    static void run() {
        if(BoundaryFog.end(256,0,0,-512,512,-512,512)!=256)throw new AssertionError("Interior fog changed");
        for(double sign:new double[]{-1,1}) {
            if(BoundaryFog.end(256,sign*500,0,-512,512,-512,512)!=8)throw new AssertionError("Side not hidden");
            if(BoundaryFog.end(256,0,sign*500,-512,512,-512,512)!=8)throw new AssertionError("Z side not hidden");
            if(BoundaryFog.end(256,sign*500,sign*500,-512,512,-512,512)!=8)throw new AssertionError("Corner not hidden");
        }
        if(BoundaryFog.end(256,512,0,-512,512,-512,512)!=.25f)throw new AssertionError("Invalid border fog");
        if(BoundaryFog.end(256,500,0,-1024,1024,-1024,1024)!=256)throw new AssertionError("Fog did not recede on unlock");
        System.out.println("Boundary fog checks passed: four sides, corners, border and expansion.");
    }
}
