package org.apache.bigtop.manager.server.controller;

import org.apache.bigtop.manager.server.enums.ResponseStatus;
import org.apache.bigtop.manager.server.model.converter.PlatformConverter;
import org.apache.bigtop.manager.server.model.dto.PlatformDTO;
import org.apache.bigtop.manager.server.model.req.PlatformReq;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthorizedVO;
import org.apache.bigtop.manager.server.model.vo.PlatformVO;
import org.apache.bigtop.manager.server.service.AIChatService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "AI Chat Controller")
@RestController
@RequestMapping("/ai/chat/")
public class AIChatController {

    @Resource
    private AIChatService chatService;

    @Operation(summary = "platforms", description = "Get all platforms")
    @GetMapping("/platforms")
    public ResponseEntity<List<PlatformVO>> platforms() {
        return ResponseEntity.success(chatService.platforms());
    }

    @Operation(summary = "platforms", description = "Get authorized platforms")
    @GetMapping("/platforms/authorized")
    public ResponseEntity<List<PlatformAuthorizedVO>> authorizedPlatforms() {
        return ResponseEntity.success(chatService.authorizedPlatforms());
    }

    @Operation(summary = "platforms", description = "Add authorized platforms")
    @PutMapping("/platforms")
    public ResponseEntity<PlatformVO> addAuthorizedPlatform(
            @RequestBody PlatformReq platformReq
    ) {
        PlatformDTO platformDTO = PlatformConverter.INSTANCE.fromReq2DTO(platformReq);
        return ResponseEntity.success(chatService.addAuthorizedPlatform(platformDTO));
    }

    @Operation(summary = "platforms", description = "Delete authorized platforms")
    @DeleteMapping("/platforms/{platformId}")
    public ResponseEntity<Integer> deleteAuthorizedPlatform(@PathVariable Long platformId) {
        int code = chatService.deleteAuthorizedPlatform(platformId);
        if (code != 0) {
            return ResponseEntity.error(ResponseStatus.PARAMETER_ERROR,"权限不足");
        }
        return ResponseEntity.success(0);
    }

    @Operation(summary = "new threads", description = "Create a chat threads")
    @PutMapping("/platforms/{platformId}/threads")
    public ResponseEntity<ChatThreadVO> createChatThreads(
            @PathVariable Long platformId, @RequestParam String model) {
        return ResponseEntity.success(chatService.createChatThreads(platformId, model));
    }

    @Operation(summary = "delete threads", description = "Delete a chat threads")
    @DeleteMapping("platforms/{platformId}/threads/{threadId}")
    public ResponseEntity<Integer> deleteChatThreads(@PathVariable Long platformId, @PathVariable Long threadId) {
        int code = chatService.deleteChatThreads(platformId, threadId);
        if (code != 0) {
            return ResponseEntity.error(ResponseStatus.PARAMETER_ERROR, "无内容");
        }
        return ResponseEntity.success(0);
    }

    @Operation(summary = "get", description = "Get all threads of a platform")
    @GetMapping("platforms/{platformId}/threads")
    public ResponseEntity<List<ChatThreadVO>> getAllChatThreads(
            @PathVariable Long platformId, @RequestParam(value = "model", required = false) String model) {
        return ResponseEntity.success(chatService.getAllChatThreads(platformId, model));
    }

    @Operation(summary = "talk", description = "Talk with AI")
    @PostMapping("platforms/{platformId}/threads/{threadId}/talk")
    public SseEmitter talk(@PathVariable Long platformId, @PathVariable Long threadId, @RequestParam String message) {
        return chatService.talk(platformId, threadId, message);
    }

    @Operation(summary = "history", description = "Get chat records")
    @GetMapping("platforms/{platformId}/threads/{threadId}/history")
    public ResponseEntity<List<ChatMessageVO>> history(@PathVariable Long platformId, @PathVariable Long threadId) {
        return ResponseEntity.success(chatService.history(platformId, threadId));
    }
}
