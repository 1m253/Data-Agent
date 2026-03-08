package edu.zsc.ai.agent.tool.ask;

import java.util.List;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import edu.zsc.ai.agent.tool.annotation.AgentTool;
import edu.zsc.ai.agent.tool.model.UserQuestion;
import lombok.extern.slf4j.Slf4j;

/**
 * Tool for asking the user structured clarification questions.
 * Available in both Agent and Plan modes.
 */
@AgentTool
@Slf4j
public class AskUserQuestionTool {

    @Tool(
            value = {
                    "Eliminates guesswork — one well-placed question saves multiple rounds of wrong ",
                    "SQL and frustrated users. Presents structured options so the user can choose with ",
                    "a single click instead of typing lengthy corrections.",
                    "",
                    "Call this generously whenever you face ambiguity: multiple candidate tables, ",
                    "unclear scope, conflicting interpretations, or missing constraints. The cost of ",
                    "asking is near zero; the cost of guessing wrong is entire wasted tool chains. ",
                    "Provide 2-3 concrete, actionable options per question."
            },
            returnBehavior = ReturnBehavior.IMMEDIATE
    )
    public String askUserQuestion(
            @P("List of questions to ask the user. Each question should have 2-3 options (maximum 3).")
            List<UserQuestion> questions) {

        int count = questions == null ? 0 : questions.size();
        log.info("[Tool] askUserQuestion, {} question(s)", count);
        return count + " question(s) presented to user.";
    }
}
