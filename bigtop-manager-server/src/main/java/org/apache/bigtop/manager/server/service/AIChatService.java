package org.apache.bigtop.manager.server.service;

import org.apache.bigtop.manager.server.model.dto.PlatformDTO;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthorizedVO;
import org.apache.bigtop.manager.server.model.vo.PlatformVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AIChatService {
    List<PlatformVO> platforms();

    List<PlatformAuthorizedVO> authorizedPlatforms();

    PlatformVO addAuthorizedPlatform(PlatformDTO platformDTO);

    int deleteAuthorizedPlatform(Long platformId);

    ChatThreadVO createChatThreads(Long platformId, String model);

    int deleteChatThreads(Long platformId, Long threadId);

    List<ChatThreadVO> getAllChatThreads(Long platformId, String model);

    SseEmitter talk(Long platformId, Long threadId, String message);

    List<ChatMessageVO> history(Long platformId, Long threadId);
}
