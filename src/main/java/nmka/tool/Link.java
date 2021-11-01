package nmka.tool;

import nmka.model.OcPipe;
import nmka.model.OcPump;
import nmka.model.OcValve;

/**
 * Created by GYH on 2021/9/10
 */

public class Link {
    public String code;
    public String startCode;
    public String endCode;
    public Double startLongitude;
    public Double startLatitude;
    public Double endLongitude;
    public Double endLatitude;
    public String state = "0";

    public Link() {
    }

    public Link(OcPipe pipe) {
        code = pipe.getCode();
        startCode = pipe.getStartNodeCode();
        endCode = pipe.getEndNodeCode();
        startLongitude = pipe.getStartLongitude();
        startLatitude = pipe.getStartLatitude();
        endLongitude = pipe.getEndLongitude();
        endLatitude = pipe.getEndLatitude();
    }

    public Link(OcPump pump) {
        code = pump.getCode();
        startCode = pump.getStartNodeCode();
        endCode = pump.getEndNodeCode();
        startLongitude = pump.getStartLongitude();
        startLatitude = pump.getStartLatitude();
        endLongitude = pump.getEndLongitude();
        endLatitude = pump.getEndLatitude();
    }

    public Link(OcValve valve) {
        code = valve.getCode();
        startCode = valve.getStartNodeCode();
        endCode = valve.getEndNodeCode();
        startLongitude = valve.getStartLongitude();
        startLatitude = valve.getStartLatitude();
        endLongitude = valve.getEndLongitude();
        endLatitude = valve.getEndLatitude();
    }
}
