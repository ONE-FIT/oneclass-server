package oneclass.oneclass.auth.controller;

import oneclass.oneclass.auth.dto.ResponseToken;
import oneclass.oneclass.auth.jwt.JWTProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
class JwtTestController {

    private final JWTProvider jwtProvider;

    public JwtTestController(JWTProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @GetMapping("/token/{username}")
    public ResponseToken generateToken(@PathVariable String username) {
        return jwtProvider.generateTokenJwe(username);
    }

    @GetMapping("/validate/{token}")
    public boolean validateToken(@PathVariable String token) throws Exception {
        String plain = jwtProvider.decyptToken(token);
        return jwtProvider.validateToken(plain);
    }
}
