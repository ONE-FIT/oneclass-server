package oneclass.oneclass.global.auth.jwt;

public final class TokenUtils {
    private TokenUtils() {}

    // 헤더·바디에서 넘어온 토큰 문자열 정리: Bearer/양쪽 따옴표/공백 제거
    public static String cleanup(String token) {
        if (token == null) return null;
        String v = token.trim();
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) v = v.substring(7).trim();
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
            v = v.substring(1, v.length() - 1);
        }
        return v;
    }

    // Compact JWE(암호화) 탐지: 세그먼트 5개(점 4개)
    public static boolean isLikelyJwe(String t) {
        if (t == null) return false;
        int dots = 0;
        for (int i = 0; i < t.length(); i++) if (t.charAt(i) == '.') dots++;
        return dots == 4;
    }
}