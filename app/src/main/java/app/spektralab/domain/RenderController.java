package app.spektralab.domain;

/** Applies a captured look and starts one frame on a session. */
public final class RenderController {
    private final RenderLookUseCase look = new RenderLookUseCase();
    public int[] begin(SessionApi session, EditorState state, String tier,
                       long longEdge, boolean reprint, RenderLookUseCase.Params params) {
        return look.render(session, state, tier, longEdge, reprint, params);
    }
}
