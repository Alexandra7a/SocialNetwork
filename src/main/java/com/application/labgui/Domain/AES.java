package com.application.labgui.Domain;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;


/**This class follows the singleton pattern design to be able to provide teh same secretKey
 * for multiple places where it is needed...*/
public class AES {
    private static AES instance = null;
    private static final String ALGORITHM = "AES";
    private static final byte[] keyValue = "1234567890123456".getBytes();
    //private static final byte[] keyValue2 = System.getenv("SECRET_KEY").getBytes();
    private Key secretKey;


    private AES() throws Exception {
        secretKey=generateKey();
    }
    public static AES getInstance() throws Exception {
        if(instance==null)
            instance= new AES();
        return instance;
    }

    public Key generateKey() throws Exception {

        // System.out.println(Base64.getEncoder().encodeToString(System.getenv("SECRET_KEY").getBytes()));
        //System.out.println(Base64.getEncoder().encodeToString(System.getenv("SECRET_KEY").getBytes(StandardCharsets.US_ASCII)));
        //System.out.println(Base64.getEncoder().encodeToString(keyValue));
        return new SecretKeySpec(keyValue, ALGORITHM);
    }

   public byte[] encryptMessage(byte[] message) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException
   {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            // daca size-ul mesajului e mai mare de 128/192/256 bits atunci el va fi dispersat in mai multe blocuri
            // daca mesajul despartit in mai multe blocuri nu se incadreaza la fix in blocuri, atunci se va face padding
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(message);
   }

    /*public  String decrypt(String encryptedValue, Key key) throws Exception {
        // Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decodedBytes = new Base64().decode(encryptedValue.getBytes());

        byte[] enctVal = cipher.doFinal(decodedBytes);

        System.out.println("Decrypted Value :: " + new String(enctVal));
        return new String(enctVal);
    }*/

}
