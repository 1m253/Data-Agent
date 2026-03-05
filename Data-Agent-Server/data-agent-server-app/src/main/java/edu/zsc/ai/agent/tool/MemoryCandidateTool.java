package edu.zsc.ai.agent.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.invocation.InvocationParameters;
import edu.zsc.ai.agent.tool.model.AgentToolResult;
import edu.zsc.ai.common.constant.RequestContextConstant;
import edu.zsc.ai.common.converter.ai.MemoryConverter;
import edu.zsc.ai.config.ai.MemoryProperties;
import edu.zsc.ai.domain.model.dto.response.ai.MemoryCandidateResponse;
import edu.zsc.ai.domain.model.entity.ai.AiMemoryCandidate;
import edu.zsc.ai.domain.service.ai.MemoryCandidateService;
import edu.zsc.ai.domain.service.ai.MemoryService;
import edu.zsc.ai.domain.service.ai.model.MemorySearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AgentTool
@Slf4j
@RequiredArgsConstructor
public class MemoryCandidateTool {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final MemoryCandidateService memoryCandidateService;
    private final MemoryService memoryService;
    private final MemoryProperties memoryProperties;

    @Tool({
            "[WHAT] Search the user's committed long-term memories by semantic similarity.",
            "[WHEN] Use when the user asks about their saved preferences, past decisions, or known business rules.",
            "[HOW] Provide a query text describing what to search for. Returns top-K most relevant memories."
    })
    public AgentToolResult searchMemories(
            @P("Natural language query to search memories") String queryText,
            @P(value = "Maximum number of results to return", required = false) Integer limit,
            InvocationParameters parameters) {
        try {
            Long userId = parameters.get(RequestContextConstant.USER_ID);
            if (Objects.isNull(userId)) {
                return AgentToolResult.noContext();
            }

            MemoryProperties.Retrieval retrieval = memoryProperties.getRetrieval();
            int safeLimit = limit == null ? retrieval.getCandidateTopK()
                    : Math.max(1, Math.min(limit, MAX_LIMIT));

            List<MemorySearchResult> results = memoryService.searchActiveMemories(
                    userId, queryText, safeLimit, retrieval.getMinScore());

            if (results.isEmpty()) {
                return AgentToolResult.empty();
            }
            return AgentToolResult.success(results);
        } catch (Exception e) {
            log.error("[Tool error] searchMemories", e);
            return AgentToolResult.fail(e);
        }
    }

    @Tool({
            "[WHAT] List memory candidates for the current conversation.",
            "[WHEN] Use to review existing candidate memories before creating new ones and avoid duplicates.",
            "[HOW] Conversation id is resolved from invocation context; optional limit defaults to 50."
    })
    public AgentToolResult listCandidateMemories(
            @P(value = "Conversation id from current session context", required = false) Long conversationId,
            @P(value = "Maximum number of candidates to return", required = false) Integer limit,
            InvocationParameters parameters) {
        try {
            Long userId = parameters.get(RequestContextConstant.USER_ID);
            Long contextConversationId = parameters.get(RequestContextConstant.CONVERSATION_ID);
            if (Objects.isNull(userId) || Objects.isNull(contextConversationId)) {
                return AgentToolResult.noContext();
            }
            if (conversationId != null && !conversationId.equals(contextConversationId)) {
                log.warn("[Tool] listCandidateMemories ignored mismatched conversationId arg={}, context={}",
                        conversationId, contextConversationId);
            }

            int safeLimit = limit == null ? DEFAULT_LIMIT : Math.max(1, Math.min(limit, MAX_LIMIT));
            List<AiMemoryCandidate> candidates = memoryCandidateService
                    .listCurrentConversationCandidates(userId, contextConversationId, safeLimit);

            List<MemoryCandidateResponse> response = candidates.stream()
                    .map(MemoryConverter::toCandidateResponse)
                    .toList();

            if (response.isEmpty()) {
                return AgentToolResult.empty();
            }
            return AgentToolResult.success(response);
        } catch (Exception e) {
            log.error("[Tool error] listCandidateMemories", e);
            return AgentToolResult.fail(e);
        }
    }

    @Tool({
            "[WHAT] Create a memory candidate in the current conversation.",
            "[WHEN] Use when the user explicitly confirms reusable preference, business rule, term definition, or reusable SQL pattern.",
            "[HOW] Conversation id is resolved from invocation context. Set candidateType, candidateContent, and optional reason."
    })
    public AgentToolResult createCandidateMemory(
            @P(value = "Conversation id from current session context", required = false) Long conversationId,
            @P("Candidate type: PREFERENCE/BUSINESS_RULE/KNOWLEDGE_POINT/GOLDEN_SQL_CASE/WORKFLOW_CONSTRAINT") String candidateType,
            @P("Normalized candidate memory text to be reviewed by user") String candidateContent,
            @P(value = "Why this candidate should be saved", required = false) String reason,
            InvocationParameters parameters) {
        try {
            Long userId = parameters.get(RequestContextConstant.USER_ID);
            Long contextConversationId = parameters.get(RequestContextConstant.CONVERSATION_ID);
            if (Objects.isNull(userId) || Objects.isNull(contextConversationId)) {
                return AgentToolResult.noContext();
            }
            if (conversationId != null && !conversationId.equals(contextConversationId)) {
                log.warn("[Tool] createCandidateMemory ignored mismatched conversationId arg={}, context={}",
                        conversationId, contextConversationId);
            }

            AiMemoryCandidate candidate = memoryCandidateService.createCandidate(
                    userId,
                    contextConversationId,
                    candidateType,
                    candidateContent,
                    reason);

            return AgentToolResult.success(MemoryConverter.toCandidateResponse(candidate));
        } catch (Exception e) {
            log.error("[Tool error] createCandidateMemory", e);
            return AgentToolResult.fail(e);
        }
    }

    @Tool({
            "[WHAT] Delete a memory candidate by id.",
            "[WHEN] Use when a candidate is incorrect, outdated, duplicated, or no longer useful.",
            "[HOW] Provide the candidate id exactly as returned by listCandidateMemories."
    })
    public AgentToolResult deleteCandidateMemory(
            @P("Candidate id to delete") Long candidateId,
            InvocationParameters parameters) {
        try {
            Long userId = parameters.get(RequestContextConstant.USER_ID);
            if (Objects.isNull(userId)) {
                return AgentToolResult.noContext();
            }

            boolean deleted = memoryCandidateService.deleteCandidate(userId, candidateId);
            if (!deleted) {
                return AgentToolResult.empty();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("candidateId", candidateId);
            result.put("deleted", true);
            return AgentToolResult.success(result);
        } catch (Exception e) {
            log.error("[Tool error] deleteCandidateMemory", e);
            return AgentToolResult.fail(e);
        }
    }
}
