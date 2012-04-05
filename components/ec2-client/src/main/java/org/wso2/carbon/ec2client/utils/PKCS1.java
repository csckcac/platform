/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.ec2client.utils;
//  Read a private key, in the PKCS1 format naturally created by openssl:
//  $ openssl req -nodes -newkey 1024 -keyout file.key -out file.req
//  ... then submit file.req to UW CA http://certs.cac.washington.edu,
//  and save result to file.crt.  The contents of file.key will look like
//
//  -----BEGIN RSA PRIVATE KEY-----
//  MIICXwIBAAKBgQDMTApZEOCWwGf4lXk... (base 64 encoded stuff)
//  -----END RSA PRIVATE KEY-----
//
//  Cf. ftp://ftp.rsasecurity.com/pub/pkcs/pkcs-1/pkcs-1v2-1.pdf
//
//  The external entry is
//    readKey(String), which returns PrivateKey
//

import sun.misc.BASE64Decoder;

import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;


public class PKCS1 {
	int pos;
	byte[] code;
	BigInteger[] ints;

	public PKCS1() { }

	RSAPrivateCrtKeySpec keySpec() {
		return new RSAPrivateCrtKeySpec(ints[0] // modulus
				, ints[1]	// publicExponent
				, ints[2]	// privateExponent
				, ints[3]	// primeP
				, ints[4]	// primeQ
				, ints[5]	// primeExponentP
				, ints[6]	// primeExponentQ
				, ints[7]	// crtCoefficient
				);
	}
	int rdLen() throws IOException {
		int t;
		if ((code[pos] & 0x80) == 0x80) {
			int n = (int) code[pos] & 0x7f;
			pos = pos + 1;
			t = rdLongLen(n);
		} else {
			t = (int) code[pos];
			pos = pos + 1;
		}
		return t;
	}
	int rdLongLen(int n) throws IOException {
		int r = 0;
		for (int i = 0; i < n; ++i) {
			r = (r << 8) | (code[pos] & 0xff);
			pos = pos + 1;
		}
		return r;
	}
	void skipInteger() throws IOException {
		if (code[pos] != 2)
			throw new IOException("encountered invalid integer tag "
				+ ((int) code[pos]) + " at " + pos);
		pos = pos + 1;
		int len = rdLen();
		pos = pos + len;
	}
	BigInteger rdInteger() throws IOException {
		if (pos >= code.length)
			throw new EOFException("end of file at " + pos);
		if (code[pos] != 2)
			throw new IOException("encountered invalid integer tag "
				+ ((int) code[pos]) + " at " + pos);
		pos = pos + 1;
		int len = rdLen();
		byte[] x = new byte[len];
		System.arraycopy(code, pos, x, 0, len);
		pos = pos + len;
		return new BigInteger(x);
	}
	void rdKey(int nb) throws IOException {
		ints = new BigInteger[8];
		skipInteger();  // version
		for (int i = 0; i < 8; ++i)
			ints[i] = rdInteger();
	}
	public void extractIntegers(byte[] data) throws IOException {
		pos = 0;
		code = data;
		if (code[pos] == 0x30) {
			pos = 1;
			int nb = rdLen();
			rdKey(nb);
		} else
			throw new IOException("invalid private key leading tag "
					+ (int) code[pos]);
	}
	char[] readWrappedBody(String name) throws IOException {
		FileReader file = new FileReader(name);
		char[] ba = new char[1024];
		int i;
		boolean igl = true;
		int p = 0;
		for (i = 0; i < 1024; ) {
			int ic = file.read();
			char c = (char) ic;
			if (ic < 0)
				break;
			else if (c == '\n') {
				igl = false;
				p = 0;
			} else if (igl) {
			} else {
				if (c == '-')
					igl = true;
				else {
					ba[i] = c;
					i = i + 1;
				}
			}
			p = p + 1;
		}
		char[] contents = new char[i];
		System.arraycopy(ba, 0, contents, 0, i);
		return contents;
	}

    public byte[] readDecodedBytes(String name) throws IOException {
		char[] ba = readWrappedBody(name);
		int n = ba.length;
		return new BASE64Decoder().decodeBuffer(new String(ba, 0, n));
	}
	RSAPrivateCrtKeySpec readKeyFile(String name) throws IOException {
		byte[] data = readDecodedBytes(name);
		extractIntegers(data);
		return keySpec();
	}
	public PrivateKey readKey(String name) throws IOException {
		RSAPrivateCrtKeySpec sp = readKeyFile(name);
		KeyFactory kf;
		try {
			kf = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("RSA: " + e.toString());
		}
		PrivateKey pk;
		try {
			 pk = kf.generatePrivate(sp);
		} catch (InvalidKeySpecException e) {
			throw new IOException(e.toString());
		}
		return pk;
	}
}
