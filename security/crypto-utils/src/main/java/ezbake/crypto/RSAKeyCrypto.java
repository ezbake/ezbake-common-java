/*   Copyright (C) 2013-2014 Computer Sciences Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package ezbake.crypto;

import ezbake.crypto.utils.CryptoUtil;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.*;

/**
 * User: jhastings
 * Date: 10/9/13
 * Time: 1:03 PM
 */
public class RSAKeyCrypto implements PKeyCrypto {
    private static Logger log = LoggerFactory.getLogger(RSAKeyCrypto.class);
    private static final String keyAlgorithm = "RSA";
    private static final String algorithmEncryptionString = "withRSA";

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String algorithm = "SHA256"+algorithmEncryptionString;

    static {
        Security.addProvider(new BouncyCastleProvider());
        if (log.isTraceEnabled()) {
            for (Provider p : Security.getProviders()) {
                log.trace("Provider: {}", p.getName());
                log.trace("{}", p.getServices());
            }
        }
    }

    /**
     * Default Constructor;
     */
    public RSAKeyCrypto() {
       generatePrivatePublicKeyPair();
    }
    
    public RSAKeyCrypto(String key, boolean isPrivate) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this(CryptoUtil.der(key), isPrivate);
    }
    public RSAKeyCrypto(byte[] key, boolean isPrivate) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec keySpec;

        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(keyAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
        } catch (NoSuchProviderException e) {
            log.info("Unable to use the bouncycastle provider for RSA key factory. using default");
            keyFactory = KeyFactory.getInstance(keyAlgorithm);
        }

        if (isPrivate) {
            keySpec = new PKCS8EncodedKeySpec(key);
            this.privateKey = keyFactory.generatePrivate(keySpec);
            
            RSAPrivateCrtKey rpk = (RSAPrivateCrtKey)privateKey;
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(rpk.getModulus(), rpk.getPublicExponent());

            try {
                this.publicKey = keyFactory.generatePublic(publicKeySpec);
            } catch (Exception e) {
                log.error("ERROR: {}", e);
            }
        } else {
            try {
                keySpec = new X509EncodedKeySpec(key);
                this.publicKey = keyFactory.generatePublic(keySpec);
            } catch (InvalidKeySpecException e) {
                this.publicKey = fixPubKey(key);
            }
        }
    }

    public RSAKeyCrypto(String privateKey, String publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this(CryptoUtil.der(privateKey), CryptoUtil.der(publicKey));
    }
    public RSAKeyCrypto(byte[] privateKey, byte[] publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(keyAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
        } catch (NoSuchProviderException e) {
            log.info("Unable to use the bouncycastle provider for RSA key factory. using default");
            keyFactory = KeyFactory.getInstance(keyAlgorithm);
        }

        KeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
        this.privateKey = keyFactory.generatePrivate(keySpec);

        if (publicKey != null) {
            try {
                keySpec = new X509EncodedKeySpec(publicKey);
                this.publicKey = keyFactory.generatePublic(keySpec);
            } catch (InvalidKeySpecException e) {
                this.publicKey = fixPubKey(publicKey);
            } catch (IllegalArgumentException e) {
                this.publicKey = fixPubKey(publicKey);
            }
        }
    }

    private static PublicKey fixPubKey(byte[] orig) throws InvalidKeySpecException, NoSuchAlgorithmException {
        try {
            ASN1InputStream in = new ASN1InputStream(orig);

            ASN1Primitive obj = in.readObject();
            RSAPublicKeyStructure keyStruct = RSAPublicKeyStructure.getInstance(obj);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(keyStruct.getModulus(), keyStruct.getPublicExponent());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (IOException e) {
            throw new InvalidKeySpecException("Unable to load public key with stopgap ASN1 method", e);
        }
    }

    public static String getPublicFromPrivatePEM(String privateKeyPEM) {
        String value = "";

        try {
            RSAKeyCrypto crypto = new RSAKeyCrypto(privateKeyPEM, true);
            value = crypto.getPublicPEM();
        } catch(NoSuchAlgorithmException e) {
            log.error("Unsupported algorithm", e);
        } catch (InvalidKeySpecException e) {
            log.error("Invalid key", e);
        }

        return value;
    }

    public boolean hasPrivate() {
        return this.privateKey != null;
    }

    public boolean hasPublic() {
        return this.publicKey != null;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }
    public String getPublicPEM() {
        return getPEM(this.publicKey);
    }
    
    public String getPrivatePEM() {
        return getPEM(this.privateKey);
    }

    private String getPEM(Key key) {
        StringWriter sw = new StringWriter(1024);
        PEMWriter pw = new PEMWriter(sw);

        try {
            pw.writeObject(key);
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sw.getBuffer().toString();
    }

    public boolean verify(byte[] data, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance(this.algorithm);
        rsa.initVerify(publicKey);
        rsa.update(data);
        return rsa.verify(signature);
    }

    public byte[] sign(byte[] data) throws PKeyCryptoException {
        if (privateKey == null) {
            throw new PKeyCryptoException("Cannot sign without private key");
        }
        byte[] signed;

        try {
            Signature rsa = Signature.getInstance(this.algorithm);
            rsa.initSign(privateKey);
            rsa.update(data);
            signed = rsa.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new PKeyCryptoException("Unable to sign with algorithm: " + this.algorithm, e);
        } catch (InvalidKeyException e) {
            throw new PKeyCryptoException("Unable to sign. Invalid private key", e);
        } catch (SignatureException e) {
            throw new PKeyCryptoException("Unable to sign. Unknown signature exception" + e.toString(), e);
        }

        return signed;
    }
    
    
    public byte[] encrypt(byte[] data) throws PKeyCryptoException {
        byte[] encrypted = null;
        log.debug("encrypt\ndata:" + data);
        Cipher cipher = null;
        try
        {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            log.debug("Using Algorithm " + cipher.getAlgorithm());
            log.debug("Block Size: " + cipher.getBlockSize());

            cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
            encrypted = cipher.doFinal(data);

        }
        catch(NoSuchPaddingException e) {
            throw new PKeyCryptoException("Unable to encrypt with padding" +e ,e);
        }
        catch(NoSuchAlgorithmException e) {
            throw new PKeyCryptoException("Unable to encrypt with algorithm: " + e, e);
        }
        catch(InvalidKeyException e) {
            throw new PKeyCryptoException("Unable to encrypt with key" + e, e);
        }
        catch(BadPaddingException e) {
            throw new PKeyCryptoException("Unable to encrypt with padding" + e, e);
        }
        catch(IllegalBlockSizeException e) {
            throw new PKeyCryptoException("Unable to encrypt with block size" + e.toString(), e);
        }


        return encrypted;
    }
    
    public byte[] decrypt(byte[] cipherData) throws PKeyCryptoException {
        byte[] data = null;
        Cipher cipher = null;

        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
            data = cipher.doFinal(cipherData);
        }
        catch(NoSuchPaddingException e) {
            throw new PKeyCryptoException("Unable to decrypt with Padding. " + e, e);
        }
        catch(NoSuchAlgorithmException e) {
            throw new PKeyCryptoException("Unable to decrypt with Algorithm " + e, e);
        }
        catch(InvalidKeyException e) {
            throw new PKeyCryptoException("Unable to decrypt with Key " + e, e);
        }
        catch(BadPaddingException e) {
            throw new PKeyCryptoException("Unable to decrypt with Padding" + e, e);
        }
        catch(IllegalBlockSizeException e) {
            throw new PKeyCryptoException("Unable to decrypt with Block size" + e, e);
        }
        finally {

        }

        return data;
    }
    
    public String getCSR(String dn) {
       return generatePKCS10(dn);
    }
    
    /**
     * Description: Generate the private key when the default constructor is used.
     */
    private void generatePrivatePublicKeyPair() {
       try {
          KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
          RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
          keyGen.initialize(spec);
          KeyPair keyPair = keyGen.generateKeyPair();
          this.privateKey = keyPair.getPrivate();
          this.publicKey = keyPair.getPublic();
       }
       catch(NoSuchAlgorithmException e) {
          log.error("Error: " + e);
       } catch (InvalidAlgorithmParameterException e) {
         log.error("Error: " + e);
      }
       
    }
    


   private String generatePKCS10(String dn) {
      String pem = null;
      X500Principal x500p = new X500Principal(dn);
      PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(x500p, this.publicKey);
      
      JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(algorithm);
      
      try {
         ContentSigner signer = csBuilder.build(this.privateKey);
         org.bouncycastle.pkcs.PKCS10CertificationRequest csr = p10Builder.build(signer);
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         PrintWriter writer= new PrintWriter(baos);
         
         PEMWriter out = new PEMWriter(writer);
         out.writeObject(csr);
         out.close();
         
         pem = new String(baos.toByteArray());
         
      } catch (OperatorCreationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      return pem;
      
   }
  
}