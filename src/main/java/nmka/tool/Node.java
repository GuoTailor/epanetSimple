package nmka.tool;

import nmka.model.OcPoint;
import nmka.model.OcPool;
import nmka.model.OcReservoir;

/**
 * Created by GYH on 2021/9/10
 */
public class Node {
    public String code;
    public Double longitude;
    public Double latitude;
    public Object target;

    public Node() {
    }

    public Node(OcPoint point) {
        code = point.getCode();
        longitude = point.getLongitude();
        latitude = point.getLatitude();
        target = point;
    }

    public Node(OcPool pool) {
        code = pool.getCode();
        longitude = pool.getLongitude();
        latitude = pool.getLatitude();
        target = pool;
    }

    public Node(OcReservoir reservoir) {
        code = reservoir.getCode();
        longitude = reservoir.getLongitude();
        latitude = reservoir.getLatitude();
        target = reservoir;
    }

    public Node(String code, Double longitude, Double latitude) {
        this.code = code;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
