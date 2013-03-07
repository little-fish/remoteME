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

package cz.babi.desktop.remoteme.crypto;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import cz.babi.desktop.remoteme.common.Common;
import cz.babi.desktop.remoteme.interfaces.DefaultCrypto;

/**
 * Class that provide simple 128bit AES encryption and decryption.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class AES128 implements DefaultCrypto {
	
	private static final Logger LOGGER = Logger.getLogger(AES128.class.getSimpleName());
	
	/** for better encryption there is second text which is addet to password */
	private final String secretId;
	/** simple password define by user */
	private final String password;
	/** generated key */
	private Key generatedKey = null;
	
	private Cipher decrypCipher;
	private Cipher encryptCipher;
	
	/**
	 * Constructor.
	 * @param secretId Secret ID define by app-maker.
	 * @param password Password define by user.
	 */
	public AES128(String secretId, String password) {
		this.secretId = (secretId!=null) ? secretId : "";
		this.password = (password!=null) ? password : "";
		
		generateKey();
		
		initDecryptCipher();
		initEncryptCipher();
	}
	
	/**
	 * @see cz.babi.desktop.remoteme.interfaces.DefaultCrypto#encryptText(java.lang.String)
	 * @return Encrypted input text. May be null.
	 */
	@Override
	public String encryptText(String originalMessage) {
		if(Common.DEBUG) LOGGER.debug("[encryptText][" + originalMessage + "]");
		
		String ret = null;
		
		if(this.generatedKey!=null) {
			byte[] encryptedRawData = null;
			try {
				encryptedRawData = encryptCipher.doFinal(originalMessage.getBytes(Common.CHARSET));
			} catch (IllegalBlockSizeException | BadPaddingException
					| UnsupportedEncodingException ex) {
				if(Common.ERROR) LOGGER.error("[encryptText][An error occurred while " +
						"trying to encrypt input text.]", ex);
			}
			
			ret = new BASE64Encoder().encode(encryptedRawData);
		}
		
		return ret;
	}
	
	/**
	 * @see cz.babi.desktop.remoteme.interfaces.DefaultCrypto#decryptText(java.lang.String)
	 * @return Decrypted input text. May be null.
	 */
	@Override
	public String decryptText(String encryptedText) {
		if(Common.DEBUG) LOGGER.debug("[decryptText][" + encryptedText + "]");
		
		String ret = null;
		
		if(this.generatedKey!=null) {
			byte[] decryptedRawData = null;
			try {
				decryptedRawData = new BASE64Decoder().decodeBuffer(encryptedText);
			} catch (IOException ioe) {
				if(Common.ERROR) LOGGER.error("[decryptText][An error occurred while " +
						"trying to decode input text.]", ioe);
			}
			
			byte[] originalMessageData = null;
			try {
				originalMessageData = decrypCipher.doFinal(decryptedRawData);
			} catch (IllegalBlockSizeException | BadPaddingException e) {
				if(Common.ERROR) LOGGER.error("[decryptText][An error occurred while trying " +
						"to retreive original message.]", e);
			}
			
			if(originalMessageData==null) return null;
			
			ret = new String(originalMessageData);
		}
		
		return ret;
	}
	
	/**
	 * Method generate Key for encryption and decryption.
	 * @return Generated Key. May be null.
	 */
	private void generateKey() {
		byte[] keyValue = null;
		
		try {
			keyValue = (secretId + password).getBytes(Common.CHARSET);
		} catch (UnsupportedEncodingException uee) {
			if(Common.ERROR) LOGGER.error("[generateKey][An error occurred while trying " +
					"to encode text.]", uee);
		}
		
		MessageDigest messageDigest = null;
		
		try {
			messageDigest = MessageDigest.getInstance(Common.ALGORITHM_SHA1);
		} catch (NoSuchAlgorithmException nsae) {
			if(Common.ERROR) LOGGER.error("[generateKey][Can not find '" + Common.ALGORITHM_SHA1 +
					"' algorithm.]", nsae);
		}
		
		if(messageDigest!=null) {
			keyValue = messageDigest.digest(keyValue);
			
			/* can use only first 128 bit */
			keyValue = Arrays.copyOf(keyValue, 16);
			
			this.generatedKey = new SecretKeySpec(keyValue, Common.ALGORITHM_AES);
		}
	}
	
	/**
	 * Init Decrypt Cipher.
	 */
	private void initDecryptCipher() {
		if(Common.DEBUG) LOGGER.debug("[initDecryptCipher][Start getting DECRYPT CIPHER.]");
		
		try {
			decrypCipher = Cipher.getInstance(Common.ALGORITHM_AES);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
			if(Common.ERROR) LOGGER.error("[initDecryptCipher][Can not find " +
					"'" + Common.ALGORITHM_AES + "' algorithm.]", ex);
		}
		
		if(Common.DEBUG) LOGGER.debug("[initDecryptCipher][Finish getting DECRYPT CIPHER.]");
		if(Common.DEBUG) LOGGER.debug("[initDecryptCipher][Start init DECRYPT CIPHER.]");
		
		try {
			decrypCipher.init(Cipher.DECRYPT_MODE, this.generatedKey);
		} catch (InvalidKeyException ike) {
			if(Common.ERROR) LOGGER.error("[initDecryptCipher][Invalid generated key.]", ike);
		}
		
		if(Common.DEBUG) LOGGER.debug("[initDecryptCipher][Finish init DECRYPT CIPHER.]");
	}
	
	/**
	 * Init Encrypt Cipher.
	 */
	private void initEncryptCipher() {
		if(Common.DEBUG) LOGGER.debug("[initEncryptCipher][Start getting ENCRYPT CIPHER.]");
		
		try {
			encryptCipher = Cipher.getInstance(Common.ALGORITHM_AES);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
			if(Common.ERROR) LOGGER.error("[initEncryptCipher][Can not find '" + Common.ALGORITHM_AES + "' " +
					"algorithm.]", ex);
		}
		
		if(Common.DEBUG) LOGGER.debug("[initEncryptCipher][Finish getting ENCRYPT CIPHER.]");
		if(Common.DEBUG) LOGGER.debug("[initEncryptCipher][Start init ENCRYPT CIPHER.]");
		
		try {
			encryptCipher.init(Cipher.ENCRYPT_MODE, this.generatedKey);
		} catch (InvalidKeyException ike) {
			if(Common.ERROR) LOGGER.error("[initEncryptCipher][Invalid generated key.]", ike);
		}
		
		if(Common.DEBUG) LOGGER.debug("[initEncryptCipher][Finish init ENCRYPT CIPHER.]");
	}
}
