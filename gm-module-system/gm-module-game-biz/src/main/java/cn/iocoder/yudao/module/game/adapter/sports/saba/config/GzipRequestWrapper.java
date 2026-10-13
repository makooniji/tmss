package cn.iocoder.yudao.module.game.adapter.sports.saba.config;

import cn.hutool.core.io.IoUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class GzipRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body; // 使用 final 保证不可变性

    public GzipRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        // 1. 先把原始流读出来
        byte[] rawData = IoUtil.readBytes(request.getInputStream());

        String encoding = request.getHeader("Content-Encoding");
        if (encoding != null && encoding.contains("gzip") && isGzip(rawData)) {
            // 2. 解压
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(rawData))) {
                this.body = IoUtil.readBytes(gis);
            }
        } else {
            this.body = rawData;
        }
    }

    // 校验魔数，直接通过数组判断，不破坏流指针
    private boolean isGzip(byte[] data) {
        return data != null && data.length >= 2 &&
                data[0] == (byte) 0x1f &&
                data[1] == (byte) (0x8b & 0xff);
    }

    @Override
    public ServletInputStream getInputStream() {
        // 关键：每次都 new 一个新的 ByteArrayInputStream
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);

        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            // 必须重写这个，提高性能并保证 Spring 正常读取
            @Override
            public int read(byte[] b, int off, int len) {
                return bais.read(b, off, len);
            }

            @Override
            public boolean isFinished() { return bais.available() == 0; }

            @Override
            public boolean isReady() { return true; }

            @Override
            public void setReadListener(ReadListener readListener) {}
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    public String getBodyString() {
        // 建议加上字符集防止乱码
        String charset = getCharacterEncoding();
        return new String(body, charset != null ? Charset.forName(charset) : StandardCharsets.UTF_8);
    }
}