package dev.xaka.visuals;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;

/** Volumetric player cosmetics: a curved cloth cape and solid 3D hats. */
public final class XakaCosmeticsRenderer {
    private static final Identifier WHITE = Identifier.of("xakavisuals", "textures/entity/cosmetic_white.png");
    private XakaCosmeticsRenderer() {}

    public static void render(VertexConsumerProvider consumers, double x, double y, double z,
                              float yawDegrees, float headYawDegrees, int capeStyle, int hatStyle, float tick) {
        if (capeStyle <= 0 && hatStyle <= 0) return;
        double a = Math.toRadians(yawDegrees);
        double sx = Math.cos(a), sz = Math.sin(a);
        double bx = Math.sin(a), bz = -Math.cos(a);
        if (capeStyle > 0) {
            int base = switch (capeStyle) {
                case 2 -> 0xFF171522;
                case 3 -> 0xFF5B1827;
                case 4 -> 0xFF18334B;
                default -> 0xFF32105A;
            };
            VertexConsumer out = consumers.getBuffer(RenderLayer.getEntityTranslucent(WHITE));
            // Wide shoulder line, narrowing slightly toward the lower hem; several
            // curved cloth segments give the cape real volume rather than flat strips.
            double sway = Math.sin(tick * 0.08) * 0.035;
            double[][] rows = {{0.32,0.18},{0.08,0.23},{-0.20,0.26},{-0.48,0.29},{-0.76,0.31},{-1.00,0.33}};
            for (int r=0;r<rows.length-1;r++) {
                double y0=1.43+rows[r][0], y1=1.43+rows[r+1][0];
                double w0=rows[r][1], w1=rows[r+1][1];
                double d0=0.18+sway*(r+1), d1=0.20+sway*(r+2);
                int shade = (r%2==0) ? base : tint(base,0.84f);
                quad(out, point(x,y,z,sx,sz,bx,bz,-w0,y0,-d0),
                        point(x,y,z,sx,sz,bx,bz,w0,y0,d0),
                        point(x,y,z,sx,sz,bx,bz,w1,y1,-d1),
                        point(x,y,z,sx,sz,bx,bz,-w1,y1,d1), shade);
                // narrow raised outer piping, rendered as solid side panels
                quad(out, point(x,y,z,sx,sz,bx,bz,-w0,y0,d0+0.012),
                        point(x,y,z,sx,sz,bx,bz,-w0+0.025,y0,-d0-0.012),
                        point(x,y,z,sx,sz,bx,bz,-w1+0.025,y1,d1+0.012),
                        point(x,y,z,sx,sz,bx,bz,-w1,y1,-d1-0.012), tint(base,1.45f));
                quad(out, point(x,y,z,sx,sz,bx,bz,w0-0.025,y0,-d0-0.012),
                        point(x,y,z,sx,sz,bx,bz,w0,y0,-d0-0.012),
                        point(x,y,z,sx,sz,bx,bz,w1,y1,-d1-0.012),
                        point(x,y,z,sx,sz,bx,bz,w1-0.025,y1,-d1-0.012), tint(base,1.45f));
            }
            // Folded collar at the shoulders.
            quad(out, point(x,y,z,sx,sz,bx,bz,-0.34,1.48,-0.17),
                    point(x,y,z,sx,sz,bx,bz,0.34,1.48,0.17),
                    point(x,y,z,sx,sz,bx,bz,0.27,1.30,-0.25),
                    point(x,y,z,sx,sz,bx,bz,-0.27,1.30,0.25), tint(base,1.22f));
        }
        if (hatStyle > 0) {
            double ha = Math.toRadians(headYawDegrees);
            double hsx = Math.cos(ha), hsz = Math.sin(ha);
            double hbx = Math.sin(ha), hbz = -Math.cos(ha);
            int color = switch (hatStyle) {
                case 2 -> 0xFF24202F;
                case 3 -> 0xFF70451F;
                case 4 -> 0xFF243C65;
                case 5 -> 0xFF45B957;
                case 6 -> 0xFFE0B34A;
                default -> 0xFF482078;
            };
            VertexConsumer out = consumers.getBuffer(RenderLayer.getEntityTranslucent(WHITE));
            // Head center is around 1.5 blocks; brim and crown are independent 3D solids.
            if (hatStyle != 6) box(out,x,y,z,hsx,hsz,hbx,hbz,-0.34,1.76,-0.34,0.34,1.82,0.34,color);
            if (hatStyle == 6) {
                // Chinese conical hat: broad solid brim, tapered crown and a contrasting lower band.
                box(out,x,y,z,hsx,hsz,hbx,hbz,-0.43,1.78,-0.43,0.43,1.84,0.43,color);
                cone(out,x,y,z,hsx,hsz,hbx,hbz,0.0,1.83,0.0,0.36,2.40,color);
                box(out,x,y,z,hsx,hsz,hbx,hbz,-0.30,1.84,-0.30,0.30,1.90,0.30,0xFF8C3D27);
            } else if (hatStyle == 2) {
                // Witch hat: a broad brim and tapering, visibly three-dimensional crown.
                box(out,x,y,z,sx,sz,bx,bz,-0.22,1.82,-0.22,0.22,2.02,0.22,tint(color,1.08f));
                pyramid(out,x,y,z,hsx,hsz,hbx,hbz,0.0,2.02,0.0,0.18,2.48,color);
            } else if (hatStyle == 3) {
                // Adventurer hat with a raised crown and a front band.
                box(out,x,y,z,sx,sz,bx,bz,-0.23,1.82,-0.23,0.23,2.08,0.23,tint(color,1.12f));
                box(out,x,y,z,sx,sz,bx,bz,-0.235,1.91,-0.245,0.235,1.97,-0.22,0xFFB58A43);
            } else if (hatStyle == 4) {
                // Compact crown with a visor: a cap.
                box(out,x,y,z,sx,sz,bx,bz,-0.25,1.82,-0.25,0.25,2.02,0.25,tint(color,1.12f));
                box(out,x,y,z,sx,sz,bx,bz,-0.22,1.82,-0.48,0.22,1.88,-0.20,color);
            } else if (hatStyle == 5) {
                // Frog hat with rounded green crown, broad brim, raised eyes and pupils.
                box(out,x,y,z,hsx,hsz,hbx,hbz,-0.34,1.82,-0.34,0.34,1.89,0.34,color);
                box(out,x,y,z,hsx,hsz,hbx,hbz,-0.27,1.88,-0.27,0.27,2.13,0.27,0xFF45B957);
                box(out,x,y,z,hsx,hsz,hbx,hbz,-0.22,2.08,-0.22,-0.055,2.27,-0.055,0xFF329644);
                box(out,x,y,z,hsx,hsz,hbx,hbz,0.055,2.08,-0.22,0.22,2.27,-0.055,0xFF329644);
                box(out,x,y,z,hsx,hsz,hbx,hbz,-0.225,2.20,-0.235,-0.045,2.37,-0.045,0xFFB8F36A);
                box(out,x,y,z,hsx,hsz,hbx,hbz,0.045,2.20,-0.235,0.225,2.37,-0.045,0xFFB8F36A);
                box(out,x,y,z,hsx,hsz,hbx,hbz,-0.155,2.23,-0.252,-0.105,2.31,-0.202,0xFF17221A);
                box(out,x,y,z,hsx,hsz,hbx,hbz,0.105,2.23,-0.252,0.155,2.31,-0.202,0xFF17221A);
            } else {
                // Royal top hat with a taller crown and contrasting ribbon.
                box(out,x,y,z,sx,sz,bx,bz,-0.23,1.82,-0.23,0.23,2.18,0.23,tint(color,1.12f));
                box(out,x,y,z,sx,sz,bx,bz,-0.235,2.02,-0.24,0.235,2.08,0.24,0xFFB99BFF);
            }
        }
    }

