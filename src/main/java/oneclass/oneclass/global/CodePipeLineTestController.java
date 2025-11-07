package oneclass.oneclass.global;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodePipeLineTestController {
    @GetMapping("/codepipelinetesting")
    public ResponseEntity<?> codePipeLineTest() {
        return ResponseEntity.ok("자동 베포 성공!");
    }
}
