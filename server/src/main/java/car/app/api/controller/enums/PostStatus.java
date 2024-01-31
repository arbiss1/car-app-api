package car.app.api.controller.enums;

public enum PostStatus {
    PENDING,
    ACTIVE;

    public car.app.api.enums.PostStatus mapToStatus(){
        if(this == PostStatus.PENDING){
            return car.app.api.enums.PostStatus.PENDING;
        } else {
            return car.app.api.enums.PostStatus.ACTIVE;
        }
    }
}
