package cn.rhzhz.controller;

import cn.rhzhz.service.ModelServiceFactory;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final ModelServiceFactory modelServiceFactory;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestParam String message,
            @RequestParam(defaultValue = "openai") String provider) throws JSONException {

        return modelServiceFactory.getService(provider)
                .streamChatCompletion(message)
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(e -> Flux.just("错误: " + e.getMessage()));
    }
}