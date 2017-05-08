package kostiskag.unitynetwork.tracker.functions;

import static org.junit.Assert.*;

import java.security.PrivateKey;
import java.security.PublicKey;

import kostiskag.unitynetwork.tracker.functions.CryptoMethods;

import org.junit.Test;

public class CryptoMethodsTest {

	@Test
	public void test() {
		String question = CryptoMethods.generateQuestion();
        System.out.println(question);

        //get your public key
        String publicKey = "-----BEGIN PUBLIC KEY-----\n"
                + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtk1cGKhieoVArJR6iY94\n"
                + "iABzPpFdVC3zy3mhHyJmR9m8qSnnrp+ip7n9Zy+ai5YIdd9agj+67hRjTeTGC/Ec\n"
                + "+x7zDDjnBBrlKYsyvdGfuKlBIrXgdl9uh4lnVkKHfECo9fnje6dVmZCK84qhUANB\n"
                + "AtRvkZFTByMQNxvBPOm/G8p6L6pOLBiiuhNuzHrSYG/AfGDTyIg4M7GAkU+uUYBv\n"
                + "e9TDY328vkzBASCdNOXC8vEi6vEIPk/yI2Hnlk07gofhx05cryhO6PbftPykdle+\n"
                + "DDT0xHvElW/MkQVX9rc0D0yJ9D2rj1K1m+lODQ8HaqX8fW5JSI6AMsWN2WFipmcJ\n"
                + "6QIDAQAB\n"
                + "-----END PUBLIC KEY-----";

        System.out.println(publicKey);
        PublicKey pubkey = CryptoMethods.getPublicKeyFromString(publicKey);

        //byte[] chiperedQuestion = CryptoMethods.RSAAuthenticateChallenge(question, pubkey);

        //now we reached the other side!!!
        String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n"
                + "MIIEowIBAAKCAQEAtk1cGKhieoVArJR6iY94iABzPpFdVC3zy3mhHyJmR9m8qSnn\n"
                + "rp+ip7n9Zy+ai5YIdd9agj+67hRjTeTGC/Ec+x7zDDjnBBrlKYsyvdGfuKlBIrXg\n"
                + "dl9uh4lnVkKHfECo9fnje6dVmZCK84qhUANBAtRvkZFTByMQNxvBPOm/G8p6L6pO\n"
                + "LBiiuhNuzHrSYG/AfGDTyIg4M7GAkU+uUYBve9TDY328vkzBASCdNOXC8vEi6vEI\n"
                + "Pk/yI2Hnlk07gofhx05cryhO6PbftPykdle+DDT0xHvElW/MkQVX9rc0D0yJ9D2r\n"
                + "j1K1m+lODQ8HaqX8fW5JSI6AMsWN2WFipmcJ6QIDAQABAoIBAQCogrulo0hcXn4Y\n"
                + "yKq4KyFL/baJWE8/t7ZKGGTh5adLtS3Z5H1fAfqVNavRzMP7UTUC1/HOweAloDzm\n"
                + "zJhwg3C5g7NAUfzg44d+rke6BGGyjOlDj4Erii0eJdmad6bLKO3FaTZon5XVfDGk\n"
                + "yzkvP8LBPeLfWMi5qSSc/A/UIXDg2uiYadFPswd+SdmaJgH59IEd2lfP+bHmSGBS\n"
                + "dD/YB9En9PTb4OSEIVfLL03ABwsxeV7IHv3hbcqZ31fhfhsdMhCkTjG7Tox1p0fD\n"
                + "UxnpfjLj5lsbYrJOIV8wUVoQQwkUzp/snMq0NFVoxZ2D5fkQ8KAS8PwNj4GXfcEl\n"
                + "M8U+cnP9AoGBAO0XkhpFT+THLFAMdoOFWW2/5pVHN3KuvmlZML3tueNqIVlKYNfc\n"
                + "EisKqLGTHtq6Sz8GTCQOXGsdMs+3U7HIdKe6snfEyrYO2JM/dIF/hITPUWSdFO56\n"
                + "cJ5ccdEDvFBhVu61bosoM/TONoDmfl3AI/xxwpANwExWkn9WdI1Z379/AoGBAMTX\n"
                + "NWI3DJijG7DL9fkp1rtTT2Y5EDnUGWmngR4nqNe0fFoXVKHBYbJasYHCZ+qsoRr8\n"
                + "+Tb2UwHKtHAbs2gmohZ4Tm78W7acIC1r8Ik+uU8PVkiBTdZZ7hkdg2mHt9k0R8v9\n"
                + "bWfkZGSmLpw1XttKfzlFKNXOnKlv4DWvgjMTHeqXAoGAAqMle+dTeS8B/i31T4c3\n"
                + "NHJTBUwSgNMSySc11JcFX1M55b1fEGehSBtJPxhs2nACEERoqmoCeyqK+yaF5s9d\n"
                + "BNSd0Zk9zAKkRBcLm7koZzXLKPxaVEDGaeyLU5DgEmDSz7ry7NdYpJt6nbpyo2ZU\n"
                + "wCUfzexpPDAmVwZGK6BZTc8CgYACLjgjLGTxU+08miXRass8LAIXKc6qNVVKvFZL\n"
                + "1TijmxY9kUCYwiGo7iRFQbgQ+3SVbfP8zeHBhVNWYpgsMTFeelq0FAuYDEa2+hki\n"
                + "DBXVcGAOUZBhLYHbuV35T02UFGYvNlF98yPBka22gUjZuQuLwN5g7/cAUYL0VUtl\n"
                + "8XJFZQKBgBIeSj9okLRLQmGaWlgEJRhlFeM1oI5OKfhG01mX/9q1Nk8r/42mXHhL\n"
                + "QYC+GgMG4+lhzsP82WeSbv08aYgA5lx0lJnJrA9aDx/OZGqMvLIyUmL8gbyJO7m2\n"
                + "zVEej4v/Jf/pKn1DiigFQphTuq8q2CsJtzxWD9FPVNQy+zrIkR2K\n"
                + "-----END RSA PRIVATE KEY-----";

        //make your private key usable            
        //PrivateKey privkey = CryptoMethods.getPrivateKeyFromString(privateKey);

        //decrypt the question            
        //String answer = new String(CryptoMethods.RSAAuthenticateResponce(chiperedQuestion, privkey));

        //THE MOMENT WE ALL WAITED FOR
        //System.out.println(answer);
        //System.out.println(question);
        //if (answer.equals(question)) {
        //    System.out.println("Match!!!!!!!!!");
        //}

	}

}
