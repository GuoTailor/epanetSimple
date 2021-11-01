package nmka;

import nmka.model.OcPoint;

public class Repetition {
    public String code;
    public Long id;

    public String startCode;
    public Long startId;

    public String endCode;
    public Long endId;

    public Repetition() {
    }

    public Repetition(OcPoint startPoint, OcPoint endPoint, String code, Long id) {
        this.code = code;
        this.id = id;
        startCode = startPoint.getCode();
        startId = startPoint.getId();
        endCode = endPoint.getCode();
        endId = endPoint.getId();
    }
}
