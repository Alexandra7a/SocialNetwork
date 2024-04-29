package com.application.labgui.Validators;

import com.application.labgui.AppExceptions.ValidationException;
import com.application.labgui.Domain.User;

public class UserValidator implements Validator<User>{
    @Override
    public void validate(User entity) throws ValidationException {
        String mesajEroare = "";
        if(entity.getFirstName().isEmpty()){
            mesajEroare += "Prenume invalid!\n";
        }
        if(entity.getLastName().isEmpty()){
            mesajEroare += "Nume invalid!\n";
        }
        if(!mesajEroare.isEmpty()){
            throw new ValidationException(mesajEroare);
        }
    }
}