    private static double[] point(double x,double y,double z,double sx,double sz,double bx,double bz,
                                  double side,double py,double back) {
        return new double[]{x+sx*side+bx*back,y+py,z+sz*side+bz*back};
    }
    private static void quad(VertexConsumer o,double[] a,double[] b,double[] c,double[] d,int color) {
        tri(o,a,b,c,color); tri(o,a,c,d,color); tri(o,c,b,a,color); tri(o,d,c,a,color);
    }
    private static void tri(VertexConsumer o,double[] a,double[] b,double[] c,int color) {
        vertex(o,a,color,0,0); vertex(o,b,color,1,0); vertex(o,c,color,1,1);
    }
    private static void vertex(VertexConsumer o,double[] p,int color,float u,float v) {
        o.vertex((float)p[0],(float)p[1],(float)p[2],color,u,v,0,0x00F000F0,0,1,0);
    }
    private static void box(VertexConsumer o,double x,double y,double z,double sx,double sz,double bx,double bz,
                            double x0,double y0,double z0,double x1,double y1,double z1,int color) {
        double[][] p = {
            point(x,y,z,sx,sz,bx,bz,x0,y0,z0), point(x,y,z,sx,sz,bx,bz,x1,y0,z0),
            point(x,y,z,sx,sz,bx,bz,x1,y1,z0), point(x,y,z,sx,sz,bx,bz,x0,y1,z0),
            point(x,y,z,sx,sz,bx,bz,x0,y0,z1), point(x,y,z,sx,sz,bx,bz,x1,y0,z1),
            point(x,y,z,sx,sz,bx,bz,x1,y1,z1), point(x,y,z,sx,sz,bx,bz,x0,y1,z1)
        };
        quad(o,p[0],p[1],p[2],p[3],color); quad(o,p[5],p[4],p[7],p[6],tint(color,0.86f));
        quad(o,p[4],p[0],p[3],p[7],tint(color,0.92f)); quad(o,p[1],p[5],p[6],p[2],tint(color,1.12f));
        quad(o,p[3],p[2],p[6],p[7],tint(color,1.18f)); quad(o,p[4],p[5],p[1],p[0],tint(color,0.72f));
    }
    private static void cone(VertexConsumer o,double x,double y,double z,double sx,double sz,double bx,double bz,
                              double cx,double baseY,double cz,double radius,double topY,int color) {
        int segments = 32;
        double[] apex = point(x,y,z,sx,sz,bx,bz,cx,topY,cz);
        for (int i=0;i<segments;i++) {
            double a0 = Math.PI * 2.0 * i / segments;
            double a1 = Math.PI * 2.0 * (i+1) / segments;
            double[] p0 = point(x,y,z,sx,sz,bx,bz,cx+Math.cos(a0)*radius,baseY,cz+Math.sin(a0)*radius);
            double[] p1 = point(x,y,z,sx,sz,bx,bz,cx+Math.cos(a1)*radius,baseY,cz+Math.sin(a1)*radius);
            int shade = (i % 4 == 0) ? tint(color,1.12f) : color;
            tri(o,p0,p1,apex,shade);
        }
    }

