package app.spektralab.ui;

/** One photograph hanging in the light-table room. */
    public final class Plane {
        int index;          // into `album`
        float t;            // where along the loop
        float lateral;      // across the path
        float depth;        // along z
        float width;        // in world units
        float scale = 1f;   // the eased focus scale
        float block = 1f;   // 0 when it would cover the station

        Plane(int index, float t, float lateral, float depth, float width) {
            this.index = index;
            this.t = t;
            this.lateral = lateral;
            this.depth = depth;
            this.width = width;
        }
    }
