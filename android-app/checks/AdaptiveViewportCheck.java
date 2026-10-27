package com.facechaos.mobile;
public final class AdaptiveViewportCheck {
 public static void main(String[] args){
  int[][] windows={{1080,2340,3},{720,1600,2},{2340,1080,3},{1600,2560,2},{2560,1600,2},{800,600,1},{600,800,1},{320,480,1}};
  for(int[] window:windows){int w=window[0],h=window[1],d=window[2];int game=AdaptiveViewport.overlayWidth(w,h,d),controls=AdaptiveViewport.controlsWidth(w,d);
   if(game<1||game>w||controls<1||controls>w)throw new AssertionError("Outside window");
   if(w<=h&&w/d<600&&game!=w)throw new AssertionError("Phone size changed");
   if(w>h&&game>h*9f/16+1)throw new AssertionError("Landscape game stretched");
   for(int mode=0;mode<15;mode++){float scale=game/390f;OverlayLayout layout=new OverlayLayout(h/scale,130*d/scale,mode);if(!Float.isFinite(layout.gameTop))throw new AssertionError("Invalid game geometry");}
  }
  System.out.println("Phone, tablet, landscape and split-window viewport checks passed");
 }
}
