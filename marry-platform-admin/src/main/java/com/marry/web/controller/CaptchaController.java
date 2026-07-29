package com.marry.web.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.marry.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Generates a random numeric captcha (default 4 digits) backed by an in-memory
 * BufferedImage, returning both the UUID token and the PNG bytes to the browser.
 *
 * <p>State is stored in Redis under {@code marry:captcha:<uuid>} with a 5-minute
 * TTL so it can be verified on login.</p>
 */
@Slf4j
@Tag(name = "验证码")
@RestController
@RequestMapping("/auth/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private static final String PREFIX = "marry:captcha:";
    private static final int TTL_SECONDS = 300;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;

    private final StringRedisTemplate redisTemplate;

    @Operation(summary = "生成图形验证码")
    @GetMapping
    public void captcha(HttpServletResponse response) throws IOException {
        String uuid = IdUtil.fastSimpleUUID();
        String code = randomCode(4);
        redisTemplate.opsForValue().set(PREFIX + uuid, code.toLowerCase(), TTL_SECONDS, TimeUnit.SECONDS);

        BufferedImage img = draw(code);
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Access-Control-Expose-Headers", "X-Captcha-Id");
        response.setHeader("X-Captcha-Id", uuid);
        ImageIO.write(img, "png", response.getOutputStream());
    }

    /** Validates a captcha code by UUID; called manually from AuthController. */
    public static boolean verify(StringRedisTemplate redis, String uuid, String code) {
        if (StrUtil.isBlank(uuid) || StrUtil.isBlank(code)) return false;
        String key = PREFIX + uuid;
        String expected = redis.opsForValue().get(key);
        if (expected == null) return false;
        // consume immediately to prevent reuse
        redis.delete(key);
        return expected.equalsIgnoreCase(code);
    }

    private String randomCode(int len) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(r.nextInt(10));
        return sb.toString();
    }

    private BufferedImage draw(String code) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(245, 247, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // noise dots
        Random r = new Random();
        for (int i = 0; i < 50; i++) {
            g.setColor(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255), 120));
            g.fillRect(r.nextInt(WIDTH), r.nextInt(HEIGHT), 1, 1);
        }

        g.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(20 + r.nextInt(80), 50 + r.nextInt(80), 130 + r.nextInt(80)));
            int x = 20 + i * 22;
            int y = 25 + r.nextInt(8);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }

        // strikethrough lines
        for (int i = 0; i < 3; i++) {
            g.setColor(new Color(r.nextInt(200), r.nextInt(200), r.nextInt(200)));
            g.drawLine(r.nextInt(WIDTH), r.nextInt(HEIGHT), r.nextInt(WIDTH), r.nextInt(HEIGHT));
        }

        g.dispose();
        return img;
    }
}