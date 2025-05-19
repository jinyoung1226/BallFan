package BallFan.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceInfo {

    private String title;
    private String roadAddress;
    private String longitude;
    private String latitude;
    private String map_url;

}
