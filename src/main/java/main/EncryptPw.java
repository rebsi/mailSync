package main;

import encryption.Encryption;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class EncryptPw {
    public static void main(String[] args) throws Exception {
        System.out.print("Enter password: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String password = reader.readLine();

        String encryptedPw = Encryption.encrypt(password);
        System.out.println(encryptedPw);
    }
}