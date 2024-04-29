package com.application.labgui.Validators;

public class FactoryValidator {
    private static FactoryValidator factoryValidator;
    private FactoryValidator(){

    }
    public static FactoryValidator getFactoryInstance(){
        if(factoryValidator == null){
            factoryValidator = new FactoryValidator();
        }
        return factoryValidator;
    }

    public Validator createValidator(ValidatorStrategies strategies){
        switch (strategies){
            case UTILIZATOR -> {
                return new UserValidator();
            }
            case PRIETENIE -> {
                return new FriendshipValidator();
            }
            case CEREREPRIETENIE -> {
                return new FriendshipRequestValidator();
            }
            default ->
            {
                return null;
            }
        }
    }
}
