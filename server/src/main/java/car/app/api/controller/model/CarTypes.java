package car.app.api.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CarTypes {
    List<String> types;
}
