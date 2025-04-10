package programo._pro.controller;

import programo._pro.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue")
public class UserQueueController {
    private final UserQueueService userQueueService;

    @GetMapping("/waiting/register")
    public Long registerWaitingList(@RequestParam(name = "email") String email) {
        return userQueueService.registerWaitingList(email);
    }

    @GetMapping("/processing/enter")
    public String enter(){
        userQueueService.enter();
        return "success";
    }

    @GetMapping("/processing/exit")
    public String exit(@RequestParam(name = "email") String email) {
        userQueueService.exit(email);
        return "success";
    }

}
