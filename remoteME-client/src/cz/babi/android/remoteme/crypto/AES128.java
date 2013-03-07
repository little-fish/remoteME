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

package cz.babi.android.remoteme.crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.interfaces.DefaultCrypto;

/**
 * Class that provide simple 128bit AES encryption and decryption.
 * 
 * @author babi
 * @author dev.misiarz@gmail.com
 */
public class AES128 implements DefaultCrypto {
	
	private static final String TAG_CLASS_NAME = AES128.class.getSimpleName();
	
	/** for better encryption there is second text which is addet to password */
	private final String secretId;
	/** simple password define by user */
	private final String password;
	/** generated key */
	private Key generatedKey = null;
	
	private Cipher decryptCipher;
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
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[encryptText][" + originalMessage + "]");
		
		String ret = null;
		
		if(this.generatedKey!=null) {
			byte[] encryptedRawData = null;
			try {
				encryptedRawData = encryptCipher.doFinal(originalMessage.getBytes(Common.CHARSET_UTF8));
			} catch (IllegalBlockSizeException ibse) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[Illegal clock size.]", ibse);
			} catch (BadPaddingException bpe) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[Bad padding.]", bpe);
			} catch (UnsupportedEncodingException uee) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[An error occurred while trying to encode text.]", uee);
			}
			
			ret = Base64.encodeToString(encryptedRawData, Base64.DEFAULT);
		}
		
		return ret;
	}
	
	/**
	 * @see cz.babi.desktop.remoteme.interfaces.DefaultCrypto#decryptText(java.lang.String)
	 * @return Decrypted input text. May be null.
	 */
	@Override
	public String decryptText(String encryptedText) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[decryptText][" + encryptedText + "]");
		
		String ret = null;
		
		if(this.generatedKey!=null) {
			byte[] decryptedRawData = Base64.decode(encryptedText, Base64.DEFAULT);
			
			byte[] originalMessageData = null;
			try {
				originalMessageData = decryptCipher.doFinal(decryptedRawData);
			} catch (IllegalBlockSizeException ibse) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[An error occurred while trying to " +
						"retreive original message.]", ibse);
			} catch (BadPaddingException bpe) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[Bad padding.]", bpe);
			}
			
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
			keyValue = (secretId + password).getBytes(Common.CHARSET_UTF8);
		} catch (UnsupportedEncodingException uee) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[An error occurred while trying to encode text.]", uee);
		}
		
		MessageDigest messageDigest = null;
		
		try {
			messageDigest = MessageDigest.getInstance(Common.ALGORITHM_SHA1);
		} catch (NoSuchAlgorithmException nsae) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[Can not find '" + Common.ALGORITHM_SHA1 + "' algorithm.]", nsae);
		}
		
		if(messageDigest!=null) {
			keyValue = messageDigest.digest(keyValue);
			
			// can use only first 128 bit
			byte[] keyValueFinal = new byte[16];
			for(int i=0; i<keyValueFinal.length; i++)
				keyValueFinal[i] = keyValue[i];
			
			this.generatedKey = new SecretKeySpec(keyValueFinal, Common.ALGORITHM_AES);
		}
	}
	
	/**
	 * Init Decrypt Cipher.
	 */
	private void initDecryptCipher() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[initDecryptCipher][Start getting DECRYPT CIPHER.]");
		
		try {
			decryptCipher = Cipher.getInstance(Common.ALGORITHM_AES);
		} catch (NoSuchAlgorithmException nsae) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[Can not find '" + Common.ALGORITHM_AES + "']" +
					" algorithm.", nsae);
		} catch (NoSuchPaddingException nspe) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[No such padding.]", nspe);
		}
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[initDecryptCipher][Finish getting DECRYPT CIPHER.]");
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[initDecryptCipher][Start init DECRYPT CIPHER.]");
		
		try {
			decryptCipher.init(Cipher.DECRYPT_MODE, this.generatedKey);
		} catch (InvalidKeyException ike) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[Invalid generated key.]", ike);
		}
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[initDecryptCipher][Finish init DECRYPT CIPHER.]");
	}
	
	/**
	 * Init Encrypt Cipher.
	 */
	private void initEncryptCipher() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[initEncryptCipher][Start getting ENCRYPT CIPHER.]");
		
		try {
			encryptCipher = Cipher.getInstance(Common.ALGORITHM_AES);
		} catch (NoSuchAlgorithmException nsae) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[Can not find '" + Common.ALGORITHM_AES + "']" +
					" algorithm.", nsae);
		} catch (NoSuchPaddingException nspe) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[No such padding.]", nspe);
		}
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[initEncryptCipher][Finish getting ENCRYPT CIPHER.]");
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[initEncryptCipher][Start init ENCRYPT CIPHER.]");
		
		try {
			encryptCipher.init(Cipher.ENCRYPT_MODE, this.generatedKey);
		} catch (InvalidKeyException ike) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[initEncryptCipher][Invalid generated key.]", ike);
		}
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[initEncryptCipher][Finish init ENCRYPT CIPHER.]");
	}
}
