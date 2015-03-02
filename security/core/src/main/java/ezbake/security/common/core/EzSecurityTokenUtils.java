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

package ezbake.security.common.core;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import ezbake.base.thrift.*;
import ezbake.crypto.PKeyCrypto;
import ezbake.crypto.PKeyCryptoException;
import ezbake.crypto.RSAKeyCrypto;
import ezbake.crypto.utils.CryptoUtil;
import ezbake.security.thrift.EzSecurity;
import ezbake.security.thrift.ProxyTokenRequest;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: jhastings
 * Date: 11/11/13
 * Time: 11:28 AM
 */
public class EzSecurityTokenUtils {
    private static Logger log = LoggerFactory.getLogger(EzSecurityTokenUtils.class);

    public static boolean isEzAdmin(EzSecurityToken token) {
        boolean admin = false;
        if (token.getExternalProjectGroups() != null) {
            List<String> groups = token.getExternalProjectGroups().get(EzSecurityConstant.EZ_INTERNAL_PROJECT);
            if (groups != null) {
                admin = groups.contains(EzSecurityConstant.EZ_INTERNAL_ADMIN_GROUP);
            }
        }
        return admin;
    }

    public static EzSecurityToken freshToken(String securityId, TokenType tokenType, long notAfter) {
        EzSecurityToken token = new EzSecurityToken(
                generateValidityCaveats("EzSecurity", securityId, notAfter),
                tokenType,
                null);
        token.setAuthorizations(new ezbake.base.thrift.Authorizations());
        return token;
    }

    public static ValidityCaveats generateValidityCaveats(String securityId, String targetSecurityId, long notAfter) {
        ValidityCaveats caveat = new ValidityCaveats(SecurityID.ReservedSecurityId.EzSecurity.getCn(), securityId,
                notAfter, null);
        caveat.setIssuedTime(System.currentTimeMillis());
        caveat.setIssuedFor(targetSecurityId);

        return caveat;
    }


    public static String tokenRequestSignature(final TokenRequest request, final PKeyCrypto signer) throws IOException, PKeyCryptoException {
        return CryptoUtil.encode(signer.sign(EzSecurityTokenUtils.serializeTokenRequest(request)));
    }

    public static boolean verifyTokenRequestSignature(final TokenRequest request, final String signature, final PKeyCrypto verifier) {
        boolean verifies = false;

        try {
            verifies = verifier.verify(serializeTokenRequest(request), CryptoUtil.decode(signature));
        } catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm supported for verifying token signature", e);
        } catch (InvalidKeyException e) {
            log.error("Invalid key used to verify token signature", e);
        } catch (SignatureException e) {
            log.error("Exception verifying signature", e);
        } catch (IOException e) {
            log.error("Unable to serialize provided token response, therefore unable to verify signature", e);
        }

