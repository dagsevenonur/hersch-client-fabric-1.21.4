package com.herschclient.ui.lunar;

import net.minecraft.client.gui.DrawContext;

public final class UiDraw {
    private UiDraw(){}

    // Basit rounded rect (çok performanslı değil ama UI için yeterli)
    public static void roundedRect(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        // orta
        ctx.fill(x + r, y, x + w - r, y + h, color);
        // sol/sağ
        ctx.fill(x, y + r, x + r, y + h - r, color);
        ctx.fill(x + w - r, y + r, x + w, y + h - r, color);

        // köşeler (basit daire dilimi yaklaşımı)
        circleQuarter(ctx, x + r, y + r, r, color, 180, 270);           // sol üst
        circleQuarter(ctx, x + w - r - 1, y + r, r, color, 270, 360);   // sağ üst
        circleQuarter(ctx, x + r, y + h - r - 1, r, color, 90, 180);    // sol alt
        circleQuarter(ctx, x + w - r - 1, y + h - r - 1, r, color, 0, 90); // sağ alt
    }

    private static void circleQuarter(DrawContext ctx, int cx, int cy, int r, int color, int deg0, int deg1) {
        // piksel doldurma (r küçük olduğu için ok)
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                if (dx*dx + dy*dy > r*r) continue;
                double ang = Math.toDegrees(Math.atan2(dy, dx));
                if (ang < 0) ang += 360;
                if (inRange(ang, deg0, deg1)) {
                    int px = cx + dx;
                    int py = cy + dy;
                    ctx.fill(px, py, px + 1, py + 1, color);
                }
            }
        }
    }

    private static boolean inRange(double ang, int a0, int a1) {
        return ang >= a0 && ang <= a1;
    }

    public static boolean isInside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }
}
