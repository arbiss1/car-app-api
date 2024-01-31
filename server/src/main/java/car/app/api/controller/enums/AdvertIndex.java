package car.app.api.controller.enums;


public enum AdvertIndex {
    FREE,
    MEDIUM,
    HIGH;

    public car.app.api.enums.AdvertIndex mapToStatus(){
        if(this == AdvertIndex.FREE){
            return car.app.api.enums.AdvertIndex.FREE;
        } else if(this == AdvertIndex.HIGH){
            return car.app.api.enums.AdvertIndex.HIGH;
        } else {
            return car.app.api.enums.AdvertIndex.MEDIUM;
        }
    }
}