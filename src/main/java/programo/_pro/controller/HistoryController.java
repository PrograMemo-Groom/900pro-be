package programo._pro.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "히스토리 페이지", description = "히스토리 페이지 API")
public class HistoryController {

}
