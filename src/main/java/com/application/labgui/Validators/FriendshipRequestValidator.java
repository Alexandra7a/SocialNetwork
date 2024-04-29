package com.application.labgui.Validators;

import com.application.labgui.AppExceptions.ValidationException;
import com.application.labgui.Domain.FriendshipRequest;

public class FriendshipRequestValidator implements Validator<FriendshipRequest> {
    @Override
    public void validate(FriendshipRequest entity) throws ValidationException {
        String mesajEroare = "";
        if(entity.getId().getLeft() <= 0){
            mesajEroare += "ID utilizator 1 invalid!\n";
        }
        if(entity.getId().getRight() <= 0){
            mesajEroare += "ID utilizator 2 invalid!\n";
        }
        if(entity.getId().getRight().equals(entity.getId().getLeft())){
            mesajEroare += "Nu poti fi prieten cu tine insuti!\n";
        }
        if(!mesajEroare.isEmpty()){
            throw new ValidationException(mesajEroare);
        }
    }
}
