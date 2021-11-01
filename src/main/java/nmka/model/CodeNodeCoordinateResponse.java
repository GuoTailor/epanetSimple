package nmka.model;


/**
 * Created by GYH on 2021/6/24
 */
public class CodeNodeCoordinateResponse implements CodeCoordinateResponse {
    private String code;
    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    public CodeNodeCoordinateResponse(String code, Double longitude, Double latitude) {
        this.code = code;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public CodeNodeCoordinateResponse() {
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
