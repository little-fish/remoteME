/**
 * Copyright 2013 Martin Misiarz (dev.misiarz@gmail.com)
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
 * limitations under the License.
 */

package cz.babi.desktop.remoteme.interfaces;

/**
 * Interface that define default methods for encryption and decryption.
 * 
 * @author babi
 * @author dev.misiarz@gmail.cm
 */
public interface DefaultCrypto {
	
	/**
	 * Method encrypt input text.
	 * @param originalMessage Input text to encrypt.
	 * @return Encrypted input text.
	 */
	public String encryptText(String originalMessage);
	
	/**
	 * Method decrypt input <i>encrypted</i> text.
	 * @param encryptedText Input text to decrypt.
	 * @return Decrypted input text.
	 */
	public String decryptText(String encryptedText);
}
