package car.app.api.controller.model;

import car.app.api.enums.Fuel;
import car.app.api.enums.PostType;
import car.app.api.enums.Transmission;
import lombok.Data;

@Data
public class SearchBuilderRequest {
    private PostType postType;
    private String type;
    private String brand;
    private String model;
    private String color;
    private Transmission transmission;
    private String kilometers;
    private Fuel fuel;
    private String power;
    private String lowestPrice;
    private String highestPrice;
    private String firstRegistration;
    private String engineSize;
    private String fromYear;
    private String toYear;
}
