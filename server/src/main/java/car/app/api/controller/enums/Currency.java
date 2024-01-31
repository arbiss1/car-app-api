package car.app.api.controller.enums;

public enum Currency{
    ALL,
    EUR,
    USD,
    GBP;

    public car.app.api.enums.Currency mapToStatus(){
        if(this == Currency.ALL){
            return car.app.api.enums.Currency.ALL;
        } else if(this == Currency.EUR){
            return car.app.api.enums.Currency.EUR;
        } else if(this == Currency.USD){
            return car.app.api.enums.Currency.USD;
        }else {
            return car.app.api.enums.Currency.GBP;
        }
    }
}