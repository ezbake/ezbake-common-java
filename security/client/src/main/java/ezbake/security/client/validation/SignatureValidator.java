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

package ezbake.security.client.validation;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import ezbake.crypto.PKeyCrypto;
import ezbake.crypto.utils.EzSSL;

import java.io.IOException;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 10/13/14
 * Time: 3:35 PM
 */
public abstract class SignatureValidator extends BaseValidator {

    protected Supplier<PKeyCrypto> crypto;

    public SignatureValidator(final Properties configuration) {
        super(configuration);
         this.crypto = Suppliers.memoize(new Supplier<PKeyCrypto>() {
             @Override
             public PKeyCrypto get() {
                 try {
                     return EzSSL.getCrypto(configuration);
                 } catch (IOException e) {
                     throw new RuntimeException("Unable to verify tokens without the proper RSA key configuration", e);
                 }
             }
         });
    }
}
