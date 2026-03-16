package com.notification.controller;

import com.notification.dto.RoutingRuleRequest;
import com.notification.dto.RoutingRuleResponse;
import com.notification.enums.MessageType;
import com.notification.model.RoutingRule;
import com.notification.service.RoutingRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RoutingRuleController {

    private final RoutingRuleService routingRuleService;

    @PostMapping
    public ResponseEntity<RoutingRuleResponse> createRule(@Valid @RequestBody RoutingRuleRequest request) {
        log.info("Creating routing rule for message type: {}", request.getMessageType());
        RoutingRule rule = routingRuleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(rule));
    }

    @PutMapping("/{messageType}")
    public ResponseEntity<RoutingRuleResponse> updateRule(
            @PathVariable MessageType messageType,
            @Valid @RequestBody RoutingRuleRequest request) {
        log.info("Updating routing rule for message type: {}", messageType);
        RoutingRule rule = routingRuleService.updateRule(messageType, request);
        return ResponseEntity.ok(mapToResponse(rule));
    }

    @DeleteMapping("/{messageType}")
    public ResponseEntity<Void> deleteRule(@PathVariable MessageType messageType) {
        log.info("Deleting routing rule for message type: {}", messageType);
        routingRuleService.deleteRule(messageType);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{messageType}")
    public ResponseEntity<RoutingRuleResponse> getRule(@PathVariable MessageType messageType) {
        log.info("Fetching routing rule for message type: {}", messageType);
        RoutingRule rule = routingRuleService.getRuleForMessageType(messageType);
        if (rule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapToResponse(rule));
    }

    @GetMapping
    public ResponseEntity<List<RoutingRuleResponse>> getAllRules() {
        log.info("Fetching all routing rules");
        List<RoutingRule> rules = routingRuleService.getAllRules();
        return ResponseEntity.ok(rules.stream().map(this::mapToResponse).collect(Collectors.toList()));
    }

    private RoutingRuleResponse mapToResponse(RoutingRule rule) {
        return RoutingRuleResponse.builder()
                .messageType(rule.getMessageType())
                .channels(rule.getChannels())
                .active(rule.isActive())
                .retryCount(rule.getRetryCount())
                .fallbackChannel(rule.getFallbackChannel())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}