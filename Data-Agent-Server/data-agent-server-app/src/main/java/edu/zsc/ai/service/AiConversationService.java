package edu.zsc.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.zsc.ai.model.entity.AiConversation;
import edu.zsc.ai.model.dto.request.CreateConversationRequest;
import edu.zsc.ai.model.dto.request.ConversationListRequest;
import edu.zsc.ai.model.dto.request.DeleteConversationRequest;
import edu.zsc.ai.model.dto.request.GetConversationRequest;
import edu.zsc.ai.model.dto.request.UpdateConversationRequest;
import edu.zsc.ai.model.dto.response.ConversationResponse;
import edu.zsc.ai.model.dto.response.PageResponse;

/**
 * Service interface for ai_conversation operations
 *
 * @author zgq
 */
public interface AiConversationService extends IService<AiConversation> {

    /**
     * Create a new conversation
     *
     * @param request conversation creation request
     * @return created conversation response
     */
    ConversationResponse createConversation(CreateConversationRequest request);

    /**
     * Get conversation list with pagination
     *
     * @param request pagination and filter request
     * @return paginated conversation list
     */
    PageResponse<ConversationResponse> getConversationList(ConversationListRequest request);

    /**
     * Get conversation details by ID
     *
     * @param request get conversation request
     * @return conversation details
     */
    ConversationResponse getConversationById(GetConversationRequest request);

    /**
     * Update conversation
     *
     * @param request update request
     * @return updated conversation response
     */
    ConversationResponse updateConversation(UpdateConversationRequest request);

    /**
     * Delete conversation (soft delete)
     *
     * @param request delete request
     */
    void deleteConversation(DeleteConversationRequest request);
}