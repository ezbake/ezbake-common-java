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

import org.junit.Ignore;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class EncryptionTest {
	
	private String privateKey = "MIICXAIBAAKBgQD1jG1xKG9vjoJuQ/I9ZP5dbr8hz/C/GabiWZx03uQqVPFv7xKc" +
								"ZFfAM87ADyDYnMV8ygESf05QUC2s2DOcA7fzBIReNkuYVT2yDhp022t379j1tWE+" +
								"PUMOFQOUJohFKfwJ72gZiy7Rm4vEQXuHLJtr86EIirZOc0x9uewQGXfpFQIDAQAB" +
								"AoGAPdEroEhQvaH//iiG7KPnUbhWz/lcn4+irutmcxnGEU4vNkHWyp6MZOvmCf4F" +
								"A+N76G2mlXSNT7TPuur3GzjobvyeUHzXCS8oL0rCTuxVkR64PBcJ+pi14peAh6Jh" +
								"HkYR6Mp48cqJdrtCWIrahvIV03nZamyjc6euuYmAVSMP/cECQQD9FkvrmFOElFIU" +
								"F00JSwFvNiS5s1GoLsO9NYup8YgwXCJfDsx3LJ7bGB8g7jpl3En765q0eXVPXaUw" +
								"y9BJB7rRAkEA+F/rTCaRigbSRN4/aSsZknlYQ4CXfzWpK9yB7rqh3ZWOtnnZibG2" +
								"TS52PAOs359Y1mmw1aJtcOr+k915P6fTBQJBAL7QB6956kYEGZoCM1e1UECL3saP" +
								"lxopH/TQoRsg+mATpupqWufjIWXoWtfWJPtVSgaAjORSyopq/Te8Aq59AHECQEqI" +
								"5mCk1loYb/NQyrCxyWvGVHF1XoFDRjAubSOKCFcpsXkbGegTV4TT45Fg/PjipdM6" +
								"RmHl63fOXXVcKi2rHE0CQCXd3GHlL0WYsioRr3C9bmnPhmZ+4ViqhC9PJsRpjma6" +
								"FPdm8fXuqWapNZb67fxXEOrXg61D5/eEf1yMvbPvPv0=";
	
	private String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD1jG1xKG9vjoJuQ/I9ZP5dbr8h" +
							   "z/C/GabiWZx03uQqVPFv7xKcZFfAM87ADyDYnMV8ygESf05QUC2s2DOcA7fzBIRe" +
							   "NkuYVT2yDhp022t379j1tWE+PUMOFQOUJohFKfwJ72gZiy7Rm4vEQXuHLJtr86EI" +
							   "irZOc0x9uewQGXfpFQIDAQAB"; 
	
	@Ignore
	public void test() throws InvalidKeySpecException, NoSuchAlgorithmException {
			RSAKeyCrypto crypto = new RSAKeyCrypto(privateKey, publicKey);
			RSAKeyCrypto decrypter = new RSAKeyCrypto(privateKey, true);
			
			String text = "Hello, World!";
			// TODO: Fix this to use the new security token.
			
			/*SecurityInfo info = new SecurityInfo();
			AppInfo appInfo = new AppInfo();
			appInfo.setSecurityId("nodeClient");
			info.setAppInfo(appInfo);
			
			List<String> auths = new ArrayList<String>();
			auths.add("SCI");
			info.setAuths(auths);
			ClearanceInfo clear = new ClearanceInfo();
			clear.setCitizenship("USA");
			clear.setClearance("Yes");
			clear.setFormalAccess(auths);
			info.setClearanceInfo(clear);
			
			byte[] value = new TSerializer().serialize(info);
			
			System.out.println("Encrypting!");
			byte[] data = crypto.encrypt(value);
			byte[] unciphered = decrypter.decrypt(data);
			
			
			SecurityInfo newInfo = new SecurityInfo();
			new TDeserializer().deserialize(newInfo, unciphered);
			System.out.println(newInfo.getAppInfo().getSecurityId());
			assertTrue(info.getAppInfo().getSecurityId().equals(newInfo.getAppInfo().getSecurityId()));*/
			
	}
}
