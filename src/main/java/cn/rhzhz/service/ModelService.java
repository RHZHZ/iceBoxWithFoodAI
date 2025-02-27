package cn.rhzhz.service;

import org.json.JSONException;
import reactor.core.publisher.Flux;

public interface ModelService {
    Flux<String> streamChatCompletion(String message) throws JSONException;
}
