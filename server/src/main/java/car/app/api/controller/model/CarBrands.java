package car.app.api.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CarBrands {
    List<String> brands;
}
