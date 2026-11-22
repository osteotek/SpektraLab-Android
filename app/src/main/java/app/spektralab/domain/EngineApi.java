package app.spektralab.domain;


/** Engine operations used by the editor, independent of the concrete JNI class. */
public interface EngineApi {
    SessionApi openSession(long pixels, int width, int height, int channels);

    SessionApi openRawSession(String filePath, String paramsJson, int maxPixels, int maxLongEdge);

    long trimPool();

    String memoryReport();

    String deviceProfile();
}