    private static void pyramid(VertexConsumer o,double x,double y,double z,double sx,double sz,double bx,double bz,
                                double cx,double cy,double cz,double radius,double topY,int color) {
        double[] a=point(x,y,z,sx,sz,bx,bz,cx-radius,cy,cz-radius);
        double[] b=point(x,y,z,sx,sz,bx,bz,cx+radius,cy,cz-radius);
        double[] c=point(x,y,z,sx,sz,bx,bz,cx+radius,cy,cz+radius);
        double[] d=point(x,y,z,sx,sz,bx,bz,cx-radius,cy,cz+radius);
        double[] t=point(x,y,z,sx,sz,bx,bz,cx+0.015,topY,cz+0.015);
        tri(o,a,b,t,color); tri(o,b,c,t,tint(color,1.12f)); tri(o,c,d,t,color); tri(o,d,a,t,tint(color,0.86f));
    }
    private static int tint(int c,float f) {
        int r=Math.min(255,Math.max(0,Math.round(((c>>16)&255)*f)));
        int g=Math.min(255,Math.max(0,Math.round(((c>>8)&255)*f)));
        int b=Math.min(255,Math.max(0,Math.round((c&255)*f)));
        return (c&0xFF000000)|(r<<16)|(g<<8)|b;
    }
}
