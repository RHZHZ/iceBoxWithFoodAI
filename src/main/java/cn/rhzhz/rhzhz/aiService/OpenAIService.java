package cn.rhzhz.aiService;


import cn.rhzhz.pojo.OpenAiServiceWrapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class OpenAIService implements ModelService {

    @Value("${ai.providers.openai.api-key}")
    private String apiKey;

    @Value("${ai.providers.openai.base-url}")
    private String baseUrl;

    @Override
    public Flux<String> streamChatCompletion(String message) {
        // 创建包装类实例
        OpenAiServiceWrapper wrapper = new OpenAiServiceWrapper(apiKey, baseUrl);
        OpenAiService service = wrapper.getOpenAiService();
        return Flux.create(emitter -> {
            service.streamChatCompletion(ChatCompletionRequest.builder()
                            .model("gpt-4")
                            .messages(Collections.singletonList(new ChatMessage("user", message)))
                            .build())
                    .blockingForEach(chunk -> {
                        String content = chunk.getChoices().get(0).getMessage().getContent();
                        if (content != null) {
                            emitter.next(content);
                        }
                    });
            emitter.complete();
        });
    }
}