        return verifies;
    }


    public static byte[] serializeProxyTokenRequest(final ProxyTokenRequest request) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(baos);

        putStringToOutputStream(dataOutputStream, request.getX509().getSubject(), EzSecurityConstant.CHARSET);
        if (request.getX509().getIssuer() != null) {
            putStringToOutputStream(dataOutputStream, request.getX509().getIssuer(), EzSecurityConstant.CHARSET);
        }
        putStringToOutputStream(dataOutputStream, request.getValidity().getIssuer(), EzSecurityConstant.CHARSET);
        putStringToOutputStream(dataOutputStream, request.getValidity().getIssuedTo(), EzSecurityConstant.CHARSET);
        if (request.getValidity().getIssuedFor() != null) {
            putStringToOutputStream(dataOutputStream, request.getValidity().getIssuedFor(), EzSecurityConstant.CHARSET);
        }
        if (request.getValidity().getIssuedTime() > 0) {
            putStringToOutputStream(dataOutputStream, String.valueOf(request.getValidity().getIssuedTime()), EzSecurityConstant.CHARSET);
        }
        if (request.getValidity().getNotBefore() > 0) {
            putStringToOutputStream(dataOutputStream, String.valueOf(request.getValidity().getNotBefore()), EzSecurityConstant.CHARSET);
        }
        putStringToOutputStream(dataOutputStream, String.valueOf(request.getValidity().getNotAfter()), EzSecurityConstant.CHARSET);

        dataOutputStream.flush();
        return baos.toByteArray();

    }
    public static String proxyTokenRequestSignature(final ProxyTokenRequest request, final PKeyCrypto signer) throws PKeyCryptoException {
        try {
            return CryptoUtil.encode(signer.sign(serializeProxyTokenRequest(request)));
        } catch (IOException e) {
            throw new PKeyCryptoException("Unable to serialize proxy token request: " + e.getMessage());
        }
    }

    public static boolean verifyProxyTokenRequest(final ProxyTokenRequest request, final PKeyCrypto verifier) {
        boolean verifies = false;
        try {
            String signature = request.getValidity().getSignature();
            verifies = verifier.verify(serializeProxyTokenRequest(request), CryptoUtil.decode(signature));
        } catch (IOException e) {
            log.error("Unable to serialize the token to a JSON string");
        } catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm supported for verifying token signature", e);
        } catch (InvalidKeyException e) {
            log.error("Invalid key used to verify token signature", e);
        } catch (SignatureException e) {
            log.error("Exception verifying signature", e);
        }
        return verifies;
    }

    public static String proxyUserTokenSignature(final ProxyUserToken token, final PKeyCrypto signer) throws PKeyCryptoException {
        try {
            String json = serializeProxyUserTokenToJSON(token);
            return CryptoUtil.encode(signer.sign(json.getBytes()));
        } catch (TException e) {
            throw new PKeyCryptoException("Unable to serialize the token to a JSON string");
        } catch (PKeyCryptoException e) {
            throw new PKeyCryptoException("Unable to sign ProxyUserToken");
        }
    }

    public static boolean verifyProxyUserToken(String token, String signature, PKeyCrypto verifier) {
        boolean verifies = false;
        try {
            verifies = verifier.verify(token.getBytes(), CryptoUtil.decode(signature));
        } catch (Exception e) {
            log.error("Unable to verify ProxyUserToken");
        }
        return verifies;
    }
    public static boolean verifyProxyUserToken(ProxyUserToken token, String signature, PKeyCrypto verifier) {
        try {
            return verifyProxyUserToken(serializeProxyUserTokenToJSON(token), signature, verifier);
        } catch (TException e) {
            log.error("Unable to verify ProxyUserToken - could not serialize to json");
            return false;
        }
    }

    public static String serializeProxyUserTokenToJSON(ProxyUserToken token) throws TException {
        try {
            return new String(new TSerializer(new TSimpleJSONProtocol.Factory()).serialize(token), EzSecurityConstant.CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new TException("Unable to encode " + EzSecurityConstant.CHARSET + " string");
        }
    }
    public static ProxyUserToken deserializeProxyUserToken(String tokenString) {
        Gson gson = new Gson();
        return gson.fromJson(tokenString, ProxyUserToken.class);
    }


    public static String tokenSignature(final EzSecurityToken token, final PKeyCrypto signer)
            throws IOException, PKeyCryptoException {
        return CryptoUtil.encode(signer.sign(EzSecurityTokenUtils.serializeToken(token)));
    }

    public static boolean verifyTokenSignature(final EzSecurityToken token, final PKeyCrypto verifier) {
        boolean verifies = false;

        try {
            verifies = verifier.verify(serializeToken(token), CryptoUtil.decode(token.getValidity().getSignature()));
        } catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm supported for verifying token signature", e);
        } catch (InvalidKeyException e) {
            log.error("Invalid key used to verify token signature", e);
        } catch (SignatureException e) {
            log.error("Exception verifying signature", e);
        } catch (IOException e) {
            log.error("Unable to serialize provided token response, therefore unable to verify signature", e);
        }

        return verifies;
    }

    public static boolean verifyPrincipalSignature(final EzSecurityPrincipal principal, final PKeyCrypto verifier) {
        boolean verifies = false;

        try {
            verifies = verifier.verify(serializePrincipal(principal), CryptoUtil.decode(principal.getValidity().getSignature()));
        } catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm supported for verifying token signature", e);
        } catch (InvalidKeyException e) {
            log.error("Invalid key used to verify token signature", e);
        } catch (SignatureException e) {
            log.error("Exception verifying signature", e);
        } catch (IOException e) {
            log.error("Unable to serialize provided token response, therefore unable to verify signature", e);
        }
        return verifies;
    }

    public static byte[] serializeTokenRequest(final TokenRequest request) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(baos);

        if (request.getProxyPrincipal() != null) {
            dataOutputStream.write(request.getProxyPrincipal().getProxyToken().getBytes(StandardCharsets.UTF_8));
            dataOutputStream.write(request.getProxyPrincipal().getSignature().getBytes(StandardCharsets.UTF_8));
        } else if (request.getTokenPrincipal() != null) {
            dataOutputStream.write(serializeToken(request.getTokenPrincipal()));
        }

        putStringToOutputStream(dataOutputStream, request.getType().toString(), EzSecurityConstant.CHARSET);
        putStringToOutputStream(dataOutputStream, request.getSecurityId(), EzSecurityConstant.CHARSET);
        putStringToOutputStream(dataOutputStream, request.getTargetSecurityId(), EzSecurityConstant.CHARSET);
        putStringToOutputStream(dataOutputStream, String.valueOf(request.getTimestamp()), EzSecurityConstant.CHARSET);

        if (request.isSetExcludeAuthorizations() && request.getExcludeAuthorizations() != null) {
            Set<String> sorted = Sets.newTreeSet(request.getExcludeAuthorizations());
            for (String s : sorted) {
                dataOutputStream.write(s.getBytes(StandardCharsets.UTF_8));
            }
        }

        dataOutputStream.flush();
        return baos.toByteArray();
    }


    public static byte[] serializeToken(final EzSecurityToken token) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(baos);

        // Validity caveats - who, what, when
        putStringToOutputStream(dataOutputStream, token.getValidity().getIssuedTo(), EzSecurityConstant.CHARSET);
        if (token.getValidity().getIssuedFor() != null && !token.getValidity().getIssuedFor().isEmpty()) {
            putStringToOutputStream(dataOutputStream, token.getValidity().getIssuedFor(), EzSecurityConstant.CHARSET);
        }
        putStringToOutputStream(dataOutputStream, String.valueOf(token.getValidity().getNotAfter()), EzSecurityConstant.CHARSET);
        putStringToOutputStream(dataOutputStream, String.valueOf(token.getValidity().getNotBefore()), EzSecurityConstant.CHARSET);
        putStringToOutputStream(dataOutputStream, String.valueOf(token.getValidity().getIssuedTime()), EzSecurityConstant.CHARSET);

        // Token Type
        putStringToOutputStream(dataOutputStream, token.getType().toString(), EzSecurityConstant.CHARSET);

        // Principal - just the identifying information. The principal has it's own signature that should verify the rest
        putStringToOutputStream(dataOutputStream, token.getTokenPrincipal().getPrincipal(), EzSecurityConstant.CHARSET);
        if (token.getTokenPrincipal().getIssuer() != null) {
            dataOutputStream.write(token.getTokenPrincipal().getIssuer().getBytes(StandardCharsets.UTF_8));
        }
        if (token.getTokenPrincipal().getRequestChain() != null) {
            for (String chain : token.getTokenPrincipal().getRequestChain()) {
                putStringToOutputStream(dataOutputStream, chain, EzSecurityConstant.CHARSET);
            }
        }

        // Authorizations
        if (token.getAuthorizationLevel() != null && !token.getAuthorizationLevel().isEmpty()) {
            putStringToOutputStream(dataOutputStream, token.getAuthorizationLevel(), EzSecurityConstant.CHARSET);
        }
        if (token.getAuthorizations() != null) {
            if (token.getAuthorizations().getFormalAuthorizations() != null) {
                Set<String> auths = ImmutableSortedSet.copyOf(token.getAuthorizations().getFormalAuthorizations());
                for (String auth : auths) {
                    putStringToOutputStream(dataOutputStream, auth, EzSecurityConstant.CHARSET);
                }
            }
            if (token.getAuthorizations().getExternalCommunityAuthorizations() != null) {
                Set<String> auths = ImmutableSortedSet.copyOf(token.getAuthorizations().getExternalCommunityAuthorizations());
                for (String auth : auths) {
                    putStringToOutputStream(dataOutputStream, auth, EzSecurityConstant.CHARSET);
                }
            }
            if (token.getAuthorizations().getPlatformObjectAuthorizations() != null) {
                Set<Long> auths = ImmutableSortedSet.copyOf(token.getAuthorizations().getPlatformObjectAuthorizations());
                for (Long auth : auths) {
                    putStringToOutputStream(dataOutputStream, Long.toString(auth), EzSecurityConstant.CHARSET);
                }
            }
        }

        if (token.getExternalProjectGroups() != null) {
            token.setExternalProjectGroups(ImmutableSortedMap.copyOf(token.getExternalProjectGroups()));
            for (Map.Entry<String, List<String>> project : token.getExternalProjectGroups().entrySet()) {
                putStringToOutputStream(dataOutputStream, project.getKey(), EzSecurityConstant.CHARSET);
                Collections.sort(project.getValue());
                for (String group : project.getValue()) {
                    putStringToOutputStream(dataOutputStream, group, EzSecurityConstant.CHARSET);
                }
            }
        }

        if (token.getExternalCommunities() != null) {
            for (Map.Entry<String, CommunityMembership> entry : token.getExternalCommunities().entrySet()) {
                CommunityMembership community = entry.getValue();
                putStringToOutputStream(dataOutputStream, community.getName(), EzSecurityConstant.CHARSET);
                putStringToOutputStream(dataOutputStream, community.getType(), EzSecurityConstant.CHARSET);
                putStringToOutputStream(dataOutputStream, community.getOrganization(), EzSecurityConstant.CHARSET);
                Collections.sort(community.getGroups());
                for (String groups : community.getGroups()) {
                    putStringToOutputStream(dataOutputStream, groups, EzSecurityConstant.CHARSET);
                }
                Collections.sort(community.getTopics());
                for (String topics : community.getTopics()) {
                    putStringToOutputStream(dataOutputStream, topics, EzSecurityConstant.CHARSET);
                }
                Collections.sort(community.getRegions());
                for (String regions : community.getRegions()) {
                    putStringToOutputStream(dataOutputStream, regions, EzSecurityConstant.CHARSET);
                }
                if (entry.getValue().getFlags() != null) {
                    Map<String, Boolean> flags = ImmutableSortedMap.copyOf(entry.getValue().getFlags());
                    for (Map.Entry<String, Boolean> fentry : flags.entrySet()) {
                        dataOutputStream.write(fentry.getKey().getBytes(StandardCharsets.UTF_8));
                        dataOutputStream.write(Boolean.toString(fentry.getValue()).getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        }

        putStringToOutputStream(dataOutputStream, String.valueOf(token.isValidForExternalRequest()), EzSecurityConstant.CHARSET);
        putStringToOutputStream(dataOutputStream, token.getCitizenship(), EzSecurityConstant.CHARSET);
        putStringToOutputStream(dataOutputStream, token.getOrganization(), EzSecurityConstant.CHARSET);

        dataOutputStream.flush();
        return baos.toByteArray();
    }

    public static String principalSignature(final EzSecurityPrincipal principal, final PKeyCrypto signer)
            throws IOException, PKeyCryptoException {
        return CryptoUtil.encode(signer.sign(EzSecurityTokenUtils.serializePrincipal(principal)));
    }
    
    public static byte[] serializePrincipal(final EzSecurityPrincipal principal) throws IOException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	DataOutputStream dos = new DataOutputStream(baos);
    	
        putStringToOutputStream(dos, principal.getPrincipal(), EzSecurityConstant.CHARSET);
        putStringToOutputStream(dos, String.valueOf(principal.getValidity().getNotAfter()), EzSecurityConstant.CHARSET);
    	
    	if(principal.getRequestChain() != null) {
    		for(String request : principal.getRequestChain()) {
    			putStringToOutputStream(dos, request, EzSecurityConstant.CHARSET);
    		}
    	}
    	 
    	dos.flush();
    	return baos.toByteArray();
    }
    
    private static void putStringToOutputStream(final OutputStream os, String value, String charset) throws IOException {
        if (value != null) {
            try {
                os.write(value.getBytes(charset));
            } catch (UnsupportedEncodingException e) {
                os.write(value.getBytes());
            }
        }
    }
    private static void putStringToByteBuffer(final ByteBuffer bb, String value, String charset) {
        if (value != null) {
            try {
                bb.put(value.getBytes(charset));
            } catch (UnsupportedEncodingException e) {
                bb.put(value.getBytes());
            }
        }
    }

    /**
     * Verify that a received EzSecurityToken was delivered to the correct application, verifying the token as well
     *
     * @param publicKey public key to verify signature
     * @param token response to verify
     * @param id application ID of the recipient
     * @return true if the token is valid
     */
    public static void verifyReceivedToken(final PKeyCrypto publicKey, final EzSecurityToken token, final String id)
            throws EzSecurityTokenException, TokenExpiredException {
        /**
         * Do faster checks first (app id, expired), then check the signature (potential bottle-neck) then
         * check the other things again (make sure token wasn't tampered with)
         */
        try {
            token.validate();
        } catch (TException e) {
            log.error("Received EzSecurityToken thrift failed to validate: {}", e.getMessage());
            throw new EzSecurityTokenException("Token failed to validate: "+e.getMessage());
        }

        // check the recipient app in the token
        if (token.getValidity().getIssuedFor() == null) {
            log.error("Received EzSecurityToken had no 'issuedFor' field");
            throw new EzSecurityTokenException("Received token was not 'issuedFor' any application");
        } else if (!token.getValidity().getIssuedFor().equals(id)) {
            log.error("Received EzSecurityToken was not 'issuedFor' this application. Expected {} was {}", id,
                    token.getValidity().getIssuedFor());
            throw new EzSecurityTokenException("Received token was not 'issuedFor' this application. Expected " + id +
                    "was " + token.getValidity().getIssuedFor());
        }

        // Check that token not expired
        long now = System.currentTimeMillis();
        if (token.getValidity().getNotAfter() <= now) {
            log.error("Received EzSecurityToken was expired. {} <= {}", token.getValidity().getNotAfter(), now);
            throw new TokenExpiredException("Received an expired token. " + token.getValidity().getNotAfter() +
                    " <= " + now);
        }

        // Check that token is valid
        if (!verifyTokenSignature(token, publicKey)) {
            log.error("Received EzSecurityToken signature failed to verify");
            throw new EzSecurityTokenException("Received EzSecurityToken signature did not validate");
        }


        // Again, check the recipient app
        if (!token.getValidity().getIssuedFor().equals(id)) {
            log.error("Received EzSecurityToken was not 'issuedFor' this application. Expected {} was {}", id,
                    token.getValidity().getIssuedFor());
            throw new EzSecurityTokenException("Received token was not 'issuedFor' this application. Expected " + id +
                    "was " + token.getValidity().getIssuedFor());
        }

        // Again, check expired
        now = System.currentTimeMillis();
        if (token.getValidity().getNotAfter() <= now) {
            log.error("Received EzSecurityToken was expired. {} <= {}", token.getValidity().getNotAfter(), now);
            throw new TokenExpiredException("Received an expired token. " + token.getValidity().getNotAfter() +
                    " <= " + now);
        }

    }

    /**
     * Verify a base64 encoded signature given a public key
     *
     * @param publicKey public key to verify signature
     * @param data raw data expected in signature
     * @param base64EncodedSignature base64 encoded signature
     * @return true if the signature is valid
     */
    protected static boolean verify(final String publicKey, final byte[] data, final String base64EncodedSignature) {
        boolean verifies = false;

        try {
            RSAKeyCrypto crypto = new RSAKeyCrypto(publicKey, false);
            verifies = crypto.verify(data, CryptoUtil.decode(base64EncodedSignature));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            log.info("Exception verifying signature {}", e.getMessage());
        } catch (InvalidKeyException e) {
            log.info("Exception verifying signature {}", e.getMessage());
            e.printStackTrace();
        } catch (SignatureException e) {
            log.info("Exception verifying signature {}", e.getMessage());
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            log.info("Exception verifying signature {}", e.getMessage());
            e.printStackTrace();
        }

        log.debug("EzSecurityUtil Verify\nPublicKey:{}\nData:{}\nEncodedSig:{}\nResult:{}",
                publicKey, data, base64EncodedSignature, verifies);

        return verifies;
    }

    public static void addLevelToAuths(Set<String> original, String clearance) {
        if (original == null) {
            return;
        }

        if (clearance != null && !clearance.isEmpty()) {
            original.add(clearance);
        }
    }

    public static void addCitizenshipToAuths(Set<String> original, String citizenship) {
        if (original == null || citizenship == null || citizenship.isEmpty()) {
            return;
        }

        if (citizenship.equals("USA")) {
            original.add("FOUO");
        }
        original.add(citizenship);
    }
}
