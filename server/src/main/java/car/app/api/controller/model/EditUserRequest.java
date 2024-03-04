package car.app.api.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EditUserRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String city;
    private String country;
    private String email;
    private String phoneNumber;
    private String address;
}
