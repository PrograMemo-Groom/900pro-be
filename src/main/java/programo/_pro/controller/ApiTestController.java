package programo._pro.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class ApiTestController {

    @GetMapping("/data")
    public String data(){
        return "연결 테스트 너 한번 더 그래봐 이번엔 돼라 ? ";
    }
}
